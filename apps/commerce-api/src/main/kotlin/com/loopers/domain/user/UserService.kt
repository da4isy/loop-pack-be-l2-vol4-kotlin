package com.loopers.domain.user

import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    @Transactional
    fun signup(command: SignupCommand): UserModel {
        if (userRepository.existsByLoginId(command.loginId)) {
            throw CoreException(errorType = ErrorType.CONFLICT, customMessage = "이미 사용 중인 로그인 ID 입니다.")
        }
        return userRepository.save(UserModel.create(command, passwordEncoder))
    }

    @Transactional(readOnly = true)
    fun getMe(loginId: String, password: String): UserModel {
        val user = userRepository.findByLoginId(loginId)
            ?: throw CoreException(errorType = ErrorType.UNAUTHORIZED, customMessage = "인증에 실패했습니다.")
        if (!passwordEncoder.matches(password, user.password)) {
            throw CoreException(errorType = ErrorType.UNAUTHORIZED, customMessage = "인증에 실패했습니다.")
        }
        return user
    }

    @Transactional
    fun changePassword(loginId: String, currentPassword: String, newPassword: String) {
        val user = userRepository.findByLoginId(loginId)
            ?: throw CoreException(errorType = ErrorType.UNAUTHORIZED, customMessage = "인증에 실패했습니다.")
        if (!passwordEncoder.matches(currentPassword, user.password)) {
            throw CoreException(errorType = ErrorType.UNAUTHORIZED, customMessage = "현재 비밀번호가 일치하지 않습니다.")
        }
        user.changePassword(currentPassword, newPassword, passwordEncoder)
        userRepository.save(user)
    }
}
