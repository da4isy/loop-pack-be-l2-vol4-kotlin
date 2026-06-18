package com.loopers.interfaces.api.product

import com.loopers.application.product.ProductAdminFacade
import com.loopers.domain.product.ProductService
import com.loopers.domain.product.ProductSortType
import com.loopers.interfaces.api.ApiResponse
import org.springframework.data.domain.PageRequest
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
@RequestMapping("/api-admin/v1/products")
class ProductAdminV1Controller(
    private val productService: ProductService,
    private val productAdminFacade: ProductAdminFacade,
) {

    @GetMapping
    fun getProducts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) brandId: Long?,
    ): ApiResponse<*> {
        return productService.getProducts(brandId, ProductSortType.LATEST, PageRequest.of(page, size))
            .map { ProductAdminV1Dto.ProductResponse.from(it) }
            .let { ApiResponse.success(it) }
    }

    @GetMapping("/{productId}")
    fun getProduct(@PathVariable productId: Long): ApiResponse<ProductAdminV1Dto.ProductResponse> {
        return productService.getProduct(productId)
            .let { ProductAdminV1Dto.ProductResponse.from(it) }
            .let { ApiResponse.success(it) }
    }

    @PostMapping
    fun createProduct(
        @RequestBody request: ProductAdminV1Dto.CreateProductRequest,
    ): ApiResponse<ProductAdminV1Dto.ProductResponse> {
        return productAdminFacade.createProduct(
            request.name,
            request.price,
            request.stock,
            request.brandId,
        )
            .let { ProductAdminV1Dto.ProductResponse.from(it) }
            .let { ApiResponse.success(it) }
    }

    @PutMapping("/{productId}")
    fun updateProduct(
        @PathVariable productId: Long,
        @RequestBody request: ProductAdminV1Dto.UpdateProductRequest,
    ): ApiResponse<ProductAdminV1Dto.ProductResponse> {
        return productService.update(productId, request.name, request.price, request.stock)
            .let { ProductAdminV1Dto.ProductResponse.from(it) }
            .let { ApiResponse.success(it) }
    }

    @DeleteMapping("/{productId}")
    fun deleteProduct(@PathVariable productId: Long): ApiResponse<Unit> {
        productService.delete(productId)
        return ApiResponse.success(Unit)
    }
}
