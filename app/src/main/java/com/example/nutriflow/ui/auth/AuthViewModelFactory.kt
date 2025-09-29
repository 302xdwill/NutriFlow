package com.example.nutriflow.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.nutriflow.domain.repository.GoalsRepository
import com.example.nutriflow.domain.repository.UserRepository
import com.example.nutriflow.domain.usecase.LoginUserUseCase
import com.example.nutriflow.domain.usecase.RegisterUserUseCase

class AuthViewModelFactory(
    private val userRepository: UserRepository,
    private val goalsRepository: GoalsRepository,
    private val registerUserUseCase: RegisterUserUseCase,
    private val loginUserUseCase: LoginUserUseCase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(
                userRepository,
                goalsRepository,
                registerUserUseCase,
                loginUserUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}