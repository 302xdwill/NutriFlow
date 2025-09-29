package com.example.nutriflow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.nutriflow.domain.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = false)
    val email: String,

    val passwordHash: String,

    val name: String,
    val lastName: String,
    val age: Int,
    val weight: Double,
    val height: Double,
    val photoUrl: String?,

    val calorieGoal: Double,
    val proteinGoal: Double,
    val carbsGoal: Double,
    val fatGoal: Double,

    val isActive: Boolean = false
) {
    // ✅ CORRECCIÓN CLAVE: Mapear 'this.email' al campo 'id' del modelo User.
    fun toDomain(): User {
        return User(
            id = this.email, // <--- EL ID DE DOMINIO AHORA ES EL EMAIL DE LA ENTITY
            name = this.name,
            lastName = this.lastName,
            email = this.email,
            photoUrl = this.photoUrl,
            age = this.age,
            weight = this.weight,
            height = this.height,
            calorieGoal = this.calorieGoal,
            proteinGoal = this.proteinGoal,
            carbsGoal = this.carbsGoal,
            fatGoal = this.fatGoal
        )
    }

    companion object {

        // Función para el REGISTRO (cuando se crea por primera vez)
        fun fromDomain(user: User, passwordHash: String): UserEntity {
            return UserEntity(
                email = user.email,
                passwordHash = passwordHash,
                name = user.name,
                lastName = user.lastName,
                age = user.age,
                weight = user.weight,
                height = user.height,
                photoUrl = user.photoUrl,
                calorieGoal = user.calorieGoal,
                proteinGoal = user.proteinGoal,
                carbsGoal = user.carbsGoal,
                fatGoal = user.fatGoal,
                isActive = true
            )
        }

        // Función para UPDATE (mantiene hash y estado activo de una entidad existente)
        fun fromDomainForUpdate(user: User, existingEntity: UserEntity): UserEntity {
            return UserEntity(
                email = user.email,
                passwordHash = existingEntity.passwordHash,
                name = user.name,
                lastName = user.lastName,
                age = user.age,
                weight = user.weight,
                height = user.height,
                photoUrl = user.photoUrl,
                calorieGoal = user.calorieGoal,
                proteinGoal = user.proteinGoal,
                carbsGoal = user.carbsGoal,
                fatGoal = user.fatGoal,
                isActive = existingEntity.isActive
            )
        }
    }
}