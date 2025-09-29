package com.example.nutriflow.domain.usecase

import com.example.nutriflow.domain.model.User
import com.example.nutriflow.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

class GetUserUseCase(
    private val userRepository: UserRepository
) {
    /**
     * Obtiene los datos del usuario especificado por su email.
     */
    operator fun invoke(email: String): Flow<User?> {
        return userRepository.getUser(email)
    }
}