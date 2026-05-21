package com.loopers.domain.user

import com.loopers.domain.BaseEntity
import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.format.DateTimeParseException

@Entity
@Table(name = "users")
class UserModel(
    loginId: String,
    password: String,
    name: String,
    birthDate: String,
    email: String,
) : BaseEntity() {

    @Column(name = "login_id", nullable = false, unique = true, length = 20)
    var loginId: String = loginId
        protected set

    @Column(name = "password", nullable = false)
    var password: String = password
        protected set

    @Column(name = "name", nullable = false)
    var name: String = name
        protected set

    @Column(name = "birth_date", nullable = false)
    var birthDate: String = birthDate
        protected set

    @Column(name = "email", nullable = false)
    var email: String = email
        protected set

    init {
        val parsed = try {
            LocalDate.parse(birthDate)
        } catch (e: DateTimeParseException) {
            throw CoreException(
                errorType = ErrorType.BAD_REQUEST,
                customMessage = "올바른 생년월일 형식이 아닙니다 (yyyy-MM-dd).",
            )
        }
        if (parsed.isAfter(LocalDate.now())) {
            throw CoreException(
                errorType = ErrorType.BAD_REQUEST,
                customMessage = "생년월일은 미래일 수 없습니다.",
            )
        }
    }

    fun maskedName(): String = if (name.length <= 1) "*" else name.dropLast(1) + "*"

    fun changePassword(currentRawPassword: String, newRawPassword: String, encoder: PasswordEncoder) {
        if (currentRawPassword == newRawPassword) {
            throw CoreException(
                errorType = ErrorType.BAD_REQUEST,
                customMessage = "현재 비밀번호와 새 비밀번호가 같습니다.",
            )
        }
        if (newRawPassword.contains(this.birthDate.replace("-", ""))) {
            throw CoreException(
                errorType = ErrorType.BAD_REQUEST,
                customMessage = "비밀번호에 생년월일을 포함할 수 없습니다.",
            )
        }
        this.password = encoder.encode(newRawPassword)
    }

    companion object {
        fun create(command: SignupCommand, encoder: PasswordEncoder): UserModel {
            if (command.password.contains(command.birthDate.replace("-", ""))) {
                throw CoreException(
                    errorType = ErrorType.BAD_REQUEST,
                    customMessage = "비밀번호에 생년월일을 포함할 수 없습니다.",
                )
            }
            return UserModel(
                loginId = command.loginId,
                password = encoder.encode(command.password),
                name = command.name,
                birthDate = command.birthDate,
                email = command.email,
            )
        }
    }
}
