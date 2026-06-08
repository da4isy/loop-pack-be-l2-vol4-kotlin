package com.loopers.interfaces.api.product

import com.loopers.application.product.ProductDetailInfo

class ProductV1Dto {
    data class ProductResponse(
        val id: Long,
        val name: String,
        val price: Long,
        val stock: Long,
        val brandId: Long,
        val brandName: String,
        val likeCount: Long,
    ) {
        companion object {
            fun from(info: ProductDetailInfo) = ProductResponse(
                id = info.id,
                name = info.name,
                price = info.price,
                stock = info.stock,
                brandId = info.brandId,
                brandName = info.brandName,
                likeCount = info.likeCount,
            )
        }
    }
}
