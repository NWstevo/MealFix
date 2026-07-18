package com.example.mealfix.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mealfix.data.Day
import com.example.mealfix.data.LogEntry
import com.example.mealfix.data.MealWithIngredients
import kotlin.math.roundToInt

/**
 * The diet-tracking half of the app: log which confirmed Menu meals you actually ate, and
 * see how that stacks up against the weekly calorie target set in the Target tab. Since a
 * meal's kcal is fixed once confirmed, logging it is just "I ate this, on this day" —
 * no quantity to enter.
 */
@Composable
fun TrackerScreen(
    meals: List<MealWithIngredients>,
    logEntries: List<LogEntry>,
    weeklyTargetKcal: Double,
    totalKcalConsumed: Double,
    progressFraction: Float,
    onLogMeal: (mealId: String, day: Day) -> Unit,
    onDeleteLogEntry: (logEntryId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedMeal by remember(meals) { mutableStateOf(meals.firstOrNull()) }
    var selectedDay by remember { mutableStateOf(Day.MONDAY) }

    Column(modifier = modifier.padding(16.dp)) {
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
            "Log a meal",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 24.dp),
        )

        if (meals.isEmpty()) {
            Text(
                "Confirm a meal in the Meal tab first.",
                modifier = Modifier.padding(top = 8.dp),
            )
        } else {
            SimpleDropdown(
                label = "meal",
                options = meals,
                selected = selectedMeal,
                optionLabel = { it.meal.name },
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
                onClick = {
                    selectedMeal?.let { onLogMeal(it.meal.id, selectedDay) }
                },
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text("Log it")
            }
        }

        Text(
            "This week's log",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
        )

        if (logEntries.isEmpty()) {
            Text("Nothing logged yet.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(logEntries, key = { it.id }) { entry ->
                    val meal = meals.find { it.meal.id == entry.mealId }?.meal
                    Card {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text("${entry.day.label} — ${meal?.name ?: "Unknown meal"}")
                                if (meal != null) {
                                    Text("${meal.totalKcal.roundToInt()} kcal")
                                }
                            }
                            TextButton(onClick = { onDeleteLogEntry(entry.id) }) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}
