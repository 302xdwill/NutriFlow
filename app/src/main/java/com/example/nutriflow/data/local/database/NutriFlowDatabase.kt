package com.example.nutriflow.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.nutriflow.data.local.dao.GoalDao
import com.example.nutriflow.data.local.dao.MealDao
import com.example.nutriflow.data.local.dao.UserDao
import com.example.nutriflow.data.local.dao.IngredientDao
import com.example.nutriflow.data.local.dao.PlateDao
import com.example.nutriflow.data.local.entity.GoalEntity
import com.example.nutriflow.data.local.entity.MealEntity
import com.example.nutriflow.data.local.entity.UserEntity
import com.example.nutriflow.data.local.entity.IngredientEntity
import com.example.nutriflow.data.local.entity.PlateEntity
import com.example.nutriflow.data.local.entity.PlateIngredientEntity
import com.example.nutriflow.data.local.util.Converters

@Database(
    entities = [
        UserEntity::class,
        MealEntity::class,
        IngredientEntity::class,
        PlateEntity::class,
        PlateIngredientEntity::class,
        GoalEntity::class // ✅ ENTIDAD AÑADIDA
    ],
    version = 7, // ✅ VERSIÓN INCREMENTADA
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class NutriFlowDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun mealDao(): MealDao
    abstract fun ingredientDao(): IngredientDao
    abstract fun plateDao(): PlateDao
    abstract fun goalDao(): GoalDao

    companion object {
        @Volatile
        private var INSTANCE: NutriFlowDatabase? = null

        fun getDatabase(context: Context): NutriFlowDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NutriFlowDatabase::class.java,
                    "nutriflow_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}