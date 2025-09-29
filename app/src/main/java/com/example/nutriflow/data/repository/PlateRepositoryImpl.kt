package com.example.nutriflow.data.repository

import androidx.room.withTransaction
import com.example.nutriflow.data.local.dao.PlateDao
import com.example.nutriflow.data.local.dao.IngredientDao // Necesario para obtener detalles del ingrediente
import com.example.nutriflow.data.local.database.NutriFlowDatabase // Necesario para transacciones
import com.example.nutriflow.data.local.entity.IngredientEntity
import com.example.nutriflow.data.local.entity.PlateEntity
import com.example.nutriflow.data.local.entity.PlateIngredientEntity
import com.example.nutriflow.data.local.entity.PlateWithIngredients
import com.example.nutriflow.domain.model.Ingredient
import com.example.nutriflow.domain.model.Plate
import com.example.nutriflow.domain.model.PlateIngredient
import com.example.nutriflow.domain.repository.PlateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform

class PlateRepositoryImpl(
    private val plateDao: PlateDao,
    private val ingredientDao: IngredientDao,
    private val database: NutriFlowDatabase // Se inyecta la DB para la función withTransaction
) : PlateRepository {

    // --- Mapeadores de Entidad a Dominio ---

    /**
     * Convierte PlateIngredientEntity a PlateIngredient, cargando el objeto Ingredient asociado.
     */
    private suspend fun PlateIngredientEntity.toDomainModel(): PlateIngredient? {
        // Necesitamos cargar el Ingrediente real (name, calPerGram, etc.)
        val ingredientEntity = ingredientDao.getIngredientById(this.ingredientId)

        return ingredientEntity?.let {
            PlateIngredient(
                id = this.id,
                plateId = this.plateId,
                ingredient = Ingredient(
                    id = it.id,
                    name = it.name,
                    type = it.type,
                    caloriesPerGram = it.caloriesPerGram,
                    userId = it.userId
                ),
                weightGrams = this.weightGrams
            )
        }
    }

    /**
     * Convierte PlateWithIngredients a Plate (Modelo de Dominio).
     */
    private suspend fun PlateWithIngredients.toDomainModel(): Plate {
        // Mapea y filtra los componentes si el ingrediente asociado no existe (debería existir)
        val components = this.ingredients.mapNotNull { it.toDomainModel() }

        return Plate(
            id = this.plate.id,
            name = this.plate.name,
            description = this.plate.description,
            userId = this.plate.userId,
            components = components,
            totalCalories = this.plate.totalCalories,
            totalProtein = this.plate.totalProtein,
            totalCarbs = this.plate.totalCarbs,
            totalFat = this.plate.totalFat
        )
    }

    // --- IMPLEMENTACIÓN DE REPOSITORIO ---

    override suspend fun savePlate(plate: Plate) {
        // El guardado debe ser una transacción atómica para asegurar que ambas tablas se actualicen.
        database.withTransaction {
            // 1. Convertir y guardar la entidad principal del plato
            val plateEntity = PlateEntity(
                id = plate.id, // Si es 0, Room lo autogenera. Si se actualiza, se usa el ID existente.
                name = plate.name,
                description = plate.description,
                userId = plate.userId,
                totalCalories = plate.totalCalories,
                totalProtein = plate.totalProtein,
                totalCarbs = plate.totalCarbs,
                totalFat = plate.totalFat
            )
            val plateId = plateDao.insertPlate(plateEntity).toInt()

            // 2. Convertir y guardar los componentes (PlateIngredientEntity)
            val plateIngredientEntities = plate.components.map { component ->
                PlateIngredientEntity(
                    id = component.id, // Permitir actualización o nueva inserción
                    plateId = plateId, // Usamos el ID recién generado/existente
                    ingredientId = component.ingredient.id, // ID del ingrediente base
                    weightGrams = component.weightGrams
                )
            }
            plateDao.insertPlateIngredients(plateIngredientEntities)
        }
    }

    override fun getAllPlates(userId: String): Flow<List<Plate>> {
        // Usamos transform para convertir el Flow<List<PlateWithIngredients>>
        // a Flow<List<Plate>> de forma síncrona/reactiva, asegurando la carga de ingredientes.
        return plateDao.getPlatesByUserId(userId).transform { listWithEntities ->
            val domainList = listWithEntities.map { it.toDomainModel() }
            emit(domainList)
        }
    }

    override suspend fun getPlateById(plateId: Int): Plate? {
        val plateWithIngredients = plateDao.getPlateWithIngredients(plateId)
        return plateWithIngredients?.toDomainModel()
    }

    override suspend fun deletePlate(plateId: Int) {
        plateDao.deletePlate(plateId)
    }
}