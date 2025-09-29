package com.example.nutriflow.domain.model

// Representa un ingrediente específico dentro de un plato, con su peso.
data class PlateIngredient(
    val id: Int = 0,
    val plateId: Int,
    val ingredient: Ingredient, // Referencia al objeto Ingredient que contiene calPerGram
    val weightGrams: Double
)