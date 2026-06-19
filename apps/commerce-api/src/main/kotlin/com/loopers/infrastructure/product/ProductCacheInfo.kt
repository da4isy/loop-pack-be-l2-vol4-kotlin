package com.loopers.infrastructure.product

data class ProductCacheInfo(
    val id: Long = 0,
    val name: String = "",
    val price: Long = 0,
    val stock: Long = 0,
    val brandId: Long = 0,
    val brandName: String = "",
    val likeCount: Long = 0,
)
