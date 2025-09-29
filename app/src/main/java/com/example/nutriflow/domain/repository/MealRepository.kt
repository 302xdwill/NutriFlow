package com.example.nutriflow.domain.repository

import com.example.nutriflow.domain.model.Meal
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface MealRepository {
    suspend fun saveMeal(meal: Meal)
    fun getDailyMeals(userId: String, date: Date): Flow<List<Meal>>
    fun getAllMeals(userId: String): Flow<List<Meal>>
    suspend fun addMeal(meal: Meal)
}