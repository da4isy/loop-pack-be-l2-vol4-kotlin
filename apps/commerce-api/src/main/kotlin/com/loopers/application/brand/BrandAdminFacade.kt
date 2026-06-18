package com.loopers.application.brand

import com.loopers.domain.brand.BrandService
import com.loopers.domain.product.ProductService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class BrandAdminFacade(
    private val brandService: BrandService,
    private val productService: ProductService,
) {

    /**
     * 브랜드 삭제 — 소속 상품 연쇄 soft delete 후 브랜드 삭제
     */
    @Transactional
    fun deleteBrandWithProducts(brandId: Long) {
        productService.deleteByBrandId(brandId)
        brandService.delete(brandId)
    }
}
