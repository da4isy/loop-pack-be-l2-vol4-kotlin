package com.loopers.application.product

import com.loopers.domain.brand.BrandModel
import com.loopers.domain.product.ProductModel

data class ProductDetailInfo(
    val id: Long,
    val name: String,
    val price: Long,
    val stock: Long,
    val brandId: Long,
    val brandName: String,
    val likeCount: Long,
) {
    companion object {
        fun of(product: ProductModel, brand: BrandModel, likeCount: Long = 0): ProductDetailInfo {
            return ProductDetailInfo(
                id = product.id,
                name = product.name,
                price = product.price,
                stock = product.stock,
                brandId = brand.id,
                brandName = brand.name,
                likeCount = likeCount,
            )
        }
    }
}
