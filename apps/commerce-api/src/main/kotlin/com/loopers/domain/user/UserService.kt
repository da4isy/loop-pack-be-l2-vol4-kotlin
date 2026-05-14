package com.loopers.domain.user

import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType
import org.springframework.stereotype.Component

@Component
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    fun signup(command: SignupCommand): UserModel {
        if (userRepository.existsByLoginId(command.loginId)) {
            throw CoreException(errorType = ErrorType.CONFLICT, customMessage = "이미 사용 중인 로그인 ID 입니다.")
        }
        LoginId(command.loginId)
        Password(command.password, command.birthDate)
        Email(command.email)
        BirthDate(command.birthDate)
        if (command.name.isBlank()) {
            throw CoreException(errorType = ErrorType.BAD_REQUEST, customMessage = "이름은 공백일 수 없습니다.")
        }
        val user = UserModel(
            loginId = command.loginId,
            password = passwordEncoder.encode(command.password),
            name = command.name,
            birthDate = command.birthDate,
            email = command.email,
        )
        return userRepository.save(user)
    }

    fun getMe(loginId: String, password: String): UserModel {
        val user = userRepository.findByLoginId(loginId)
            ?: throw CoreException(errorType = ErrorType.UNAUTHORIZED, customMessage = "인증에 실패했습니다.")
        if (!passwordEncoder.matches(password, user.password)) {
            throw CoreException(errorType = ErrorType.UNAUTHORIZED, customMessage = "인증에 실패했습니다.")
        }
        return user
    }
}
