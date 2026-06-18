package com.loopers.domain.coupon

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface IssuedCouponRepository {
    fun save(issuedCoupon: IssuedCouponModel): IssuedCouponModel
    fun findById(id: Long): IssuedCouponModel?
    fun findByIdWithLock(id: Long): IssuedCouponModel?
    fun findAllByUserId(userId: Long, pageable: Pageable): Page<IssuedCouponModel>
    fun findAllByCouponTemplateId(couponTemplateId: Long, pageable: Pageable): Page<IssuedCouponModel>
    fun existsByUserIdAndCouponTemplateId(userId: Long, couponTemplateId: Long): Boolean
}
