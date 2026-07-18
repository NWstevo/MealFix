package com.example.mealfix.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.mealfix.data.FoodItem
import com.example.mealfix.data.LogEntry
import com.example.mealfix.data.Meal
import com.example.mealfix.data.MealIngredient
import com.example.mealfix.data.ScheduledMeal
import com.example.mealfix.data.WeeklyTarget

/**
 * The Room database itself — this is what actually creates/opens the SQLite file on disk
 * and hands out the DAOs. version bumps every time a column/table shape changes (most
 * recently: FoodItem switched from storing kcalPerGram directly to storing the package's
 * referenceKcal/referenceGrams instead). fallbackToDestructiveMigration() tells Room "if you
 * see an old, incompatible schema, just wipe and recreate the database" rather than requiring
 * a formal step-by-step Migration — the right call while this app is still in active
 * development and nobody's real data needs to survive an upgrade yet.
 */
@Database(
    entities = [
        FoodItem::class,
        Meal::class,
        MealIngredient::class,
        ScheduledMeal::class,
        LogEntry::class,
        WeeklyTarget::class,
    ],
    version = 3,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodItemDao(): FoodItemDao
    abstract fun mealDao(): MealDao
    abstract fun mealIngredientDao(): MealIngredientDao
    abstract fun scheduledMealDao(): ScheduledMealDao
    abstract fun logEntryDao(): LogEntryDao
    abstract fun weeklyTargetDao(): WeeklyTargetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Only one database connection should exist for the whole app. This double-checked
         * lock is the standard way to build that singleton safely even if multiple threads
         * ask for it at the same moment.
         */
        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mealfix.db",
                ).fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
