package com.example.nutriflow.domain.model

data class Ingredient(
    val id: Int = 0, // ID de Room
    val name: String,
    // Tipo: Proteina, Carbohidrato, Mineral, Grasa. Usaremos enum para seguridad.
    val type: String,
    // Calorías por gramo (para cálculo: 1g * calPerGram)
    val caloriesPerGram: Double,
    val userId: String // Clave foránea para el usuario que lo creó
)