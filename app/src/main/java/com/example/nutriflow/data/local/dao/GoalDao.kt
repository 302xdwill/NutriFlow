package com.example.nutriflow.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nutriflow.data.local.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoals(goals: List<GoalEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity)

    @Query("SELECT * FROM goals WHERE userId = :userId")
    fun getAllGoalsByUserId(userId: String): Flow<List<GoalEntity>>

    // Si necesitas obtener una meta específica por tipo (ej: solo calorías)
    @Query("SELECT * FROM goals WHERE userId = :userId AND type = :type LIMIT 1")
    suspend fun getGoalByType(userId: String, type: String): GoalEntity?
}