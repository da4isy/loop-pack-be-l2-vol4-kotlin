package com.loopers.interfaces.api.like

import com.loopers.application.like.LikeFacade
import com.loopers.domain.user.UserService
import com.loopers.interfaces.api.ApiResponse
import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class LikeV1Controller(
    private val userService: UserService,
    private val likeFacade: LikeFacade,
) : LikeV1ApiSpec {

    @PostMapping("/api/v1/products/{productId}/likes")
    override fun like(
        @RequestHeader("X-Loopers-LoginId") loginId: String,
        @RequestHeader("X-Loopers-LoginPw") password: String,
        @PathVariable productId: Long,
    ): ApiResponse<Any> {
        val user = userService.getMe(loginId, password)
        likeFacade.like(user.id, productId)
        return ApiResponse.success()
    }

    @DeleteMapping("/api/v1/products/{productId}/likes")
    override fun unlike(
        @RequestHeader("X-Loopers-LoginId") loginId: String,
        @RequestHeader("X-Loopers-LoginPw") password: String,
        @PathVariable productId: Long,
    ): ApiResponse<Any> {
        val user = userService.getMe(loginId, password)
        likeFacade.unlike(user.id, productId)
        return ApiResponse.success()
    }

    @GetMapping("/api/v1/users/me/likes")
    override fun getMyLikes(
        @RequestHeader("X-Loopers-LoginId") loginId: String,
        @RequestHeader("X-Loopers-LoginPw") password: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ApiResponse<*> {
        val user = userService.getMe(loginId, password)
        val pageable = PageRequest.of(page, size)
        return likeFacade.getMyLikes(user.id, pageable)
            .map { LikeV1Dto.LikeResponse.from(it) }
            .let { ApiResponse.success(it) }
    }
}
