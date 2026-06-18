package com.loopers.interfaces.api.brand

import com.loopers.domain.brand.BrandModel
import java.time.ZonedDateTime

class BrandAdminV1Dto {

    data class CreateBrandRequest(
        val name: String,
    )

    data class UpdateBrandRequest(
        val name: String,
    )

    data class BrandResponse(
        val brandId: Long,
        val name: String,
        val createdAt: ZonedDateTime,
        val updatedAt: ZonedDateTime,
    ) {
        companion object {
            fun from(model: BrandModel) = BrandResponse(
                brandId = model.id,
                name = model.name,
                createdAt = model.createdAt,
                updatedAt = model.updatedAt,
            )
        }
    }
}
