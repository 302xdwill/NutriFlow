package com.example.nutriflow.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nutriflow.data.local.entity.MealEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface MealDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntity)

    /**
     * Obtiene las comidas programadas para un usuario dentro de un rango de fechas.
     * En el ViewModel/Repository, este rango será el inicio y el fin de un día específico.
     * * El ordenamiento es CRÍTICO: Las comidas se muestran en el orden en que fueron programadas (scheduledTime).
     */
    @Query("""
        SELECT * FROM meals 
        WHERE userId = :userId 
        AND date >= :startDate 
        AND date < :endDate
        ORDER BY scheduledTime ASC
    """)
    fun getDailyMeals(userId: String, startDate: Date, endDate: Date): Flow<List<MealEntity>>

    @Query("SELECT * FROM meals WHERE userId = :userId ORDER BY date DESC")
    fun getAllMeals(userId: String): Flow<List<MealEntity>>
}