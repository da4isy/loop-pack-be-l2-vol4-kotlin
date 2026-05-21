package com.loopers.domain.user

data class SignupCommand(
    val loginId: String,
    val password: String,
    val name: String,
    val birthDate: String,
    val email: String,
)
