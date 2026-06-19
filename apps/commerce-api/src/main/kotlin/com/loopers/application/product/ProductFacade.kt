package com.loopers.application.product

import com.loopers.domain.product.ProductDetailService
import com.loopers.domain.product.ProductSortType
import com.loopers.infrastructure.product.ProductCacheManager
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class ProductFacade(
    private val productDetailService: ProductDetailService,
    private val productCacheManager: ProductCacheManager,
) {

    fun getProductDetail(productId: Long): ProductDetailInfo {
        productCacheManager.getDetail(productId)?.let { return it }

        val (product, brand) = productDetailService.getProductWithBrand(productId)
        val info = ProductDetailInfo.of(product, brand, product.likeCount)

        productCacheManager.putDetail(productId, info)
        return info
    }

    fun getProducts(brandId: Long?, sortType: ProductSortType, pageable: Pageable): Page<ProductDetailInfo> {
        val productsWithBrands = productDetailService.getProductsWithBrands(brandId, sortType, pageable)
        return productsWithBrands.map { (product, brand) ->
            ProductDetailInfo.of(product, brand, product.likeCount)
        }
    }
}
