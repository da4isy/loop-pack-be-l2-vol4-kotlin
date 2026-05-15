package com.loopers.interfaces.api.user

import com.loopers.domain.user.SignupCommand
import com.loopers.domain.user.UserModel
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

class UserDto {
    data class SignupRequest(
        @field:Pattern(regexp = "^[a-zA-Z0-9]+$", message = "로그인 ID 는 영문/숫자만 허용됩니다.")
        @field:Size(min = 4, max = 20, message = "로그인 ID 는 4~20자여야 합니다.")
        val loginId: String,

        @field:Pattern(
            regexp = "^[a-zA-Z0-9!@#\$%^&*()_+\\-=]+$",
            message = "비밀번호에 허용되지 않은 문자가 포함되어 있습니다.",
        )
        @field:Size(min = 8, max = 16, message = "비밀번호는 8~16자여야 합니다.")
        val password: String,

        @field:NotBlank(message = "이름은 공백일 수 없습니다.")
        val name: String,

        @field:Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}\$", message = "올바른 생년월일 형식이 아닙니다 (yyyy-MM-dd).")
        val birthDate: String,

        @field:Email(message = "올바른 이메일 형식이 아닙니다.")
        val email: String,
    ) {
        fun toCommand() = SignupCommand(
            loginId = loginId,
            password = password,
            name = name,
            birthDate = birthDate,
            email = email,
        )
    }

    data class SignupResponse(
        val loginId: String,
        val name: String,
        val birthDate: String,
        val email: String,
    ) {
        companion object {
            fun from(model: UserModel) = SignupResponse(
                loginId = model.loginId,
                name = model.name,
                birthDate = model.birthDate,
                email = model.email,
            )
        }
    }

    data class GetMeResponse(
        val loginId: String,
        val name: String,
        val birthDate: String,
        val email: String,
    ) {
        companion object {
            fun from(model: UserModel) = GetMeResponse(
                loginId = model.loginId,
                name = model.maskedName(),
                birthDate = model.birthDate,
                email = model.email,
            )
        }
    }

    data class ChangePasswordRequest(
        @field:NotBlank(message = "현재 비밀번호는 공백일 수 없습니다.")
        val currentPassword: String,

        @field:Pattern(
            regexp = "^[a-zA-Z0-9!@#\$%^&*()_+\\-=]+$",
            message = "비밀번호에 허용되지 않은 문자가 포함되어 있습니다.",
        )
        @field:Size(min = 8, max = 16, message = "비밀번호는 8~16자여야 합니다.")
        val newPassword: String,
    )
}
