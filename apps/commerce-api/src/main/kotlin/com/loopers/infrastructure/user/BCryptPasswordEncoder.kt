package com.loopers.infrastructure.user

import at.favre.lib.crypto.bcrypt.BCrypt
import com.loopers.domain.user.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class BCryptPasswordEncoder : PasswordEncoder {
    override fun encode(rawPassword: String): String =
        BCrypt.withDefaults().hashToString(COST, rawPassword.toCharArray())

    companion object {
        private const val COST = 12
    }
}
