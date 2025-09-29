package com.example.nutriflow.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.nutriflow.data.local.entity.UserEntity
import com.example.nutriflow.domain.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    // 1. Obtiene un usuario (devuelve la Entity)
    @Query("SELECT * FROM users WHERE email = :email")
    fun getUser(email: String): Flow<UserEntity?>

    // 2. Método crucial para el LOGIN: Verifica email y password.
    @Query("SELECT * FROM users WHERE email = :email AND passwordHash = :passwordHash LIMIT 1")
    suspend fun getUserByEmailAndPassword(email: String, passwordHash: String): UserEntity?

    @Query("DELETE FROM users WHERE email = :email")
    suspend fun deleteUser(email: String)

    @Update
    suspend fun updateUser(user: UserEntity)

    // 3. Obtiene el email del usuario activo para el repositorio.
    @Query("SELECT email FROM users WHERE isActive = 1 LIMIT 1")
    fun getActiveUserEmail(): Flow<String?>

    // ✅ CORRECCIÓN CLAVE: Mapeo explícito de 'email AS id'
    // Esto resuelve el error de "missing field id" al decirle a Room que use la columna 'email'
    // para llenar el campo 'id' del modelo 'User'.
    @Query("""
        SELECT 
            email AS id, email, passwordHash, name, lastName, age, weight, height, photoUrl, 
            calorieGoal, proteinGoal, carbsGoal, fatGoal, isActive 
        FROM users 
        WHERE isActive = 1 
        LIMIT 1
    """)
    fun getLoggedInUser(): Flow<User?>

    // 4. Cierra todas las sesiones abiertas (Logout)
    @Query("UPDATE users SET isActive = 0")
    suspend fun setAllInactive()

    // 5. Marca un usuario específico como activo (Login)
    @Query("UPDATE users SET isActive = 1 WHERE email = :email")
    suspend fun setActive(email: String)

    // Actualiza solo las metas (mantenemos las columnas como Doubles en el DAO para consistencia con la Entity)
    @Query("""
        UPDATE users 
        SET calorieGoal = :calorieGoal, 
            proteinGoal = :proteinGoal, 
            carbsGoal = :carbsGoal, 
            fatGoal = :fatGoal 
        WHERE email = :email
    """)
    suspend fun updateGoals(
        email: String,
        calorieGoal: Double, // Usar Double aquí si la Entity usa Double
        proteinGoal: Double,
        carbsGoal: Double,
        fatGoal: Double
    )

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getExistingUserEntity(email: String): UserEntity?
}