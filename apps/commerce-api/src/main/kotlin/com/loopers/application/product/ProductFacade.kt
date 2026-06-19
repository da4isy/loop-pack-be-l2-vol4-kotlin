package com.loopers.application.product

import com.loopers.domain.product.ProductDetailService
import com.loopers.domain.product.ProductSortType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class ProductFacade(
    private val productDetailService: ProductDetailService,
) {

    fun getProductDetail(productId: Long): ProductDetailInfo {
        val (product, brand) = productDetailService.getProductWithBrand(productId)
        return ProductDetailInfo.of(product, brand, product.likeCount)
    }

    fun getProducts(brandId: Long?, sortType: ProductSortType, pageable: Pageable): Page<ProductDetailInfo> {
        val productsWithBrands = productDetailService.getProductsWithBrands(brandId, sortType, pageable)
        return productsWithBrands.map { (product, brand) ->
            ProductDetailInfo.of(product, brand, product.likeCount)
        }
    }
}
