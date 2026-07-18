package com.example.mealfix.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mealfix.data.MealWithIngredients
import kotlin.math.roundToInt

/**
 * The catalog of confirmed meals — every meal you've built and saved from the Meal tab,
 * each showing its fixed total kcal and the ingredients (with grams) that make it up.
 * This is what the Target and Tracker tabs pick from.
 */
@Composable
fun MenuScreen(
    meals: List<MealWithIngredients>,
    onDeleteMeal: (mealId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(16.dp)) {
        Text("Your menu", style = MaterialTheme.typography.titleMedium)

        if (meals.isEmpty()) {
            Text(
                "No meals confirmed yet — build one in the Meal tab first.",
                modifier = Modifier.padding(top = 8.dp),
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 8.dp),
            ) {
                items(meals, key = { it.meal.id }) { mealWithIngredients ->
                    val meal = mealWithIngredients.meal
                    Card {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(meal.name, style = MaterialTheme.typography.bodyLarge)
                                TextButton(onClick = { onDeleteMeal(meal.id) }) {
                                    Text("Delete")
                                }
                            }
                            Text(
                                "${meal.totalKcal.roundToInt()} kcal total",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            mealWithIngredients.ingredients.forEach { ingredient ->
                                Text(
                                    "  • ${ingredient.foodItemName} — ${ingredient.gramsUsed.roundToInt()} g (${ingredient.kcal.roundToInt()} kcal)",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
