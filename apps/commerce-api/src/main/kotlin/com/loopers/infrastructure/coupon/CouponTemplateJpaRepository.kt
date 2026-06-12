package com.loopers.infrastructure.coupon

import com.loopers.domain.coupon.CouponTemplateModel
import jakarta.persistence.LockModeType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query

interface CouponTemplateJpaRepository : JpaRepository<CouponTemplateModel, Long> {
    fun findAllByDeletedAtIsNull(pageable: Pageable): Page<CouponTemplateModel>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ct FROM CouponTemplateModel ct WHERE ct.id = :id")
    fun findByIdWithLock(id: Long): CouponTemplateModel?
}
