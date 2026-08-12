package com.example.mealfix.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mealfix.data.Day
import com.example.mealfix.data.LogEntry
import com.example.mealfix.data.Meal
import com.example.mealfix.data.ScheduledMeal
import kotlin.math.roundToInt

/**
 * The diet-tracking half of the app: purely reflects what the Target tab already scheduled
 * for each day — no independent meal picker here. For each day that has a scheduled meal,
 * a checkbox confirms "I followed the plan that day." Only ticked days count toward the
 * weekly total, which is matched against the target at the bottom.
 */
@Composable
fun TrackerScreen(
    scheduledMeals: List<ScheduledMeal>,
    mealById: (String) -> Meal?,
    logEntries: List<LogEntry>,
    weeklyTargetKcal: Double,
    totalKcalConsumed: Double,
    progressFraction: Float,
    remainingKcal: Double,
    onSetDayFollowed: (day: Day, followed: Boolean) -> Unit,
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
            "This week's plan",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
        )

        if (scheduledMeals.isEmpty()) {
            Text("Schedule some meals in the Target tab first.")
        } else {
            Day.entries.forEach { day ->
                val scheduledMealId = scheduledMeals.find { it.day == day }?.mealId
                val scheduledMeal = scheduledMealId?.let(mealById)
                val followed = logEntries.any { it.day == day }

                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(day.label, style = MaterialTheme.typography.bodyLarge)
                        if (scheduledMeal == null) {
                            Text(
                                "No meal scheduled",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column {
                                    Text(scheduledMeal.name)
                                    Text(
                                        "${scheduledMeal.totalKcal.roundToInt()} kcal",
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Followed")
                                    Checkbox(
                                        checked = followed,
                                        onCheckedChange = { onSetDayFollowed(day, it) },
                                    )
                                }
                            }
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
