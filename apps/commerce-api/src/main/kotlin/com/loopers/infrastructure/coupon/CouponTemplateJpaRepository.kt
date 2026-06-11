package com.loopers.infrastructure.coupon

import com.loopers.domain.coupon.CouponTemplateModel
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface CouponTemplateJpaRepository : JpaRepository<CouponTemplateModel, Long> {
    fun findAllByDeletedAtIsNull(pageable: Pageable): Page<CouponTemplateModel>
}
