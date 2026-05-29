package com.loopers.interfaces.api.like

import com.loopers.interfaces.api.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "Like", description = "좋아요 API")
interface LikeV1ApiSpec {

    @Operation(summary = "좋아요 등록", description = "멱등 — 이미 좋아요한 경우에도 200 OK")
    fun like(loginId: String, password: String, productId: Long): ApiResponse<Any>

    @Operation(summary = "좋아요 취소", description = "멱등 — 좋아요하지 않은 경우에도 200 OK")
    fun unlike(loginId: String, password: String, productId: Long): ApiResponse<Any>

    @Operation(summary = "내 좋아요 목록 조회")
    fun getMyLikes(loginId: String, password: String, page: Int, size: Int): ApiResponse<*>
}
