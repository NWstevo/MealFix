package com.example.mealfix.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mealfix.data.Day
import com.example.mealfix.data.LogEntry
import com.example.mealfix.data.MealWithIngredients
import kotlin.math.roundToInt

/**
 * The diet-tracking half of the app: for each day of the week, pick which confirmed Menu
 * meal was actually eaten. Selecting a meal for a day logs it immediately (replacing
 * whatever was logged for that day before); picking "— None —" un-logs it. The weekly
 * target set in the Target tab is reduced automatically as meals are logged, with what's
 * left shown at the bottom.
 */
@Composable
fun TrackerScreen(
    meals: List<MealWithIngredients>,
    logEntries: List<LogEntry>,
    weeklyTargetKcal: Double,
    totalKcalConsumed: Double,
    progressFraction: Float,
    remainingKcal: Double,
    onLogMeal: (day: Day, mealId: String) -> Unit,
    onUnlogDay: (day: Day) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Text("Weekly progress", style = MaterialTheme.typography.titleMedium)
        LinearProgressIndicator(
            progress = { progressFraction },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )
        Text(
            "${totalKcalConsumed.roundToInt()} / ${weeklyTargetKcal.roundToInt()} kcal logged this week",
            modifier = Modifier.padding(top = 4.dp),
        )

        Text(
            "This week's log",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
        )

        if (meals.isEmpty()) {
            Text("Confirm a meal in the Meal tab first before you can log anything.")
        } else {
            Day.entries.forEach { day ->
                val loggedMealId = logEntries.find { it.day == day }?.mealId
                val loggedMeal = meals.find { it.meal.id == loggedMealId }

                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(day.label, style = MaterialTheme.typography.bodyLarge)
                        SimpleDropdown(
                            label = "meal",
                            options = listOf<MealWithIngredients?>(null) + meals,
                            selected = loggedMeal,
                            optionLabel = { it?.meal?.name ?: "— None —" },
                            onSelect = { selection ->
                                if (selection == null) onUnlogDay(day) else onLogMeal(day, selection.meal.id)
                            },
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        if (loggedMeal != null) {
                            Text(
                                "${loggedMeal.meal.totalKcal.roundToInt()} kcal",
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        Text(
            if (remainingKcal >= 0.0) {
                "Remaining: ${remainingKcal.roundToInt()} kcal"
            } else {
                "Over budget by ${(-remainingKcal).roundToInt()} kcal"
            },
            style = MaterialTheme.typography.titleMedium,
        )
    }
}
