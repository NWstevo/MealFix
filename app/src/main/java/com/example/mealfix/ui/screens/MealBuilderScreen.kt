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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlin.math.roundToInt
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.mealfix.data.DraftIngredient
import com.example.mealfix.data.FoodItem

/**
 * Where a meal actually gets built: pick food items from the shelf, say how many grams of
 * each go into the meal, and watch the kcal total add up line by line. Nothing is saved
 * until "Confirm meal" — up to that point, the ingredient list only lives in this screen's
 * own state.
 */
@Composable
fun MealBuilderScreen(
    foodItems: List<FoodItem>,
    onConfirmMeal: (name: String, ingredients: List<DraftIngredient>) -> Unit,
    modifier: Modifier = Modifier,
) {
    var mealName by remember { mutableStateOf("") }
    var selectedFoodItem by remember(foodItems) { mutableStateOf(foodItems.firstOrNull()) }
    var gramsInput by remember { mutableStateOf("") }
    val draftIngredients = remember { mutableStateListOf<DraftIngredient>() }

    val totalKcal = draftIngredients.sumOf { it.kcal }

    Column(modifier = modifier.padding(16.dp)) {
        Text("Build a meal", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = mealName,
            onValueChange = { mealName = it },
            label = { Text("Meal name") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )

        if (foodItems.isEmpty()) {
            Text(
                "Add items to your food shelf first — you need at least one before you can build a meal.",
                modifier = Modifier.padding(top = 16.dp),
            )
        } else {
            Text(
                "Add ingredients",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 24.dp),
            )
            SimpleDropdown(
                label = "food item",
                options = foodItems,
                selected = selectedFoodItem,
                optionLabel = { it.name },
                onSelect = { selectedFoodItem = it },
                modifier = Modifier.padding(top = 8.dp),
            )
            OutlinedTextField(
                value = gramsInput,
                onValueChange = { gramsInput = it },
                label = { Text("Grams used (g)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
            Button(
                onClick = {
                    val grams = gramsInput.toDoubleOrNull()
                    val item = selectedFoodItem
                    if (item != null && grams != null && grams > 0) {
                        draftIngredients.add(
                            DraftIngredient(
                                foodItemId = item.id,
                                foodItemName = item.name,
                                gramsUsed = grams,
                                kcal = grams * item.kcalPerGram,
                            ),
                        )
                        gramsInput = ""
                    }
                },
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text("Add ingredient")
            }
        }

        if (draftIngredients.isNotEmpty()) {
            Text(
                "This meal so far",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
            )
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(draftIngredients.toList()) { ingredient ->
                    Card {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(ingredient.foodItemName)
                                Text("${ingredient.gramsUsed.roundToInt()} g — ${ingredient.kcal.roundToInt()} kcal")
                            }
                            TextButton(onClick = { draftIngredients.remove(ingredient) }) {
                                Text("Remove")
                            }
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text(
                "Total: ${totalKcal.roundToInt()} kcal",
                style = MaterialTheme.typography.titleMedium,
            )

            Button(
                onClick = {
                    onConfirmMeal(mealName, draftIngredients.toList())
                    mealName = ""
                    draftIngredients.clear()
                },
                modifier = Modifier.padding(top = 16.dp),
            ) {
                Text("Confirm meal")
            }
        }
    }
}
