package com.example.mealfix.data.local

import androidx.room.TypeConverter
import com.example.mealfix.data.Day

/**
 * Room only knows how to store primitive types (String, Int, Double, etc.) directly in
 * SQLite columns. For anything else — like our Day enum — we need to teach it how to
 * convert to and from a storable type. Here, Day becomes its name ("MONDAY") as a String.
 */
class Converters {
    @TypeConverter
    fun fromDay(day: Day): String = day.name

    @TypeConverter
    fun toDay(value: String): Day = Day.valueOf(value)
}
