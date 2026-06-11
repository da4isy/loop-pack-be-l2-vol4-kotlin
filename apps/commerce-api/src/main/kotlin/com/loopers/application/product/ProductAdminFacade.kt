package com.loopers.application.product

import com.loopers.domain.brand.BrandService
import com.loopers.domain.product.ProductModel
import com.loopers.domain.product.ProductService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ProductAdminFacade(
    private val productService: ProductService,
    private val brandService: BrandService,
) {

    /**
     * 상품 등록 — 브랜드 존재 여부 검증 후 생성
     */
    @Transactional
    fun createProduct(name: String, price: Long, stock: Long, brandId: Long): ProductModel {
        brandService.getBrand(brandId) // 삭제된 브랜드면 NOT_FOUND
        return productService.create(name, price, stock, brandId)
    }
}
