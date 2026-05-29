package com.loopers.domain.product

import com.loopers.domain.brand.BrandModel

data class ProductWithBrand(
    val product: ProductModel,
    val brand: BrandModel,
)
