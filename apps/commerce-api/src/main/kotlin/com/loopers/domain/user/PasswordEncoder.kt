package com.loopers.domain.user

interface PasswordEncoder {
    fun encode(rawPassword: String): String
}
