package com.example.mealfix.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.mealfix.data.FoodItem
import com.example.mealfix.data.ProductLookupResult
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Your "food shelf" — the raw ingredients you cook with. Instead of asking you to work out
 * a calories-per-gram figure yourself, you enter the numbers straight off the package's
 * nutrition label (e.g. "375 kcal per 500 g") and the app derives the per-gram density.
 * Everything else in the app (meals, their kcal totals) is built up from this list.
 *
 * Fields can also be prefilled by scanning a product's barcode — see [onLookupBarcode] —
 * against Open Food Facts' database, which reports calories per 100g.
 */
@Composable
fun FoodShelfScreen(
    foodItems: List<FoodItem>,
    onAddFoodItem: (name: String, referenceKcal: Double, referenceGrams: Double) -> Unit,
    onDeleteFoodItem: (foodItemId: String) -> Unit,
    onLookupBarcode: suspend (barcode: String) -> ProductLookupResult?,
    modifier: Modifier = Modifier,
) {
    var nameInput by remember { mutableStateOf("") }
    var kcalInput by remember { mutableStateOf("") }
    var gramsInput by remember { mutableStateOf("") }
    var showScanner by remember { mutableStateOf(false) }
    var isLookingUp by remember { mutableStateOf(false) }
    var lookupError by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    fun startLookup(barcode: String) {
        showScanner = false
        isLookingUp = true
        lookupError = false
        coroutineScope.launch {
            val result = onLookupBarcode(barcode)
            isLookingUp = false
            if (result == null) {
                lookupError = true
            } else {
                nameInput = result.name
                kcalInput = result.kcalPer100g.roundToInt().toString()
                gramsInput = "100"
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> if (granted) showScanner = true }

    Column(modifier = modifier.padding(16.dp)) {
        Text("Add a food item", style = MaterialTheme.typography.titleMedium)
        Text(
            "Enter it as shown on the package, e.g. oats: 375 kcal for 500 g — or scan the barcode.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp),
        )

        OutlinedTextField(
            value = nameInput,
            onValueChange = { nameInput = it },
            label = { Text("Food item name") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )
        Row(modifier = Modifier.padding(top = 8.dp)) {
            OutlinedTextField(
                value = kcalInput,
                onValueChange = { kcalInput = it },
                label = { Text("Calories (kcal)") },
                placeholder = { Text("e.g. 375") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = gramsInput,
                onValueChange = { gramsInput = it },
                label = { Text("Grams") },
                placeholder = { Text("e.g. 500") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f).padding(start = 8.dp),
            )
        }
        Row(modifier = Modifier.padding(top = 8.dp)) {
            Button(
                onClick = {
                    val kcal = kcalInput.toDoubleOrNull()
                    val grams = gramsInput.toDoubleOrNull()
                    if (nameInput.isNotBlank() && kcal != null && kcal > 0 && grams != null && grams > 0) {
                        onAddFoodItem(nameInput, kcal, grams)
                        nameInput = ""
                        kcalInput = ""
                        gramsInput = ""
                    }
                },
            ) {
                Text("Add to food shelf")
            }
            OutlinedButton(
                onClick = {
                    val granted = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA,
                    ) == PackageManager.PERMISSION_GRANTED
                    if (granted) showScanner = true else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                },
                modifier = Modifier.padding(start = 8.dp),
            ) {
                Text("Scan barcode")
            }
        }

        if (isLookingUp) {
            Row(modifier = Modifier.padding(top = 8.dp)) {
                CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                Text("Looking up product…")
            }
        }
        if (lookupError) {
            Text(
                "Couldn't find that product — enter it manually.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        Text(
            "Your food shelf",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
        )

        if (foodItems.isEmpty()) {
            Text("Nothing on the shelf yet — add an item above to get started.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(foodItems, key = { it.id }) { item ->
                    Card {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column {
                                Text(item.name, style = MaterialTheme.typography.bodyLarge)
                                Text("${item.referenceKcal.roundToInt()} kcal per ${item.referenceGrams.roundToInt()} g")
                            }
                            TextButton(onClick = { onDeleteFoodItem(item.id) }) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showScanner) {
        Dialog(
            onDismissRequest = { showScanner = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            BarcodeScannerScreen(
                onBarcodeDetected = ::startLookup,
                onClose = { showScanner = false },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
