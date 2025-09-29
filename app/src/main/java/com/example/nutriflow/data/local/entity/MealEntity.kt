package com.example.nutriflow.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.nutriflow.domain.model.Meal
import java.util.Date

@Entity(
    tableName = "meals",
    // Agregamos Foreign Key a UserEntity
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["email"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class MealEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val name: String,
    val type: String, // "Desayuno", "Almuerzo", "Cena", "Snack"
    val proteinGrams: Double,
    val carbsGrams: Double,
    val mineralsGrams: Double,
    val calories: Double,
    val date: Date, // Asumimos un TypeConverter para Date

    // ✅ CAMPOS AÑADIDOS PARA EL HORARIO Y RECORDATORIO
    val scheduledTime: Long, // Hora exacta de la comida (milisegundos)
    val reminderTime: Long? = null // Hora exacta del recordatorio (milisegundos)
) {
    companion object {
        fun fromDomain(meal: Meal): MealEntity {
            return MealEntity(
                userId = meal.userId,
                name = meal.name,
                type = meal.type,
                proteinGrams = meal.proteinGrams,
                carbsGrams = meal.carbsGrams,
                mineralsGrams = meal.mineralsGrams,
                calories = meal.calories,
                date = meal.date,
                scheduledTime = meal.scheduledTime, // Mapeo añadido
                reminderTime = meal.reminderTime // Mapeo añadido
            )
        }
    }

    fun toDomain(): Meal {
        return Meal(
            id = this.id,
            userId = this.userId,
            name = this.name,
            type = this.type,
            proteinGrams = this.proteinGrams,
            carbsGrams = this.carbsGrams,
            mineralsGrams = this.mineralsGrams,
            calories = this.calories,
            date = this.date,
            scheduledTime = this.scheduledTime, // Mapeo añadido
            reminderTime = this.reminderTime // Mapeo añadido
        )
    }
}