package com.example.nutriflow.domain.model

data class Goal(
    val id: Long = 0,
    val userId: String, // Usamos String si el ID/Email del usuario es String
    val type: String,   // Ejemplo: "Calorías", "Proteína"
    val value: Double   // El valor de la meta
)