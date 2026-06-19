package com.loopers.application.brand

import com.loopers.domain.brand.BrandModel
import com.loopers.domain.brand.BrandService
import com.loopers.domain.product.ProductService
import com.loopers.infrastructure.brand.BrandCacheManager
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class BrandAdminFacade(
    private val brandService: BrandService,
    private val productService: ProductService,
    private val brandCacheManager: BrandCacheManager,
) {

    @Transactional
    fun updateBrand(id: Long, name: String): BrandModel {
        val brand = brandService.update(id, name)
        brandCacheManager.evictDetail(id)
        return brand
    }

    @Transactional
    fun deleteBrandWithProducts(brandId: Long) {
        productService.deleteByBrandId(brandId)
        brandService.delete(brandId)
        brandCacheManager.evictDetail(brandId)
    }
}
