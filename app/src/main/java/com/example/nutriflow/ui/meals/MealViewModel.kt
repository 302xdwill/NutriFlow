package com.example.nutriflow.ui.meals

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutriflow.data.local.database.NutriFlowDatabase
import com.example.nutriflow.data.repository.MealRepositoryImpl
import com.example.nutriflow.domain.model.Meal
import com.example.nutriflow.domain.usecase.AddMealUseCase
import kotlinx.coroutines.launch
import android.util.Log

class MealViewModel(application: Application) : AndroidViewModel(application) {

    private val mealRepository: MealRepositoryImpl
    private val addMealUseCase: AddMealUseCase

    init { // CRÍTICO: Inicialización de la base de datos y repositorios
        val database = NutriFlowDatabase.getDatabase(application)
        mealRepository = MealRepositoryImpl(database.mealDao())
        addMealUseCase = AddMealUseCase(mealRepository)
    }

    // Función modificada para incluir un callback de éxito/fallo
    fun saveMeal(meal: Meal, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                addMealUseCase(meal)
                onComplete(true) // Éxito
            } catch (e: Exception) {
                Log.e("MealViewModel", "Error al guardar plato", e)
                onComplete(false) // Fallo
            }
        }
    }
}