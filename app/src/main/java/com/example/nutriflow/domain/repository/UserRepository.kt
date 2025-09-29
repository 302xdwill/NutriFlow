package com.example.nutriflow.domain.repository

import com.example.nutriflow.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    // ✅ ELIMINADO: saveUser (Ya que register y updateUser cubren su funcionalidad)

    suspend fun register(email: String, password: String): String
    suspend fun login(email: String, password: String): User
    suspend fun logout()
    fun getCurrentUserFlow(): Flow<User?>
    fun getActiveUserEmail(): Flow<String?>
    fun getUser(email: String): Flow<User?>
    suspend fun updateUser(user: User)
    suspend fun deleteUser(email: String) // Mantener este si es necesario
}