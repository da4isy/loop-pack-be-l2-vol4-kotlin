package com.loopers.application.like

import com.loopers.domain.like.LikeModel
import com.loopers.domain.product.ProductModel

data class LikeInfo(
    val likeId: Long,
    val productId: Long,
    val productName: String,
    val productPrice: Long,
    val isProductDeleted: Boolean,
) {
    companion object {
        fun of(like: LikeModel, product: ProductModel?): LikeInfo {
            return LikeInfo(
                likeId = like.id,
                productId = like.productId,
                productName = product?.name ?: "삭제된 상품",
                productPrice = product?.price ?: 0,
                isProductDeleted = product == null || product.isDeleted(),
            )
        }
    }
}
