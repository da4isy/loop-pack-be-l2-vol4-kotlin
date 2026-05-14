package com.loopers.domain.user

import org.springframework.stereotype.Component

@Component
class UserService(
    private val userRepository: UserRepository,
) {
    fun signup(command: SignupCommand): UserModel {
        val user = UserModel(
            loginId = command.loginId,
            password = command.password,
            name = command.name,
            birthDate = command.birthDate,
            email = command.email,
        )
        return userRepository.save(user)
    }
}
