package com.example.mealfix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mealfix.data.OpenFoodFactsApi
import com.example.mealfix.ui.screens.FoodShelfScreen
import com.example.mealfix.ui.screens.MealBuilderScreen
import com.example.mealfix.ui.screens.MenuScreen
import com.example.mealfix.ui.screens.TargetScreen
import com.example.mealfix.ui.screens.TrackerScreen
import com.example.mealfix.ui.theme.MealFixTheme
import com.example.mealfix.ui.theme.ThemePreferences
import com.example.mealfix.viewmodel.MealPlannerViewModel
import com.example.mealfix.viewmodel.MealPlannerViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val systemDefault = isSystemInDarkTheme()
            val context = LocalContext.current
            var isDarkTheme by remember {
                mutableStateOf(ThemePreferences.isDarkMode(context, systemDefault))
            }
            MealFixTheme(darkTheme = isDarkTheme) {
                MealFixApp(
                    isDarkTheme = isDarkTheme,
                    onToggleDarkTheme = { isDark ->
                        isDarkTheme = isDark
                        ThemePreferences.setDarkMode(context, isDark)
                    },
                )
            }
        }
    }
}

private enum class AppScreen(val label: String) {
    FOOD_SHELF("Shelf"),
    MEAL("Meal"),
    MENU("Menu"),
    TARGET("Target"),
    TRACKER("Tracker"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MealFixApp(
    isDarkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
) {
    val application = LocalContext.current.applicationContext as MealFixApplication
    val viewModel: MealPlannerViewModel = viewModel(
        factory = MealPlannerViewModelFactory(application.repository),
    )
    val uiState by viewModel.uiState.collectAsState()
    var currentScreen by remember { mutableStateOf(AppScreen.FOOD_SHELF) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("MealFix") },
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (isDarkTheme) "Dark" else "Light")
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = onToggleDarkTheme,
                            modifier = Modifier.padding(start = 4.dp, end = 12.dp),
                        )
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                AppScreen.entries.forEach { screen ->
                    NavigationBarItem(
                        selected = currentScreen == screen,
                        onClick = { currentScreen = screen },
                        icon = {},
                        label = { Text(screen.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)
        when (currentScreen) {
            AppScreen.FOOD_SHELF -> FoodShelfScreen(
                foodItems = uiState.foodItems,
                onAddFoodItem = viewModel::addFoodItem,
                onDeleteFoodItem = viewModel::deleteFoodItem,
                onLookupBarcode = OpenFoodFactsApi::lookup,
                modifier = contentModifier,
            )
            AppScreen.MEAL -> MealBuilderScreen(
                foodItems = uiState.foodItems,
                onConfirmMeal = viewModel::confirmMeal,
                modifier = contentModifier,
            )
            AppScreen.MENU -> MenuScreen(
                meals = uiState.meals,
                onDeleteMeal = viewModel::deleteMeal,
                modifier = contentModifier,
            )
            AppScreen.TARGET -> TargetScreen(
                meals = uiState.meals,
                scheduledMeals = uiState.scheduledMeals,
                dailyTargetKcal = uiState.weeklyTarget.dailyTargetKcal,
                weeklyTargetKcal = uiState.weeklyTarget.kcalPerWeek,
                totalScheduledKcal = uiState.totalScheduledKcal,
                scheduledProgressFraction = uiState.scheduledProgressFraction,
                onSetDailyTarget = viewModel::setDailyTarget,
                onScheduleMeal = viewModel::scheduleMeal,
                onUnscheduleDay = viewModel::unscheduleDay,
                modifier = contentModifier,
            )
            AppScreen.TRACKER -> TrackerScreen(
                scheduledMeals = uiState.scheduledMeals,
                mealById = uiState::mealById,
                logEntries = uiState.logEntries,
                weeklyTargetKcal = uiState.weeklyTarget.kcalPerWeek,
                totalKcalConsumed = uiState.totalKcalConsumed,
                progressFraction = uiState.progressFraction,
                remainingKcal = uiState.remainingKcal,
                onSetDayFollowed = viewModel::setDayFollowed,
                modifier = contentModifier,
            )
        }
    }
}
