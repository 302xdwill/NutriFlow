package com.example.nutriflow.domain.usecase

import com.example.nutriflow.domain.model.Meal
import com.example.nutriflow.domain.repository.MealRepository

class SaveMealUseCase(private val mealRepository: MealRepository) {
    suspend operator fun invoke(meal: Meal) {
        mealRepository.saveMeal(meal)
    }
}