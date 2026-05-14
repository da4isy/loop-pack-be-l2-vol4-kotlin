package com.loopers.interfaces.api.user

import com.loopers.application.user.UserFacade
import com.loopers.interfaces.api.ApiResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userFacade: UserFacade,
) {
    @PostMapping
    fun signup(@RequestBody request: UserDto.SignupRequest): ApiResponse<UserDto.SignupResponse> {
        return userFacade.signup(request.toCommand())
            .let { UserDto.SignupResponse.from(it) }
            .let { ApiResponse.success(it) }
    }
}
