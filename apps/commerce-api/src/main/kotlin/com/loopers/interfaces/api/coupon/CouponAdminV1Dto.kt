package com.loopers.interfaces.api.coupon

import com.loopers.domain.coupon.CouponStatus
import com.loopers.domain.coupon.CouponTemplateModel
import com.loopers.domain.coupon.CouponType
import com.loopers.domain.coupon.IssuedCouponModel
import java.time.ZonedDateTime

class CouponAdminV1Dto {

    data class CreateCouponTemplateRequest(
        val name: String,
        val type: CouponType,
        val value: Long,
        val minOrderAmount: Long?,
        val expiredAt: ZonedDateTime,
        val totalQuantity: Long? = null,
    )

    data class UpdateCouponTemplateRequest(
        val name: String,
        val value: Long,
        val minOrderAmount: Long?,
        val expiredAt: ZonedDateTime,
    )

    data class CouponTemplateResponse(
        val couponId: Long,
        val name: String,
        val type: CouponType,
        val value: Long,
        val minOrderAmount: Long?,
        val totalQuantity: Long?,
        val issuedCount: Long,
        val expiredAt: ZonedDateTime,
        val createdAt: ZonedDateTime,
    ) {
        companion object {
            fun from(model: CouponTemplateModel) = CouponTemplateResponse(
                couponId = model.id,
                name = model.name,
                type = model.type,
                value = model.value,
                minOrderAmount = model.minOrderAmount,
                totalQuantity = model.totalQuantity,
                issuedCount = model.issuedCount,
                expiredAt = model.expiredAt,
                createdAt = model.createdAt,
            )
        }
    }

    data class IssuedCouponResponse(
        val issuedCouponId: Long,
        val couponTemplateId: Long,
        val userId: Long,
        val status: CouponStatus,
        val usedAt: ZonedDateTime?,
        val createdAt: ZonedDateTime,
    ) {
        companion object {
            fun from(model: IssuedCouponModel) = IssuedCouponResponse(
                issuedCouponId = model.id,
                couponTemplateId = model.couponTemplateId,
                userId = model.userId,
                status = model.status,
                usedAt = model.usedAt,
                createdAt = model.createdAt,
            )
        }
    }
}
