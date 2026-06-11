package com.loopers.infrastructure.coupon

import com.loopers.domain.coupon.IssuedCouponModel
import jakarta.persistence.LockModeType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query

interface IssuedCouponJpaRepository : JpaRepository<IssuedCouponModel, Long> {
    fun findAllByUserIdOrderByCreatedAtDesc(userId: Long, pageable: Pageable): Page<IssuedCouponModel>
    fun findAllByCouponTemplateIdOrderByCreatedAtDesc(couponTemplateId: Long, pageable: Pageable): Page<IssuedCouponModel>
    fun existsByUserIdAndCouponTemplateId(userId: Long, couponTemplateId: Long): Boolean

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ic FROM IssuedCouponModel ic WHERE ic.id = :id")
    fun findByIdWithLock(id: Long): IssuedCouponModel?
}
