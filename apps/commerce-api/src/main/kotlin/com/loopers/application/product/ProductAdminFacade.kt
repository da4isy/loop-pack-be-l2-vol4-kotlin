package com.loopers.application.product

import com.loopers.domain.brand.BrandService
import com.loopers.domain.product.ProductModel
import com.loopers.domain.product.ProductService
import com.loopers.infrastructure.product.ProductCacheManager
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ProductAdminFacade(
    private val productService: ProductService,
    private val brandService: BrandService,
    private val productCacheManager: ProductCacheManager,
) {

    @Transactional
    fun createProduct(name: String, price: Long, stock: Long, brandId: Long): ProductModel {
        brandService.getBrand(brandId)
        return productService.create(name, price, stock, brandId)
    }

    @Transactional
    fun updateProduct(id: Long, name: String, price: Long, stock: Long): ProductModel {
        val product = productService.update(id, name, price, stock)
        productCacheManager.evictDetail(id)
        return product
    }

    @Transactional
    fun deleteProduct(id: Long) {
        productService.delete(id)
        productCacheManager.evictDetail(id)
    }
}
