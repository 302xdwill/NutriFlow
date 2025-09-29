package com.example.nutriflow.domain.usecase

import com.example.nutriflow.domain.model.Meal
import com.example.nutriflow.domain.repository.MealRepository
import kotlinx.coroutines.flow.Flow
import java.util.Date

class GetDailyMealsUseCase(private val mealRepository: MealRepository) {
    operator fun invoke(userId: String, date: Date): Flow<List<Meal>> {
        return mealRepository.getDailyMeals(userId, date)
    }
}