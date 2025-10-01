package com.example.nutriflow.domain.model

data class User(
    val id: String = "",
    val name: String = "",
    val lastName: String = "",
    val email: String = "",
    // Todos los campos ahora tienen valores por defecto para evitar errores
    val age: Int = 0,
    val weight: Double = 0.0,
    val height: Double = 0.0,
    val photoUrl: String? = null,
    val calorieGoal: Double = 2000.0,
    val proteinGoal: Double = 100.0,
    val carbsGoal: Double = 250.0,
    val fatGoal: Double = 70.0
)