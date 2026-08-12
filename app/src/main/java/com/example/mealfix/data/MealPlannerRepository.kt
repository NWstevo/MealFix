package com.example.mealfix.data

import androidx.room.withTransaction
import com.example.mealfix.data.local.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

private const val DEFAULT_DAILY_KCAL = 2000.0 // used until the user sets their own

/** A confirmed meal bundled with the ingredient lines that make it up. */
data class MealWithIngredients(
    val meal: Meal,
    val ingredients: List<MealIngredient>,
)

/**
 * One ingredient row being assembled in the Meal builder screen, before the meal is
 * confirmed and saved. Not persisted on its own — only used to pass a whole draft
 * recipe into [MealPlannerRepository.confirmMeal] at once.
 */
data class DraftIngredient(
    val foodItemId: String,
    val foodItemName: String,
    val gramsUsed: Double,
    val kcal: Double,
)

/**
 * Everything the UI needs to render, bundled into one immutable snapshot.
 * Compose screens observe this via collectAsState() and recompose whenever it changes.
 */
data class MealPlannerUiState(
    val foodItems: List<FoodItem> = emptyList(),
    val meals: List<MealWithIngredients> = emptyList(),
    val weeklyTarget: WeeklyTarget = WeeklyTarget(dailyTargetKcal = DEFAULT_DAILY_KCAL),
    val scheduledMeals: List<ScheduledMeal> = emptyList(),
    val logEntries: List<LogEntry> = emptyList(),
) {
    fun mealById(id: String): Meal? = meals.find { it.meal.id == id }?.meal

    /** The meal Target has scheduled for a day, if any. */
    fun scheduledMealFor(day: Day): Meal? = scheduledMeals.find { it.day == day }?.mealId?.let(::mealById)

    /** Total kcal across everything currently scheduled for the week (the Target tab's tally). */
    val totalScheduledKcal: Double
        get() = scheduledMeals.sumOf { scheduled -> mealById(scheduled.mealId)?.totalKcal ?: 0.0 }

    val scheduledProgressFraction: Float
        get() = if (weeklyTarget.kcalPerWeek <= 0.0) 0f
        else (totalScheduledKcal / weeklyTarget.kcalPerWeek).toFloat().coerceIn(0f, 1f)

    /**
     * Total kcal for days whose scheduled meal has been ticked as "followed" (the Tracker
     * tab's tally) — always the *currently* scheduled meal for that day, never a stale
     * snapshot, since LogEntry itself carries no meal reference.
     */
    val totalKcalConsumed: Double
        get() = logEntries.sumOf { entry -> scheduledMealFor(entry.day)?.totalKcal ?: 0.0 }

    val progressFraction: Float
        get() = if (weeklyTarget.kcalPerWeek <= 0.0) 0f
        else (totalKcalConsumed / weeklyTarget.kcalPerWeek).toFloat().coerceIn(0f, 1f)

    /** What's left of the weekly target after everything logged so far — can go negative if over budget. */
    val remainingKcal: Double
        get() = weeklyTarget.kcalPerWeek - totalKcalConsumed
}

/**
 * The single source of truth for MealFix's data. This is the layer that talks to Room —
 * nothing above it (the ViewModel, the screens) needs to know SQL or DAOs exist at all,
 * they just see a reactive [uiState] stream and a handful of suspend functions to call.
 */
class MealPlannerRepository(private val db: AppDatabase) {
    private val foodItemDao = db.foodItemDao()
    private val mealDao = db.mealDao()
    private val mealIngredientDao = db.mealIngredientDao()
    private val scheduledMealDao = db.scheduledMealDao()
    private val logEntryDao = db.logEntryDao()
    private val weeklyTargetDao = db.weeklyTargetDao()

    /** Joins meals with their ingredient lines — kept separate so the outer combine() below stays within combine's 5-flow overload. */
    private val mealsWithIngredients: Flow<List<MealWithIngredients>> = combine(
        mealDao.getAll(),
        mealIngredientDao.getAll(),
    ) { meals, ingredients ->
        meals.map { meal ->
            MealWithIngredients(meal = meal, ingredients = ingredients.filter { it.mealId == meal.id })
        }
    }

    /**
     * combine() merges the independent Flows into one. Whenever ANY of the underlying
     * tables changes, this re-runs and emits a fresh, fully up-to-date MealPlannerUiState.
     */
    val uiState: Flow<MealPlannerUiState> = combine(
        foodItemDao.getAll(),
        mealsWithIngredients,
        weeklyTargetDao.getTarget(),
        scheduledMealDao.getAll(),
        logEntryDao.getAll(),
    ) { foodItems, meals, target, scheduled, logs ->
        MealPlannerUiState(
            foodItems = foodItems,
            meals = meals,
            weeklyTarget = target ?: WeeklyTarget(dailyTargetKcal = DEFAULT_DAILY_KCAL),
            scheduledMeals = scheduled,
            logEntries = logs,
        )
    }

    // ---------- Food shelf ----------

    suspend fun addFoodItem(name: String, referenceKcal: Double, referenceGrams: Double) {
        if (name.isBlank() || referenceKcal <= 0.0 || referenceGrams <= 0.0) return
        foodItemDao.insert(
            FoodItem(name = name.trim(), referenceKcal = referenceKcal, referenceGrams = referenceGrams),
        )
    }

    suspend fun deleteFoodItem(foodItemId: String) {
        // Meals keep their snapshotted ingredient data even if the food item that inspired
        // them is deleted later — a confirmed meal is a historical record, not a live join.
        foodItemDao.deleteById(foodItemId)
    }

    // ---------- Meal builder ----------

    /**
     * Confirms a draft meal: saves the Meal row (with its total kcal) and all its ingredient
     * lines together in one atomic transaction, so a meal never ends up half-saved if
     * something goes wrong partway through.
     */
    suspend fun confirmMeal(name: String, ingredients: List<DraftIngredient>) {
        if (name.isBlank() || ingredients.isEmpty()) return
        db.withTransaction {
            val meal = Meal(name = name.trim(), totalKcal = ingredients.sumOf { it.kcal })
            mealDao.insert(meal)
            mealIngredientDao.insertAll(
                ingredients.map {
                    MealIngredient(
                        mealId = meal.id,
                        foodItemId = it.foodItemId,
                        foodItemName = it.foodItemName,
                        gramsUsed = it.gramsUsed,
                        kcal = it.kcal,
                    )
                },
            )
        }
    }

    suspend fun deleteMeal(mealId: String) {
        // The days this meal was scheduled for are about to lose that schedule, so any
        // "followed" tick on them stops meaning anything — clear those before unscheduling.
        val affectedDays = scheduledMealDao.getDaysByMealId(mealId)
        mealDao.deleteById(mealId)
        mealIngredientDao.deleteByMealId(mealId)
        scheduledMealDao.deleteByMealId(mealId)
        affectedDays.forEach { logEntryDao.deleteByDay(it) }
    }

    // ---------- Target (weekly schedule) ----------

    suspend fun setDailyTarget(dailyKcal: Double) {
        if (dailyKcal <= 0.0) return
        weeklyTargetDao.upsert(WeeklyTarget(dailyTargetKcal = dailyKcal))
    }

    /**
     * Assigns a confirmed meal to a day — replaces whatever was previously scheduled for that
     * day. Also clears any "followed" tick on that day, since it referred to the old plan.
     */
    suspend fun scheduleMeal(day: Day, mealId: String) {
        scheduledMealDao.upsert(ScheduledMeal(day = day, mealId = mealId))
        logEntryDao.deleteByDay(day)
    }

    suspend fun unscheduleDay(day: Day) {
        scheduledMealDao.deleteByDay(day)
        logEntryDao.deleteByDay(day)
    }

    // ---------- Tracker ----------

    suspend fun setDayFollowed(day: Day, followed: Boolean) {
        if (followed) logEntryDao.markFollowed(LogEntry(day = day)) else logEntryDao.deleteByDay(day)
    }
}
