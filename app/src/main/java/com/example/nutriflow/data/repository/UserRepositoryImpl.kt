package com.example.nutriflow.data.repository

import com.example.nutriflow.data.local.dao.UserDao
import com.example.nutriflow.data.local.entity.UserEntity
import com.example.nutriflow.domain.model.User
import com.example.nutriflow.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// 🛑 ¡ATENCIÓN! Hemos eliminado todas las funciones de extensión/mapper
// para evitar los conflictos persistentes. El mapeo se hace manualmente.

class UserRepositoryImpl(
    private val userDao: UserDao
) : UserRepository {

    // --- FUNCIÓN DE REGISTRO ---
    override suspend fun register(email: String, password: String): String {
        val userEntity = UserEntity(
            email = email,
            passwordHash = password,
            name = "", lastName = "", age = 0, weight = 0.0, height = 0.0, photoUrl = null,
            calorieGoal = 0.0, proteinGoal = 0.0, carbsGoal = 0.0, fatGoal = 0.0,
            isActive = true
        )
        userDao.insertUser(userEntity)
        return email
    }

    // --- FUNCIÓN DE LOGIN (Mapeo manual) ---
    override suspend fun login(email: String, password: String): User {
        val entity = userDao.getUserByEmailAndPassword(email, password)
            ?: throw Exception("Credenciales inválidas o usuario no registrado.")

        userDao.setAllInactive()
        userDao.setActive(email)

        // MAPEO MANUAL
        return User(
            email = entity.email,
            lastName = entity.lastName,
            age = entity.age,
            weight = entity.weight,
            height = entity.height,
            photoUrl = entity.photoUrl,
            calorieGoal = entity.calorieGoal,
            proteinGoal = entity.proteinGoal,
            carbsGoal = entity.carbsGoal,
            fatGoal = entity.fatGoal
        )
    }

    // --- FUNCIÓN DE LOGOUT ---
    override suspend fun logout() {
        userDao.setAllInactive()
    }

    // --- FUNCIÓN DE OBTENER (Por Email) (Firma corregida) ---
    override fun getUser(email: String): Flow<User?> { // ⬅️ Firma corregida: 'override fun', SIN 'suspend'
        return userDao.getUser(email).map { userEntity ->
            // MAPEO MANUAL
            userEntity?.let { entity ->
                User(
                    email = entity.email,
                    lastName = entity.lastName,
                    age = entity.age,
                    weight = entity.weight,
                    height = entity.height,
                    photoUrl = entity.photoUrl,
                    calorieGoal = entity.calorieGoal,
                    proteinGoal = entity.proteinGoal,
                    carbsGoal = entity.carbsGoal,
                    fatGoal = entity.fatGoal
                )
            }
        }
    }

    // --- FUNCIÓN DE ACTUALIZAR ---
    override suspend fun updateUser(user: User) {
        val existingEntity = userDao.getExistingUserEntity(user.email)
            ?: throw IllegalStateException("No se puede actualizar, el usuario no existe.")

        // Accede a la función estática en el Companion Object de UserEntity
        val updatedEntity = UserEntity.Companion.fromDomainForUpdate(user, existingEntity)

        userDao.updateUser(updatedEntity)
    }

    // --- FUNCIÓN DE OBTENER EMAIL ACTIVO ---
    override fun getActiveUserEmail(): Flow<String?> {
        return userDao.getActiveUserEmail()
    }

    // --- FUNCIÓN DE ELIMINAR ---
    override suspend fun deleteUser(email: String) {
        userDao.deleteUser(email)
    }

    // --- FUNCIÓN DE FLUJO DE USUARIO ACTIVO ---
    override fun getCurrentUserFlow(): Flow<User?> {
        return userDao.getLoggedInUser().map { userEntity ->
            // MAPEO MANUAL
            userEntity?.let { entity ->
                User(
                    email = entity.email,
                    lastName = entity.lastName,
                    age = entity.age,
                    weight = entity.weight,
                    height = entity.height,
                    photoUrl = entity.photoUrl,
                    calorieGoal = entity.calorieGoal,
                    proteinGoal = entity.proteinGoal,
                    carbsGoal = entity.carbsGoal,
                    fatGoal = entity.fatGoal
                )
            }
        }
    }
}