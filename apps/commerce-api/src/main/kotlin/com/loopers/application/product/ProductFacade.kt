package com.loopers.application.product

import com.loopers.domain.brand.BrandService
import com.loopers.domain.product.ProductService
import com.loopers.domain.product.ProductSortType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class ProductFacade(
    private val productService: ProductService,
    private val brandService: BrandService,
) {
    fun getProductDetail(productId: Long): ProductDetailInfo {
        val product = productService.getProduct(productId)
        val brand = brandService.getBrand(product.brandId)
        return ProductDetailInfo.of(product, brand)
    }

    fun getProducts(brandId: Long?, sortType: ProductSortType, pageable: Pageable): Page<ProductDetailInfo> {
        val products = productService.getProducts(brandId, sortType, pageable)
        val brandIds = products.content.map { it.brandId }.distinct()
        val brands = brandService.getBrandsByIds(brandIds)

        return products.map { product ->
            ProductDetailInfo.of(
                product = product,
                brand = brands[product.brandId]
                    ?: throw IllegalStateException("브랜드 정보를 찾을 수 없습니다. brandId=${product.brandId}"),
            )
        }
    }
}
