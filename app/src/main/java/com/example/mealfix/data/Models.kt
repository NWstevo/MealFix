package com.example.mealfix.data

import java.util.UUID

/**
 * The days a meal can be planned for. Kept as our own enum (rather than java.time.DayOfWeek)
 * so the UI can control ordering and display easily.
 */
enum class Day(val label: String) {
    MONDAY("Mon"),
    TUESDAY("Tue"),
    WEDNESDAY("Wed"),
    THURSDAY("Thu"),
    FRIDAY("Fri"),
    SATURDAY("Sat"),
    SUNDAY("Sun"),
}

/**
 * A meal you know how to prepare, described by its calorie density.
 * kcalPerKg is the key number: "this meal delivers X kcal for every kilogram of it".
 * That's what lets us work backwards from a calorie target to a kg amount.
 */
data class Meal(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val kcalPerKg: Double,
)

/**
 * The overall calorie goal for the week. Kept as its own small model so it's easy
 * to extend later (e.g. per-day targets, macro targets) without reshaping everything else.
 */
data class WeeklyTarget(
    val kcalPerWeek: Double,
)

/**
 * One meal assigned to one day of the week, with a calorie allocation.
 * targetKcal is how many of the week's calories this specific meal is responsible for.
 */
data class PlannedMeal(
    val id: String = UUID.randomUUID().toString(),
    val mealId: String,
    val day: Day,
    val targetKcal: Double,
) {
    /** How many kg of [meal] you need to prepare/eat to hit this planned meal's calorie target. */
    fun kgNeeded(meal: Meal): Double =
        if (meal.kcalPerKg <= 0.0) 0.0 else targetKcal / meal.kcalPerKg
}

/**
 * An actual, logged instance of eating something — this is the "tracker" half of the app.
 * quantityKg is what you actually ate, which may differ from what was planned.
 */
data class LogEntry(
    val id: String = UUID.randomUUID().toString(),
    val mealId: String,
    val day: Day,
    val quantityKg: Double,
) {
    fun kcalConsumed(meal: Meal): Double = quantityKg * meal.kcalPerKg
}
