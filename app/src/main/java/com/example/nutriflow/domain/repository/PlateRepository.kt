package com.example.nutriflow.domain.repository

import com.example.nutriflow.domain.model.Plate
import kotlinx.coroutines.flow.Flow

interface PlateRepository {

    /**
     * Guarda un plato personalizado (receta) y todos sus ingredientes asociados
     * en una única transacción.
     */
    suspend fun savePlate(plate: Plate)

    /**
     * Obtiene todos los platos personalizados creados por un usuario.
     * Retorna un Flow para actualizaciones reactivas.
     */
    fun getAllPlates(userId: String): Flow<List<Plate>>

    /**
     * Obtiene un plato específico por su ID.
     */
    suspend fun getPlateById(plateId: Int): Plate?

    /**
     * Elimina un plato y todos sus componentes asociados.
     */
    suspend fun deletePlate(plateId: Int)
}