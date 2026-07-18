package com.example.mealfix.viewmodel

import androidx.lifecycle.ViewModel
import com.example.mealfix.data.Day
import com.example.mealfix.data.LogEntry
import com.example.mealfix.data.Meal
import com.example.mealfix.data.PlannedMeal
import com.example.mealfix.data.WeeklyTarget
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Everything the UI needs to render, bundled into one immutable snapshot.
 * Compose screens observe this via collectAsState() and recompose whenever it changes.
 */
data class MealPlannerUiState(
    val meals: List<Meal> = emptyList(),
    val weeklyTarget: WeeklyTarget = WeeklyTarget(kcalPerWeek = 14000.0), // ~2000 kcal/day default
    val plannedMeals: List<PlannedMeal> = emptyList(),
    val logEntries: List<LogEntry> = emptyList(),
) {
    fun mealById(id: String): Meal? = meals.find { it.id == id }

    val totalKcalConsumed: Double
        get() = logEntries.sumOf { entry -> mealById(entry.mealId)?.let { entry.kcalConsumed(it) } ?: 0.0 }

    /** Fraction of the weekly target eaten so far, clamped to [0, 1] for progress bars. */
    val progressFraction: Float
        get() = if (weeklyTarget.kcalPerWeek <= 0.0) 0f
        else (totalKcalConsumed / weeklyTarget.kcalPerWeek).toFloat().coerceIn(0f, 1f)
}

/**
 * Holds the app's state in memory (nothing is saved to disk yet — that's the next phase,
 * once this version is confirmed running). All state changes go through this class so
 * every screen sees the same, consistent data.
 */
class MealPlannerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MealPlannerUiState())
    val uiState: StateFlow<MealPlannerUiState> = _uiState.asStateFlow()

    // ---------- Meal library ----------

    fun addMeal(name: String, kcalPerKg: Double) {
        if (name.isBlank() || kcalPerKg <= 0.0) return
        _uiState.update { it.copy(meals = it.meals + Meal(name = name.trim(), kcalPerKg = kcalPerKg)) }
    }

    fun deleteMeal(mealId: String) {
        _uiState.update {
            it.copy(
                meals = it.meals.filterNot { m -> m.id == mealId },
                // also drop anything that referenced this meal so we don't leave orphaned entries
                plannedMeals = it.plannedMeals.filterNot { p -> p.mealId == mealId },
                logEntries = it.logEntries.filterNot { l -> l.mealId == mealId },
            )
        }
    }

    // ---------- Weekly plan ----------

    fun setWeeklyTarget(kcal: Double) {
        if (kcal <= 0.0) return
        _uiState.update { current ->
            val perMeal = if (current.plannedMeals.isEmpty()) 0.0 else kcal / current.plannedMeals.size
            current.copy(
                weeklyTarget = WeeklyTarget(kcalPerWeek = kcal),
                plannedMeals = current.plannedMeals.map { it.copy(targetKcal = perMeal) },
            )
        }
    }

    /** Adds a meal to the weekly plan and re-splits the weekly target evenly across all planned meals. */
    fun addPlannedMeal(mealId: String, day: Day) {
        _uiState.update { current ->
            val newCount = current.plannedMeals.size + 1
            val perMeal = current.weeklyTarget.kcalPerWeek / newCount
            val rebalanced = current.plannedMeals.map { it.copy(targetKcal = perMeal) }
            current.copy(
                plannedMeals = rebalanced + PlannedMeal(mealId = mealId, day = day, targetKcal = perMeal),
            )
        }
    }

    fun removePlannedMeal(plannedMealId: String) {
        _uiState.update { current ->
            val remaining = current.plannedMeals.filterNot { it.id == plannedMealId }
            val perMeal = if (remaining.isEmpty()) 0.0 else current.weeklyTarget.kcalPerWeek / remaining.size
            current.copy(plannedMeals = remaining.map { it.copy(targetKcal = perMeal) })
        }
    }

    // ---------- Tracker ----------

    fun logMeal(mealId: String, day: Day, quantityKg: Double) {
        if (quantityKg <= 0.0) return
        _uiState.update {
            it.copy(logEntries = it.logEntries + LogEntry(mealId = mealId, day = day, quantityKg = quantityKg))
        }
    }

    fun deleteLogEntry(logEntryId: String) {
        _uiState.update { it.copy(logEntries = it.logEntries.filterNot { l -> l.id == logEntryId }) }
    }
}
