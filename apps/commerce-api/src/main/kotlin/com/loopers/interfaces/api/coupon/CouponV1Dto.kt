package com.loopers.interfaces.api.coupon

import com.loopers.application.coupon.IssuedCouponInfo
import com.loopers.domain.coupon.CouponStatus
import com.loopers.domain.coupon.CouponType
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
            fun from(info: IssuedCouponInfo) = IssuedCouponResponse(
                issuedCouponId = info.issuedCouponId,
                couponTemplateId = info.couponTemplateId,
                couponName = info.couponName,
                couponType = info.couponType,
                couponValue = info.couponValue,
                status = info.status,
                usedAt = info.usedAt,
                createdAt = info.createdAt,
            )
        }
    }
}
