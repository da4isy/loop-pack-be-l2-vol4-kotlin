package com.loopers.interfaces.api.coupon

import com.loopers.domain.coupon.CouponStatus
import com.loopers.domain.coupon.CouponType
import com.loopers.domain.coupon.IssuedCouponModel
import java.time.ZonedDateTime

class CouponV1Dto {

    data class IssuedCouponResponse(
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
            ) = IssuedCouponResponse(
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
}
