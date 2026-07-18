package com.example.mealfix.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.example.mealfix.data.Day
import com.example.mealfix.data.FoodItem
import com.example.mealfix.data.LogEntry
import com.example.mealfix.data.Meal
import com.example.mealfix.data.MealIngredient
import com.example.mealfix.data.ScheduledMeal
import com.example.mealfix.data.WeeklyTarget
import kotlinx.coroutines.flow.Flow

/**
 * Each DAO ("Data Access Object") is an interface describing the queries Room should
 * generate code for. `Flow<...>`-returning queries automatically re-emit whenever the
 * underlying table changes — that's what lets the UI update itself reactively without
 * us manually telling it "the data changed."
 */

@Dao
interface FoodItemDao {
    @Query("SELECT * FROM food_items")
    fun getAll(): Flow<List<FoodItem>>

    @Insert
    suspend fun insert(foodItem: FoodItem)

    @Query("DELETE FROM food_items WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface MealDao {
    @Query("SELECT * FROM meals")
    fun getAll(): Flow<List<Meal>>

    @Insert
    suspend fun insert(meal: Meal)

    @Query("DELETE FROM meals WHERE id = :mealId")
    suspend fun deleteById(mealId: String)
}

@Dao
interface MealIngredientDao {
    @Query("SELECT * FROM meal_ingredients")
    fun getAll(): Flow<List<MealIngredient>>

    @Insert
    suspend fun insertAll(ingredients: List<MealIngredient>)

    @Query("DELETE FROM meal_ingredients WHERE mealId = :mealId")
    suspend fun deleteByMealId(mealId: String)
}

@Dao
interface ScheduledMealDao {
    @Query("SELECT * FROM scheduled_meals")
    fun getAll(): Flow<List<ScheduledMeal>>

    /** Upsert keyed on `day` — assigning a new meal to a day that already has one replaces it. */
    @Upsert
    suspend fun upsert(scheduledMeal: ScheduledMeal)

    @Query("DELETE FROM scheduled_meals WHERE day = :day")
    suspend fun deleteByDay(day: Day)

    @Query("DELETE FROM scheduled_meals WHERE mealId = :mealId")
    suspend fun deleteByMealId(mealId: String)
}

@Dao
interface LogEntryDao {
    @Query("SELECT * FROM log_entries")
    fun getAll(): Flow<List<LogEntry>>

    @Insert
    suspend fun insert(logEntry: LogEntry)

    @Query("DELETE FROM log_entries WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM log_entries WHERE mealId = :mealId")
    suspend fun deleteByMealId(mealId: String)
}

@Dao
interface WeeklyTargetDao {
    @Query("SELECT * FROM weekly_target WHERE id = 0 LIMIT 1")
    fun getTarget(): Flow<WeeklyTarget?>

    @Query("SELECT * FROM weekly_target WHERE id = 0 LIMIT 1")
    suspend fun getTargetOnce(): WeeklyTarget?

    /** Upsert = insert if the row doesn't exist yet, otherwise update it. Perfect for a single-row table. */
    @Upsert
    suspend fun upsert(target: WeeklyTarget)
}
