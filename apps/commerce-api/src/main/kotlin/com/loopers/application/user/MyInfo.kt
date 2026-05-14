package com.loopers.application.user

import com.loopers.domain.user.UserModel

data class MyInfo(
    val loginId: String,
    val maskedName: String,
    val birthDate: String,
    val email: String,
) {
    companion object {
        fun from(model: UserModel) = MyInfo(
            loginId = model.loginId,
            maskedName = model.maskedName(),
            birthDate = model.birthDate,
            email = model.email,
        )
    }
}
