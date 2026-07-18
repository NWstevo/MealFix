package com.example.mealfix.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.mealfix.data.Day
import com.example.mealfix.data.MealWithIngredients
import com.example.mealfix.data.ScheduledMeal
import kotlin.math.roundToInt

/**
 * The weekly schedule: set a calorie target, then assign one confirmed Menu meal to each
 * day. Since a meal's kcal is already fixed once confirmed, this doesn't calculate anything
 * backwards — it just tallies up what you've scheduled against the target as you go.
 */
@Composable
fun TargetScreen(
    meals: List<MealWithIngredients>,
    scheduledMeals: List<ScheduledMeal>,
    weeklyTargetKcal: Double,
    totalScheduledKcal: Double,
    scheduledProgressFraction: Float,
    onSetWeeklyTarget: (Double) -> Unit,
    onScheduleMeal: (day: Day, mealId: String) -> Unit,
    onUnscheduleDay: (day: Day) -> Unit,
    modifier: Modifier = Modifier,
) {
    var targetInput by remember(weeklyTargetKcal) { mutableStateOf(weeklyTargetKcal.roundToInt().toString()) }

    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Text("Weekly calorie target", style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = targetInput,
                onValueChange = { targetInput = it },
                label = { Text("kcal for the week") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f).padding(top = 8.dp),
            )
            Button(
                onClick = { targetInput.toDoubleOrNull()?.let(onSetWeeklyTarget) },
                modifier = Modifier.padding(start = 8.dp, top = 8.dp),
            ) {
                Text("Set")
            }
        }

        LinearProgressIndicator(
            progress = { scheduledProgressFraction },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        )
        Text(
            "${totalScheduledKcal.roundToInt()} / ${weeklyTargetKcal.roundToInt()} kcal scheduled this week",
            modifier = Modifier.padding(top = 4.dp),
        )

        Text(
            "This week's schedule",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
        )

        if (meals.isEmpty()) {
            Text("Confirm a meal in the Meal tab first before you can schedule anything.")
        } else {
            Day.entries.forEach { day ->
                val scheduledMealId = scheduledMeals.find { it.day == day }?.mealId
                val scheduledMeal = meals.find { it.meal.id == scheduledMealId }

                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(day.label, style = MaterialTheme.typography.bodyLarge)
                        SimpleDropdown(
                            label = "meal",
                            options = listOf<MealWithIngredients?>(null) + meals,
                            selected = scheduledMeal,
                            optionLabel = { it?.meal?.name ?: "— None —" },
                            onSelect = { selection ->
                                if (selection == null) onUnscheduleDay(day) else onScheduleMeal(day, selection.meal.id)
                            },
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        if (scheduledMeal != null) {
                            Text(
                                "${scheduledMeal.meal.totalKcal.roundToInt()} kcal",
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
