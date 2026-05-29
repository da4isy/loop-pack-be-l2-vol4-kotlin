package com.loopers.infrastructure.brand

import com.loopers.domain.brand.BrandModel
import com.loopers.domain.brand.BrandRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class BrandRepositoryImpl(
    private val brandJpaRepository: BrandJpaRepository,
) : BrandRepository {
    override fun save(brand: BrandModel): BrandModel = brandJpaRepository.save(brand)
    override fun findById(id: Long): BrandModel? = brandJpaRepository.findByIdOrNull(id)
    override fun findAllByIds(ids: List<Long>): List<BrandModel> = brandJpaRepository.findAllById(ids)
}
