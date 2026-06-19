package com.loopers.application.product

import com.loopers.domain.brand.BrandService
import com.loopers.domain.product.ProductDetailService
import com.loopers.domain.product.ProductService
import com.loopers.domain.product.ProductSortType
import com.loopers.infrastructure.brand.BrandCacheInfo
import com.loopers.infrastructure.brand.BrandCacheManager
import com.loopers.infrastructure.product.ProductCacheManager
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class ProductFacade(
    private val productDetailService: ProductDetailService,
    private val productService: ProductService,
    private val brandService: BrandService,
    private val productCacheManager: ProductCacheManager,
    private val brandCacheManager: BrandCacheManager,
) {

    fun getProductDetail(productId: Long): ProductDetailInfo {
        productCacheManager.getDetail(productId)?.let { return it }

        val product = productService.getProduct(productId)

        val cachedBrand = brandCacheManager.getDetail(product.brandId)
        val brandId: Long
        val brandName: String

        if (cachedBrand != null) {
            brandId = cachedBrand.id
            brandName = cachedBrand.name
        } else {
            val brand = brandService.getBrand(product.brandId)
            brandCacheManager.putDetail(product.brandId, BrandCacheInfo.from(brand))
            brandId = brand.id
            brandName = brand.name
        }

        val info = ProductDetailInfo(
            id = product.id,
            name = product.name,
            price = product.price,
            stock = product.stock,
            brandId = brandId,
            brandName = brandName,
            likeCount = product.likeCount,
        )

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
