package com.loopers.application.user

import com.loopers.domain.user.UserModel

data class UserInfo(
    val loginId: String,
    val name: String,
    val birthDate: String,
    val email: String,
) {
    companion object {
        fun from(model: UserModel) = UserInfo(
            loginId = model.loginId,
            name = model.name,
            birthDate = model.birthDate,
            email = model.email,
        )
    }
}
