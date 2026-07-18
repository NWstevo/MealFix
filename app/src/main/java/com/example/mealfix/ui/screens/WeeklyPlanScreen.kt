package com.example.mealfix.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.mealfix.data.Meal
import com.example.mealfix.data.PlannedMeal

/**
 * Where the weekly calorie target is set and meals get assigned to days.
 * The target is currently split evenly across every planned meal (see MealPlannerViewModel) —
 * that's what turns "weekly target" into "kg needed for this one meal".
 */
@Composable
fun WeeklyPlanScreen(
    meals: List<Meal>,
    plannedMeals: List<PlannedMeal>,
    weeklyTargetKcal: Double,
    onSetWeeklyTarget: (Double) -> Unit,
    onAddPlannedMeal: (mealId: String, day: Day) -> Unit,
    onRemovePlannedMeal: (plannedMealId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var targetInput by remember(weeklyTargetKcal) { mutableStateOf(weeklyTargetKcal.toInt().toString()) }
    var selectedMeal by remember(meals) { mutableStateOf(meals.firstOrNull()) }
    var selectedDay by remember { mutableStateOf(Day.MONDAY) }

    Column(modifier = modifier.padding(16.dp)) {
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

        Text(
            "Add a meal to the plan",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 24.dp),
        )

        if (meals.isEmpty()) {
            Text(
                "Add meals in the Library tab first — you need at least one before you can plan a week.",
                modifier = Modifier.padding(top = 8.dp),
            )
        } else {
            SimpleDropdown(
                label = "meal",
                options = meals,
                selected = selectedMeal,
                optionLabel = { it.name },
                onSelect = { selectedMeal = it },
                modifier = Modifier.padding(top = 8.dp),
            )
            SimpleDropdown(
                label = "day",
                options = Day.entries,
                selected = selectedDay,
                optionLabel = { it.label },
                onSelect = { selectedDay = it },
                modifier = Modifier.padding(top = 8.dp),
            )
            Button(
                onClick = { selectedMeal?.let { onAddPlannedMeal(it.id, selectedDay) } },
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text("Add to plan")
            }
        }

        Text(
            "This week's plan",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
        )

        if (plannedMeals.isEmpty()) {
            Text("Nothing planned yet.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(plannedMeals, key = { it.id }) { planned ->
                    val meal = meals.find { it.id == planned.mealId }
                    Card {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column {
                                Text("${planned.day.label} — ${meal?.name ?: "Unknown meal"}")
                                Text("${planned.targetKcal.toInt()} kcal allocated")
                                if (meal != null) {
                                    Text("≈ %.2f kg needed".format(planned.kgNeeded(meal)))
                                }
                            }
                            TextButton(onClick = { onRemovePlannedMeal(planned.id) }) {
                                Text("Remove")
                            }
                        }
                    }
                }
            }
        }
    }
}
