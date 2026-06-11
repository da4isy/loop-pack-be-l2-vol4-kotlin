package com.loopers.infrastructure.coupon

import com.loopers.domain.coupon.IssuedCouponModel
import com.loopers.domain.coupon.IssuedCouponRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class IssuedCouponRepositoryImpl(
    private val issuedCouponJpaRepository: IssuedCouponJpaRepository,
) : IssuedCouponRepository {

    override fun save(issuedCoupon: IssuedCouponModel): IssuedCouponModel =
        issuedCouponJpaRepository.save(issuedCoupon)

    override fun findById(id: Long): IssuedCouponModel? =
        issuedCouponJpaRepository.findByIdOrNull(id)

    override fun findByIdWithLock(id: Long): IssuedCouponModel? =
        issuedCouponJpaRepository.findByIdWithLock(id)

    override fun findAllByUserId(userId: Long, pageable: Pageable): Page<IssuedCouponModel> =
        issuedCouponJpaRepository.findAllByUserIdOrderByCreatedAtDesc(userId, pageable)

    override fun findAllByCouponTemplateId(couponTemplateId: Long, pageable: Pageable): Page<IssuedCouponModel> =
        issuedCouponJpaRepository.findAllByCouponTemplateIdOrderByCreatedAtDesc(couponTemplateId, pageable)

    override fun existsByUserIdAndCouponTemplateId(userId: Long, couponTemplateId: Long): Boolean =
        issuedCouponJpaRepository.existsByUserIdAndCouponTemplateId(userId, couponTemplateId)
}
