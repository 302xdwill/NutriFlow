package com.example.nutriflow.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "plate_ingredients",
    // PlateId se relaciona con PlateEntity
    // IngredientId se relaciona con IngredientEntity
    foreignKeys = [
        ForeignKey(
            entity = PlateEntity::class,
            parentColumns = ["id"],
            childColumns = ["plateId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = IngredientEntity::class,
            parentColumns = ["id"],
            childColumns = ["ingredientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["plateId"]),
        Index(value = ["ingredientId"])
    ]
)
data class PlateIngredientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val plateId: Int, // ID del plato al que pertenece
    val ingredientId: Int, // ID del ingrediente base
    val weightGrams: Double // Cantidad usada en este plato
)