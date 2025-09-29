package com.example.nutriflow.domain.usecase

import com.example.nutriflow.domain.model.User
import com.example.nutriflow.domain.repository.UserRepository

class UpdateUserUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(user: User) {
        userRepository.updateUser(user)
    }
}