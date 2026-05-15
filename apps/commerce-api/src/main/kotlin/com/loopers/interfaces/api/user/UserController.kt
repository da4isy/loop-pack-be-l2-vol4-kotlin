package com.loopers.interfaces.api.user

import com.loopers.application.user.UserFacade
import com.loopers.interfaces.api.ApiResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userFacade: UserFacade,
) {
    @PostMapping
    fun signup(@Valid @RequestBody request: UserDto.SignupRequest): ApiResponse<UserDto.SignupResponse> {
        return userFacade.signup(request.toCommand())
            .let { UserDto.SignupResponse.from(it) }
            .let { ApiResponse.success(it) }
    }

    @GetMapping("/me")
    fun getMe(
        @RequestHeader("X-Loopers-LoginId") loginId: String,
        @RequestHeader("X-Loopers-LoginPw") password: String,
    ): ApiResponse<UserDto.GetMeResponse> {
        return userFacade.getMe(loginId, password)
            .let { UserDto.GetMeResponse.from(it) }
            .let { ApiResponse.success(it) }
    }

    @PatchMapping("/me/password")
    fun changePassword(
        @RequestHeader("X-Loopers-LoginId") loginId: String,
        @RequestHeader("X-Loopers-LoginPw") password: String,
        @Valid @RequestBody request: UserDto.ChangePasswordRequest,
    ): ApiResponse<Any> {
        userFacade.changePassword(loginId, request.currentPassword, request.newPassword)
        return ApiResponse.success()
    }
}
