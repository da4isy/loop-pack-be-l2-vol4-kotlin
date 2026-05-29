package com.loopers.interfaces.api.like

import com.loopers.application.like.LikeInfo

class LikeV1Dto {
    data class LikeResponse(
        val likeId: Long,
        val productId: Long,
        val productName: String,
        val productPrice: Long,
        val isProductDeleted: Boolean,
    ) {
        companion object {
            fun from(info: LikeInfo) = LikeResponse(
                likeId = info.likeId,
                productId = info.productId,
                productName = info.productName,
                productPrice = info.productPrice,
                isProductDeleted = info.isProductDeleted,
            )
        }
    }
}
