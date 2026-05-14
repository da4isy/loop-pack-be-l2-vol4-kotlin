package com.loopers.interfaces.api.user

import com.loopers.application.user.MyInfo
import com.loopers.application.user.UserInfo
import com.loopers.domain.user.SignupCommand

class UserDto {
    data class SignupRequest(
        val loginId: String,
        val password: String,
        val name: String,
        val birthDate: String,
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
            fun from(info: UserInfo) = SignupResponse(
                loginId = info.loginId,
                name = info.name,
                birthDate = info.birthDate,
                email = info.email,
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
            fun from(info: MyInfo) = GetMeResponse(
                loginId = info.loginId,
                name = info.maskedName,
                birthDate = info.birthDate,
                email = info.email,
            )
        }
    }
}
