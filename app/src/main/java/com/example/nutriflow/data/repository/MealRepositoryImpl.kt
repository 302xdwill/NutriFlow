package com.example.nutriflow.data.repository

import com.example.nutriflow.data.local.dao.MealDao
import com.example.nutriflow.data.local.entity.MealEntity
import com.example.nutriflow.domain.model.Meal
import com.example.nutriflow.domain.repository.MealRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.Date

class MealRepositoryImpl(
    private val mealDao: MealDao
) : MealRepository {

    // Define una clase simple para agrupar el inicio y el fin del día
    data class DayRange(val startDate: Date, val endDate: Date)

    /**
     * Función auxiliar para calcular el rango de 24 horas para un día dado,
     * asegurando que la búsqueda en la base de datos sea precisa.
     */
    private fun Date.toDayRange(): DayRange {
        val calendar = Calendar.getInstance()

        // 1. Establecer el inicio del día (00:00:00)
        calendar.time = this
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.time

        // 2. Establecer el fin del día (inicio del día siguiente)
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val endDate = calendar.time

        return DayRange(startDate, endDate)
    }

    override suspend fun saveMeal(meal: Meal) {
        // Usa fromDomain para asegurar que todos los campos (incluidos los de tiempo) se mapeen.
        mealDao.insertMeal(MealEntity.fromDomain(meal))
    }

    override fun getDailyMeals(userId: String, date: Date): Flow<List<Meal>> {
        // ✅ CORRECCIÓN: Llamamos a la función auxiliar para obtener startDate y endDate.
        val range = date.toDayRange()

        return mealDao.getDailyMeals(
            userId = userId,
            startDate = range.startDate, // Primer parámetro
            endDate = range.endDate      // Segundo parámetro requerido por el DAO
        ).map { entities ->
            entities.map { entity ->
                Meal(
                    id = entity.id,
                    userId = entity.userId,
                    name = entity.name,
                    type = entity.type,
                    proteinGrams = entity.proteinGrams,
                    carbsGrams = entity.carbsGrams,
                    mineralsGrams = entity.mineralsGrams,
                    calories = entity.calories,
                    date = entity.date,
                    scheduledTime = entity.scheduledTime,
                    reminderTime = entity.reminderTime
                )
            }
        }
    }

    override fun getAllMeals(userId: String): Flow<List<Meal>> {
        return mealDao.getAllMeals(userId).map { entities ->
            entities.map { entity ->
                Meal(
                    id = entity.id,
                    userId = entity.userId,
                    name = entity.name,
                    type = entity.type,
                    proteinGrams = entity.proteinGrams,
                    carbsGrams = entity.carbsGrams,
                    mineralsGrams = entity.mineralsGrams,
                    calories = entity.calories,
                    date = entity.date,
                    scheduledTime = entity.scheduledTime,
                    reminderTime = entity.reminderTime
                )
            }
        }
    }

    override suspend fun addMeal(meal: Meal) {
        mealDao.insertMeal(MealEntity.fromDomain(meal))
    }
}