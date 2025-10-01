package com.example.nutriflow.ui.meals

import com.example.nutriflow.domain.model.Ingredient
import com.example.nutriflow.domain.model.PlateIngredient

// Estado que representa la receta que el usuario está construyendo
data class PlateCreatorState(
    val plateName: String = "",
    val plateDescription: String = "",
    val availableIngredients: List<Ingredient> = emptyList(),
    val components: List<PlateIngredient> = emptyList(),

    // Campos para el formulario de adición
    val ingredientSearchQuery: String = "",
    val filteredIngredients: List<Ingredient> = emptyList(),
    val ingredientAmount: String = "",

    // **✅ CAMPO DE ESTADO AÑADIDO/CORREGIDO**
    val selectedIngredientForAdding: Ingredient? = null,

    // Totales
    val totalCalories: Double = 0.0,
    val totalProtein: Double = 0.0,
    val totalCarbs: Double = 0.0,
    val totalFat: Double = 0.0,

    // UI y lógica
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)