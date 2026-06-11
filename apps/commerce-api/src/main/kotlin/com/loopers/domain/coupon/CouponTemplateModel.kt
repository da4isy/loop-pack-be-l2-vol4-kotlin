package com.loopers.domain.coupon

import com.loopers.domain.BaseEntity
import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import java.time.ZonedDateTime
import kotlin.math.min

@Entity
@Table(name = "coupon_templates")
class CouponTemplateModel(
    name: String,
    type: CouponType,
    value: Long,
    minOrderAmount: Long?,
    expiredAt: ZonedDateTime,
) : BaseEntity() {

    @Column(name = "name", nullable = false)
    var name: String = name
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: CouponType = type
        protected set

    @Column(name = "value", nullable = false)
    var value: Long = value
        protected set

    @Column(name = "min_order_amount")
    var minOrderAmount: Long? = minOrderAmount
        protected set

    @Column(name = "expired_at", nullable = false)
    var expiredAt: ZonedDateTime = expiredAt
        protected set

    init {
        if (name.isBlank()) {
            throw CoreException(errorType = ErrorType.BAD_REQUEST, customMessage = "쿠폰 이름은 비어있을 수 없습니다.")
        }
        if (value <= 0) {
            throw CoreException(errorType = ErrorType.BAD_REQUEST, customMessage = "쿠폰 할인 값은 0보다 커야 합니다.")
        }
        if (type == CouponType.RATE && value > 100) {
            throw CoreException(errorType = ErrorType.BAD_REQUEST, customMessage = "정률 쿠폰 할인율은 100%를 초과할 수 없습니다.")
        }
        if (minOrderAmount != null && minOrderAmount!! < 0) {
            throw CoreException(errorType = ErrorType.BAD_REQUEST, customMessage = "최소 주문 금액은 0 이상이어야 합니다.")
        }
    }

    fun calculateDiscount(orderAmount: Long): Long {
        return when (type) {
            CouponType.FIXED -> min(value, orderAmount)
            CouponType.RATE -> orderAmount * value / 100
        }
    }

    fun isExpired(): Boolean = ZonedDateTime.now().isAfter(expiredAt)

    fun update(name: String, value: Long, minOrderAmount: Long?, expiredAt: ZonedDateTime) {
        this.name = name
        this.value = value
        this.minOrderAmount = minOrderAmount
        this.expiredAt = expiredAt
    }
}
