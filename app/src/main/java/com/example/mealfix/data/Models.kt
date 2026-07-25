package com.example.mealfix.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * The days a meal can be scheduled for. Kept as our own enum (rather than java.time.DayOfWeek)
 * so the UI can control ordering and display easily. Room can't store an enum directly, so
 * it's converted to/from a plain String by Converters (see data/local/Converters.kt).
 */
enum class Day(val label: String) {
    MONDAY("Mon"),
    TUESDAY("Tue"),
    WEDNESDAY("Wed"),
    THURSDAY("Thu"),
    FRIDAY("Fri"),
    SATURDAY("Sat"),
    SUNDAY("Sun"),
}

/**
 * A raw ingredient in your "food shelf". Rather than asking you to pre-calculate a
 * calories-per-gram figure, this stores the numbers exactly as they appear on the
 * package's nutrition label — e.g. "375 kcal per 500 g" — and derives kcalPerGram from
 * them. referenceKcal/referenceGrams are stored (as real columns); kcalPerGram is computed
 * on the fly from those two and isn't a database column at all.
 */
@Entity(tableName = "food_items")
data class FoodItem(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val referenceKcal: Double,
    val referenceGrams: Double,
) {
    /** Calories per gram, derived from the package reference values. */
    val kcalPerGram: Double
        get() = if (referenceGrams > 0.0) referenceKcal / referenceGrams else 0.0
}

/**
 * A confirmed, saved meal — a named combination of food items with a fixed total calorie
 * count. Once confirmed, a meal's recipe (its MealIngredient rows) doesn't change; it's a
 * stable record you can schedule (Target tab) or log (Tracker tab) again and again.
 */
@Entity(tableName = "meals")
data class Meal(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val totalKcal: Double,
)

/**
 * One ingredient line within a confirmed meal's recipe: how many grams of which food item,
 * and how many kcal that line contributes. foodItemName and kcal are snapshotted at the
 * moment the meal is confirmed, so a meal's history stays accurate even if the food item's
 * name or kcal/gram is edited (or the food item is deleted) later.
 */
@Entity(tableName = "meal_ingredients")
data class MealIngredient(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val mealId: String,
    val foodItemId: String,
    val foodItemName: String,
    val gramsUsed: Double,
    val kcal: Double,
)

/**
 * Which confirmed meal is planned for which day of the week. Only one meal per day —
 * assigning a new meal to a day that already has one replaces it (see WeeklyTargetDao's
 * use of @Upsert with `day` as the primary key).
 */
@Entity(tableName = "scheduled_meals")
data class ScheduledMeal(
    @PrimaryKey val day: Day,
    val mealId: String,
)

/**
 * The overall calorie goal, set as a daily figure (id is always 0 — a single-row "settings"
 * table). The weekly figure used everywhere else in the app is derived from this, not stored
 * separately, so the two can never drift out of sync.
 */
@Entity(tableName = "weekly_target")
data class WeeklyTarget(
    @PrimaryKey val id: Int = 0,
    val dailyTargetKcal: Double,
) {
    val kcalPerWeek: Double
        get() = dailyTargetKcal * 7
}

/**
 * Which confirmed meal was actually eaten on which day of the week — the "tracker" half of
 * the app. Only one logged meal per day, mirroring ScheduledMeal: picking a different meal
 * for a day that's already logged replaces it (see LogEntryDao's use of @Upsert with `day`
 * as the primary key).
 */
@Entity(tableName = "log_entries")
data class LogEntry(
    @PrimaryKey val day: Day,
    val mealId: String,
)
