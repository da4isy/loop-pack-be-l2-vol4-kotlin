package com.loopers.interfaces.api.brand

import com.loopers.domain.brand.BrandModel

class BrandV1Dto {
    data class BrandResponse(
        val id: Long,
        val name: String,
    ) {
        companion object {
            fun from(model: BrandModel) = BrandResponse(
                id = model.id,
                name = model.name,
            )
        }
    }
}
