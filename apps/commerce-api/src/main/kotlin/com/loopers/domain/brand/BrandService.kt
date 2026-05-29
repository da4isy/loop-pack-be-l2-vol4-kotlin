package com.loopers.domain.brand

import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType
import org.springframework.stereotype.Component

@Component
class BrandService(
    private val brandRepository: BrandRepository,
) {
    fun getBrand(id: Long): BrandModel {
        val brand = brandRepository.findById(id)
            ?: throw CoreException(errorType = ErrorType.NOT_FOUND, customMessage = "존재하지 않는 브랜드입니다.")
        if (brand.isDeleted()) {
            throw CoreException(errorType = ErrorType.NOT_FOUND, customMessage = "삭제된 브랜드입니다.")
        }
        return brand
    }

    fun getBrandsByIds(ids: List<Long>): Map<Long, BrandModel> {
        return brandRepository.findAllByIds(ids).associateBy { it.id }
    }
}
