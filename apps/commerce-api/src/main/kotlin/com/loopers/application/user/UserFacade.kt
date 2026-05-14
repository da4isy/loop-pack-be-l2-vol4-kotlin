package com.loopers.application.user

import com.loopers.domain.user.SignupCommand
import com.loopers.domain.user.UserService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class UserFacade(
    private val userService: UserService,
) {
    @Transactional
    fun signup(command: SignupCommand): UserInfo {
        val user = userService.signup(command)
        return UserInfo.from(user)
    }
}
