package com.loopers.interfaces.api.brand

import com.loopers.domain.brand.BrandService
import com.loopers.domain.product.ProductService
import com.loopers.interfaces.api.ApiResponse
import org.springframework.data.domain.PageRequest
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api-admin/v1/brands")
class BrandAdminV1Controller(
    private val brandService: BrandService,
    private val productService: ProductService,
) {

    @GetMapping
    fun getBrands(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ApiResponse<*> {
        return brandService.getAll(PageRequest.of(page, size))
            .map { BrandAdminV1Dto.BrandResponse.from(it) }
            .let { ApiResponse.success(it) }
    }

    @GetMapping("/{brandId}")
    fun getBrand(@PathVariable brandId: Long): ApiResponse<BrandAdminV1Dto.BrandResponse> {
        return brandService.getBrand(brandId)
            .let { BrandAdminV1Dto.BrandResponse.from(it) }
            .let { ApiResponse.success(it) }
    }

    @PostMapping
    fun createBrand(
        @RequestBody request: BrandAdminV1Dto.CreateBrandRequest,
    ): ApiResponse<BrandAdminV1Dto.BrandResponse> {
        return brandService.create(request.name)
            .let { BrandAdminV1Dto.BrandResponse.from(it) }
            .let { ApiResponse.success(it) }
    }

    @PutMapping("/{brandId}")
    fun updateBrand(
        @PathVariable brandId: Long,
        @RequestBody request: BrandAdminV1Dto.UpdateBrandRequest,
    ): ApiResponse<BrandAdminV1Dto.BrandResponse> {
        return brandService.update(brandId, request.name)
            .let { BrandAdminV1Dto.BrandResponse.from(it) }
            .let { ApiResponse.success(it) }
    }

    @DeleteMapping("/{brandId}")
    @Transactional
    fun deleteBrand(@PathVariable brandId: Long): ApiResponse<Unit> {
        productService.deleteByBrandId(brandId)
        brandService.delete(brandId)
        return ApiResponse.success(Unit)
    }
}
