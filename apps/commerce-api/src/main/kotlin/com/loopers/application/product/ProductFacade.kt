package com.loopers.application.product

import com.loopers.domain.like.LikeService
import com.loopers.domain.product.ProductDetailService
import com.loopers.domain.product.ProductSortType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class ProductFacade(
    private val productDetailService: ProductDetailService,
    private val likeService: LikeService,
) {

    fun getProductDetail(productId: Long): ProductDetailInfo {
        val (product, brand) = productDetailService.getProductWithBrand(productId)
        val likeCount = likeService.countByProductId(productId)
        return ProductDetailInfo.of(product, brand, likeCount)
    }

    fun getProducts(brandId: Long?, sortType: ProductSortType, pageable: Pageable): Page<ProductDetailInfo> {
        val productsWithBrands = productDetailService.getProductsWithBrands(brandId, sortType, pageable)
        val productIds = productsWithBrands.content.map { it.product.id }
        val likeCounts = likeService.countByProductIds(productIds)

        return productsWithBrands.map { (product, brand) ->
            ProductDetailInfo.of(product, brand, likeCounts[product.id] ?: 0)
        }
    }
}
