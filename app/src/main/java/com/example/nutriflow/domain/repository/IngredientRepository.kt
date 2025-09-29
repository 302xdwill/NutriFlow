package com.example.nutriflow.domain.repository

import com.example.nutriflow.domain.model.Ingredient
import kotlinx.coroutines.flow.Flow

interface IngredientRepository {
    suspend fun saveIngredient(ingredient: Ingredient)
    fun getAllIngredients(userId: String): Flow<List<Ingredient>>
    fun getIngredientsByType(userId: String, type: String): Flow<List<Ingredient>>
    suspend fun deleteIngredient(ingredientId: Int)
}