package com.example.nutriflow.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nutriflow.data.local.entity.IngredientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: IngredientEntity)

    // Obtener todos los ingredientes de un usuario
    @Query("SELECT * FROM ingredients WHERE userId = :userId ORDER BY name ASC")
    fun getIngredientsByUserId(userId: String): Flow<List<IngredientEntity>>

    // Obtener ingredientes filtrados por tipo para armar un plato
    @Query("SELECT * FROM ingredients WHERE userId = :userId AND type = :type ORDER BY name ASC")
    fun getIngredientsByType(userId: String, type: String): Flow<List<IngredientEntity>>

    // Necesario para la eliminación si el usuario quiere borrar un ingrediente
    @Query("DELETE FROM ingredients WHERE id = :ingredientId")
    suspend fun deleteIngredient(ingredientId: Int)
    @Query("SELECT * FROM ingredients WHERE id = :ingredientId")
    suspend fun getIngredientById(ingredientId: Int): IngredientEntity?
}