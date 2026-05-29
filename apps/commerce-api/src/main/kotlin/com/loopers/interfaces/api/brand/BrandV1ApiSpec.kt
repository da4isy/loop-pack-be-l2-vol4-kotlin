package com.loopers.interfaces.api.brand

import com.loopers.interfaces.api.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "Brand", description = "브랜드 API")
interface BrandV1ApiSpec {
    @Operation(summary = "브랜드 상세 조회")
    fun getBrand(brandId: Long): ApiResponse<BrandV1Dto.BrandResponse>
}
