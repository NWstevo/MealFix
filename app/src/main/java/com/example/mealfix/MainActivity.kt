package com.example.mealfix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mealfix.ui.screens.MealLibraryScreen
import com.example.mealfix.ui.screens.TrackerScreen
import com.example.mealfix.ui.screens.WeeklyPlanScreen
import com.example.mealfix.ui.theme.MealFixTheme
import com.example.mealfix.viewmodel.MealPlannerViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MealFixTheme {
                MealFixApp()
            }
        }
    }
}

private enum class AppScreen(val label: String) {
    LIBRARY("Library"),
    PLAN("Plan"),
    TRACKER("Tracker"),
}

@Composable
private fun MealFixApp(viewModel: MealPlannerViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var currentScreen by remember { mutableStateOf(AppScreen.LIBRARY) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
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
            AppScreen.LIBRARY -> MealLibraryScreen(
                meals = uiState.meals,
                onAddMeal = viewModel::addMeal,
                onDeleteMeal = viewModel::deleteMeal,
                modifier = contentModifier,
            )
            AppScreen.PLAN -> WeeklyPlanScreen(
                meals = uiState.meals,
                plannedMeals = uiState.plannedMeals,
                weeklyTargetKcal = uiState.weeklyTarget.kcalPerWeek,
                onSetWeeklyTarget = viewModel::setWeeklyTarget,
                onAddPlannedMeal = viewModel::addPlannedMeal,
                onRemovePlannedMeal = viewModel::removePlannedMeal,
                modifier = contentModifier,
            )
            AppScreen.TRACKER -> TrackerScreen(
                meals = uiState.meals,
                logEntries = uiState.logEntries,
                weeklyTargetKcal = uiState.weeklyTarget.kcalPerWeek,
                totalKcalConsumed = uiState.totalKcalConsumed,
                progressFraction = uiState.progressFraction,
                onLogMeal = viewModel::logMeal,
                onDeleteLogEntry = viewModel::deleteLogEntry,
                modifier = contentModifier,
            )
        }
    }
}
