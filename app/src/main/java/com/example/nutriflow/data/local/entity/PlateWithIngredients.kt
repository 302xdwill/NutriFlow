package com.example.nutriflow.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

// Clase auxiliar para que Room pueda cargar un Plato con todos sus ingredientes.
data class PlateWithIngredients(
    @Embedded val plate: PlateEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "plateId"
    )
    val ingredients: List<PlateIngredientEntity> // Contiene IngredientId y weightGrams

    // NOTA: Para obtener los detalles completos del Ingrediente (name, calPerGram),
    // necesitaremos un Join adicional o cargarlos por separado en el Repositorio.
)