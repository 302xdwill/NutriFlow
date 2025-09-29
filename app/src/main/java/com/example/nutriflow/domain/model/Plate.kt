package com.example.nutriflow.domain.model

// Un plato es esencialmente una plantilla de comida personalizada
data class Plate(
    val id: Int = 0,
    val name: String,
    val description: String? = null,
    val userId: String,
    // La lista real de ingredientes con su peso, se cargará por separado
    val components: List<PlateIngredient> = emptyList(),

    // Resumen nutricional (se calcula y se guarda para facilitar la lectura)
    val totalCalories: Double = 0.0,
    val totalProtein: Double = 0.0,
    val totalCarbs: Double = 0.0,
    val totalFat: Double = 0.0 // Usamos Fat aquí, ya que Minerals es solo para el registro diario.
)