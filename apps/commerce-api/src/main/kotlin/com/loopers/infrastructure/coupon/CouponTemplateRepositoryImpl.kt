package com.loopers.infrastructure.coupon

import com.loopers.domain.coupon.CouponTemplateModel
import com.loopers.domain.coupon.CouponTemplateRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class CouponTemplateRepositoryImpl(
    private val couponTemplateJpaRepository: CouponTemplateJpaRepository,
) : CouponTemplateRepository {

    override fun save(couponTemplate: CouponTemplateModel): CouponTemplateModel =
        couponTemplateJpaRepository.save(couponTemplate)

    override fun findById(id: Long): CouponTemplateModel? =
        couponTemplateJpaRepository.findByIdOrNull(id)

    override fun findByIdWithLock(id: Long): CouponTemplateModel? =
        couponTemplateJpaRepository.findByIdWithLock(id)

    override fun findAll(pageable: Pageable): Page<CouponTemplateModel> =
        couponTemplateJpaRepository.findAllByDeletedAtIsNull(pageable)
}
