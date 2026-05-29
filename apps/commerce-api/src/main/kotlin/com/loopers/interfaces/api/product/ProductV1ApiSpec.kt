package com.loopers.interfaces.api.product

import com.loopers.interfaces.api.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "Product", description = "상품 API")
interface ProductV1ApiSpec {

    @Operation(summary = "상품 목록 조회")
    fun getProducts(
        brandId: Long?,
        sort: String?,
        page: Int,
        size: Int,
    ): ApiResponse<*>

    @Operation(summary = "상품 상세 조회")
    fun getProduct(productId: Long): ApiResponse<ProductV1Dto.ProductResponse>
}
