package com.loopers.interfaces.api.product

import com.loopers.domain.product.ProductModel
import java.time.ZonedDateTime

class ProductAdminV1Dto {

    data class CreateProductRequest(
        val name: String,
        val price: Long,
        val stock: Long,
        val brandId: Long,
    )

    data class UpdateProductRequest(
        val name: String,
        val price: Long,
        val stock: Long,
    )

    data class ProductResponse(
        val productId: Long,
        val name: String,
        val price: Long,
        val stock: Long,
        val brandId: Long,
        val createdAt: ZonedDateTime,
        val updatedAt: ZonedDateTime,
    ) {
        companion object {
            fun from(model: ProductModel) = ProductResponse(
                productId = model.id,
                name = model.name,
                price = model.price,
                stock = model.stock,
                brandId = model.brandId,
                createdAt = model.createdAt,
                updatedAt = model.updatedAt,
            )
        }
    }
}
