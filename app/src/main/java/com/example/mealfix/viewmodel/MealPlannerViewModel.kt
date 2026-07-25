package com.example.mealfix.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mealfix.data.Day
import com.example.mealfix.data.DraftIngredient
import com.example.mealfix.data.MealPlannerRepository
import com.example.mealfix.data.MealPlannerUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * A thin layer between the UI and the repository: it exposes the repository's reactive
 * uiState as something Compose can collect, and turns each user action into a coroutine
 * that calls the matching suspend function on the repository. No data lives in this class
 * anymore — Room (via the repository) is the single source of truth now.
 */
class MealPlannerViewModel(private val repository: MealPlannerRepository) : ViewModel() {

    val uiState: StateFlow<MealPlannerUiState> = repository.uiState.stateIn(
        scope = viewModelScope,
        // Keeps the underlying Flow alive for 5s after the last observer disappears,
        // so quick screen rotations/navigation don't restart the whole query needlessly.
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MealPlannerUiState(),
    )

    // ---------- Food shelf ----------

    fun addFoodItem(name: String, referenceKcal: Double, referenceGrams: Double) {
        viewModelScope.launch { repository.addFoodItem(name, referenceKcal, referenceGrams) }
    }

    fun deleteFoodItem(foodItemId: String) {
        viewModelScope.launch { repository.deleteFoodItem(foodItemId) }
    }

    // ---------- Meal builder ----------

    fun confirmMeal(name: String, ingredients: List<DraftIngredient>) {
        viewModelScope.launch { repository.confirmMeal(name, ingredients) }
    }

    fun deleteMeal(mealId: String) {
        viewModelScope.launch { repository.deleteMeal(mealId) }
    }

    // ---------- Target (weekly schedule) ----------

    fun setDailyTarget(dailyKcal: Double) {
        viewModelScope.launch { repository.setDailyTarget(dailyKcal) }
    }

    fun scheduleMeal(day: Day, mealId: String) {
        viewModelScope.launch { repository.scheduleMeal(day, mealId) }
    }

    fun unscheduleDay(day: Day) {
        viewModelScope.launch { repository.unscheduleDay(day) }
    }

    // ---------- Tracker ----------

    fun logMeal(day: Day, mealId: String) {
        viewModelScope.launch { repository.logMeal(day, mealId) }
    }

    fun unlogDay(day: Day) {
        viewModelScope.launch { repository.unlogDay(day) }
    }
}

/**
 * Compose's viewModel() function needs to know how to construct a MealPlannerViewModel,
 * but it only knows how to build no-argument ViewModels by default. This factory tells it
 * how, by supplying the repository ourselves (see MealFixApplication and MainActivity).
 */
class MealPlannerViewModelFactory(
    private val repository: MealPlannerRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MealPlannerViewModel(repository) as T
    }
}
