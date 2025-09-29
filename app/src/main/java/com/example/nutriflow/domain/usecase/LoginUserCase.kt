package com.example.nutriflow.domain.usecase

import com.example.nutriflow.domain.repository.UserRepository
import com.example.nutriflow.domain.model.User // Necesitas importar el modelo User

class LoginUserUseCase(
    private val userRepository: UserRepository
) {
    /**
     * Autentica a un usuario. Si tiene éxito, activa su sesión y devuelve sus datos.
     * @return El objeto User si el login es exitoso.
     * @throws Exception Si las credenciales son inválidas.
     */
    suspend operator fun invoke(email: String, password: String): User {
        // Llama al método del repositorio que verifica credenciales y activa la sesión.
        return userRepository.login(email, password)
    }
}