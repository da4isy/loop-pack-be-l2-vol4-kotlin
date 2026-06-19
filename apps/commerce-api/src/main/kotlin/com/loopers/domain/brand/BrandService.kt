package com.loopers.domain.brand

import com.loopers.support.cache.BrandCacheEvictEvent
import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class BrandService(
    private val brandRepository: BrandRepository,
    private val eventPublisher: ApplicationEventPublisher,
) {
    @Transactional(readOnly = true)
    fun getBrand(id: Long): BrandModel {
        val brand = brandRepository.findById(id)
            ?: throw CoreException(errorType = ErrorType.NOT_FOUND, customMessage = "존재하지 않는 브랜드입니다.")
        if (brand.isDeleted()) {
            throw CoreException(errorType = ErrorType.NOT_FOUND, customMessage = "삭제된 브랜드입니다.")
        }
        return brand
    }

    @Transactional(readOnly = true)
    fun getBrandsByIds(ids: List<Long>): Map<Long, BrandModel> {
        return brandRepository.findAllByIds(ids).associateBy { it.id }
    }

    @Transactional(readOnly = true)
    fun getAll(pageable: Pageable): Page<BrandModel> {
        return brandRepository.findAll(pageable)
    }

    @Transactional
    fun create(name: String): BrandModel {
        return brandRepository.save(BrandModel(name = name))
    }

    @Transactional
    fun update(id: Long, name: String): BrandModel {
        val brand = getBrand(id)
        brand.update(name)
        val saved = brandRepository.save(brand)
        eventPublisher.publishEvent(BrandCacheEvictEvent(id))
        return saved
    }

    @Transactional
    fun delete(id: Long) {
        val brand = getBrandIncludingDeleted(id)
        brand.delete()
        brandRepository.save(brand)
        eventPublisher.publishEvent(BrandCacheEvictEvent(id))
    }

    @Transactional(readOnly = true)
    fun getBrandIncludingDeleted(id: Long): BrandModel {
        return brandRepository.findById(id)
            ?: throw CoreException(errorType = ErrorType.NOT_FOUND, customMessage = "존재하지 않는 브랜드입니다.")
    }
}
