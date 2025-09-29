package com.example.nutriflow.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.nutriflow.data.local.entity.PlateEntity
import com.example.nutriflow.data.local.entity.PlateIngredientEntity
import com.example.nutriflow.data.local.entity.PlateWithIngredients
import kotlinx.coroutines.flow.Flow

@Dao
interface PlateDao {

    // --- OPERACIONES DE INSERCIÓN ---

    /**
     * Inserta un nuevo plato o lo reemplaza si hay conflicto.
     * Devuelve el ID generado del plato para usarlo en PlateIngredientEntity.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlate(plate: PlateEntity): Long

    /**
     * Inserta la lista de ingredientes que componen un plato.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlateIngredients(ingredients: List<PlateIngredientEntity>)

    // --- OPERACIONES DE CONSULTA ---

    /**
     * Obtiene todos los platos creados por un usuario.
     * Se usa @Transaction y PlateWithIngredients para cargar el plato junto con su lista de ingredientes.
     */
    @Transaction
    @Query("SELECT * FROM plates WHERE userId = :userId ORDER BY name ASC")
    fun getPlatesByUserId(userId: String): Flow<List<PlateWithIngredients>>

    /**
     * Obtiene un plato específico por su ID.
     */
    @Transaction
    @Query("SELECT * FROM plates WHERE id = :plateId")
    suspend fun getPlateWithIngredients(plateId: Int): PlateWithIngredients?

    // --- OPERACIONES DE ELIMINACIÓN ---

    /**
     * Elimina un plato por su ID.
     * NOTA: Gracias a la regla ForeignKey.CASCADE definida en la entidad,
     * todos los PlateIngredientEntity asociados a este plato también serán eliminados automáticamente.
     */
    @Query("DELETE FROM plates WHERE id = :plateId")
    suspend fun deletePlate(plateId: Int)
}