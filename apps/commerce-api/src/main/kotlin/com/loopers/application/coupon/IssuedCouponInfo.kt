package com.loopers.application.coupon

import com.loopers.domain.coupon.CouponStatus
import com.loopers.domain.coupon.CouponType
import com.loopers.domain.coupon.IssuedCouponModel
import java.time.ZonedDateTime

data class IssuedCouponInfo(
    val issuedCouponId: Long,
    val couponTemplateId: Long,
    val couponName: String?,
    val couponType: CouponType?,
    val couponValue: Long?,
    val status: CouponStatus,
    val usedAt: ZonedDateTime?,
    val createdAt: ZonedDateTime,
) {
    companion object {
        fun from(
            model: IssuedCouponModel,
            couponName: String?,
            couponType: CouponType?,
            couponValue: Long?,
        ) = IssuedCouponInfo(
            issuedCouponId = model.id,
            couponTemplateId = model.couponTemplateId,
            couponName = couponName,
            couponType = couponType,
            couponValue = couponValue,
            status = model.status,
            usedAt = model.usedAt,
            createdAt = model.createdAt,
        )
    }
}
