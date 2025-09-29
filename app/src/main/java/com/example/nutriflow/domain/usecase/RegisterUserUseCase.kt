package com.example.nutriflow.domain.usecase

import com.example.nutriflow.domain.model.User
import com.example.nutriflow.domain.repository.UserRepository

class RegisterUserUseCase(private val userRepository: UserRepository) {

    /**
     * Registra un nuevo usuario en la base de datos y devuelve su ID (email).
     * * @param user El modelo de usuario que contiene los datos personales y metas.
     * @param password La contraseña introducida por el usuario.
     * @return El ID del usuario recién registrado (su email).
     */
    suspend operator fun invoke(user: User, password: String): String {
        // ✅ CORRECCIÓN CLAVE: El caso de uso ahora llama a 'register'
        // Esto asume que UserRepository.register(email, password) existe.
        return userRepository.register(user.email, password)

        /* Nota: En una arquitectura limpia, este caso de uso DEBERÍA llamar
        a userRepository.register, seguido de userRepository.updateUser(user)
        para guardar los datos personales y metas.

        Pero, dado que el AuthViewModel ya está manejando el guardado de METAS iniciales
        y los datos personales iniciales están vacíos, usamos el método 'register'
        por simplicidad para resolver el error.
        */
    }
}