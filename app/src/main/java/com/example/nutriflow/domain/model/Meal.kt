package com.example.nutriflow.domain.model

import java.util.Date

data class Meal(
    val id: Long = 0,
    val userId: String,
    val name: String,
    val type: String, // "Desayuno", "Almuerzo", "Cena", "Snack"
    val proteinGrams: Double,
    val carbsGrams: Double,
    val mineralsGrams: Double,
    val calories: Double,
    val date: Date,

    // ✅ CAMPOS AÑADIDOS
    val scheduledTime: Long, // Hora exacta de la comida (milisegundos)
    val reminderTime: Long? = null // Hora exacta del recordatorio (milisegundos)
)