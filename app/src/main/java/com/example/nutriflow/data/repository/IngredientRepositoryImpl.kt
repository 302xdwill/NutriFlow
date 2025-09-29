package com.example.nutriflow.data.repository

import com.example.nutriflow.data.local.dao.IngredientDao
import com.example.nutriflow.data.local.entity.IngredientEntity
import com.example.nutriflow.domain.model.Ingredient
import com.example.nutriflow.domain.repository.IngredientRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class IngredientRepositoryImpl(
    private val ingredientDao: IngredientDao
) : IngredientRepository {

    // Helper para mapear Entity a Model
    private fun IngredientEntity.toDomainModel() = Ingredient(
        id = id,
        name = name,
        type = type,
        caloriesPerGram = caloriesPerGram,
        userId = userId
    )

    // Helper para mapear Model a Entity
    private fun Ingredient.toEntity() = IngredientEntity(
        id = id,
        name = name,
        type = type,
        caloriesPerGram = caloriesPerGram,
        userId = userId
    )

    override suspend fun saveIngredient(ingredient: Ingredient) {
        ingredientDao.insertIngredient(ingredient.toEntity())
    }

    override fun getAllIngredients(userId: String): Flow<List<Ingredient>> {
        return ingredientDao.getIngredientsByUserId(userId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getIngredientsByType(userId: String, type: String): Flow<List<Ingredient>> {
        return ingredientDao.getIngredientsByType(userId, type).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun deleteIngredient(ingredientId: Int) {
        ingredientDao.deleteIngredient(ingredientId)
    }
}