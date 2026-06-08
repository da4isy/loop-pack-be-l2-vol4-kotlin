package com.loopers.domain.product

import com.loopers.domain.brand.BrandService
import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

/**
 * Domain Service — Product + Brand 조합 협력 로직.
 * 상태 없이, 도메인 객체의 협력 중심으로 설계.
 */
@Component
class ProductDetailService(
    private val productService: ProductService,
    private val brandService: BrandService,
) {

    fun getProductWithBrand(productId: Long): ProductWithBrand {
        val product = productService.getProduct(productId)
        val brand = brandService.getBrand(product.brandId)
        return ProductWithBrand(product, brand)
    }

    fun getProductsWithBrands(
        brandId: Long?,
        sortType: ProductSortType,
        pageable: Pageable,
    ): Page<ProductWithBrand> {
        val products = productService.getProducts(brandId, sortType, pageable)
        val brandIds = products.content.map { it.brandId }.distinct()
        val brands = brandService.getBrandsByIds(brandIds)

        return products.map { product ->
            ProductWithBrand(
                product = product,
                brand = brands[product.brandId]
                    ?: throw CoreException(
                        errorType = ErrorType.NOT_FOUND,
                        customMessage = "브랜드 정보를 찾을 수 없습니다. brandId=${product.brandId}",
                    ),
            )
        }
    }
}
