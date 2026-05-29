package com.loopers.interfaces.api.like

import com.loopers.application.like.LikeInfo
import com.loopers.domain.like.LikeService
import com.loopers.domain.product.ProductService
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
    private val productService: ProductService,
    private val likeService: LikeService,
) : LikeV1ApiSpec {

    @PostMapping("/api/v1/products/{productId}/likes")
    override fun like(
        @RequestHeader("X-Loopers-LoginId") loginId: String,
        @RequestHeader("X-Loopers-LoginPw") password: String,
        @PathVariable productId: Long,
    ): ApiResponse<Any> {
        val user = userService.getMe(loginId, password)
        productService.getProduct(productId)
        likeService.like(user.id, productId)
        return ApiResponse.success()
    }

    @DeleteMapping("/api/v1/products/{productId}/likes")
    override fun unlike(
        @RequestHeader("X-Loopers-LoginId") loginId: String,
        @RequestHeader("X-Loopers-LoginPw") password: String,
        @PathVariable productId: Long,
    ): ApiResponse<Any> {
        val user = userService.getMe(loginId, password)
        likeService.unlike(user.id, productId)
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
        val likes = likeService.getLikesByUserId(user.id, pageable)
        val productIds = likes.content.map { it.productId }.distinct()
        val products = productService.getProductsByIds(productIds)

        return likes.map { like -> LikeInfo.of(like, products[like.productId]) }
            .map { LikeV1Dto.LikeResponse.from(it) }
            .let { ApiResponse.success(it) }
    }
}
