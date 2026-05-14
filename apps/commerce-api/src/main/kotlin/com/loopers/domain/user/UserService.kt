package com.loopers.domain.user

import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType
import org.springframework.stereotype.Component

@Component
class UserService(
    private val userRepository: UserRepository,
) {
    fun signup(command: SignupCommand): UserModel {
        if (userRepository.existsByLoginId(command.loginId)) {
            throw CoreException(errorType = ErrorType.CONFLICT, customMessage = "이미 사용 중인 로그인 ID 입니다.")
        }
        LoginId(command.loginId)
        Password(command.password, command.birthDate)
        Email(command.email)
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
