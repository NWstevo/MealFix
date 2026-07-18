package com.example.mealfix

import android.app.Application
import com.example.mealfix.data.MealPlannerRepository
import com.example.mealfix.data.local.AppDatabase

/**
 * The Application class is created once, before any Activity, and lives for as long as the
 * app process does. It's the natural place to build long-lived, app-wide objects like the
 * database and repository once, rather than recreating them every time a screen is shown.
 * `by lazy` means these aren't actually built until the first time something asks for them.
 */
class MealFixApplication : Application() {
    private val database by lazy { AppDatabase.getInstance(this) }

    val repository by lazy { MealPlannerRepository(database) }
}
