package com.example.nutriflow.domain.usecase

import com.example.nutriflow.domain.model.Meal
import com.example.nutriflow.domain.repository.MealRepository

/**
 * Caso de uso para añadir un nuevo plato (Meal) a la base de datos.
 * Esta capa asegura que la lógica de negocio simple (solo guardar) se ejecute.
 */
class AddMealUseCase(
    private val mealRepository: MealRepository
) {
    /**
     * El operador 'invoke' permite llamar al caso de uso como una función: addMealUseCase(meal).
     */
    suspend operator fun invoke(meal: Meal) {
        mealRepository.addMeal(meal)
    }
}