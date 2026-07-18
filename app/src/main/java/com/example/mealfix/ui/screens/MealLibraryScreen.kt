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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.mealfix.data.Meal

/**
 * Lets the user build up their meal "library" — each meal is just a name plus its calorie
 * density (kcal per kg). Everything else in the app (kg-needed calculations, tracking)
 * pulls from this list.
 */
@Composable
fun MealLibraryScreen(
    meals: List<Meal>,
    onAddMeal: (name: String, kcalPerKg: Double) -> Unit,
    onDeleteMeal: (mealId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var nameInput by remember { mutableStateOf("") }
    var kcalInput by remember { mutableStateOf("") }

    Column(modifier = modifier.padding(16.dp)) {
        Text("Add a meal", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = nameInput,
            onValueChange = { nameInput = it },
            label = { Text("Meal name") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )
        OutlinedTextField(
            value = kcalInput,
            onValueChange = { kcalInput = it },
            label = { Text("Calories per kg (kcal/kg)") },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )
        Button(
            onClick = {
                val kcal = kcalInput.toDoubleOrNull()
                if (nameInput.isNotBlank() && kcal != null && kcal > 0) {
                    onAddMeal(nameInput, kcal)
                    nameInput = ""
                    kcalInput = ""
                }
            },
            modifier = Modifier.padding(top = 8.dp),
        ) {
            Text("Add meal")
        }

        Text(
            "Your meals",
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
        )

        if (meals.isEmpty()) {
            Text("No meals yet — add one above to get started.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(meals, key = { it.id }) { meal ->
                    Card {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column {
                                Text(meal.name, style = androidx.compose.material3.MaterialTheme.typography.bodyLarge)
                                Text("${meal.kcalPerKg} kcal/kg")
                            }
                            TextButton(onClick = { onDeleteMeal(meal.id) }) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}
