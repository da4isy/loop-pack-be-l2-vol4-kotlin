package com.loopers.application.coupon

import com.loopers.domain.coupon.CouponTemplateModel
import com.loopers.domain.coupon.CouponTemplateService
import com.loopers.domain.coupon.IssuedCouponModel
import com.loopers.domain.coupon.IssuedCouponService
import com.loopers.infrastructure.coupon.CouponSoldOutCacheManager
import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class CouponFacade(
    private val issuedCouponService: IssuedCouponService,
    private val couponTemplateService: CouponTemplateService,
    private val couponSoldOutCacheManager: CouponSoldOutCacheManager,
) {

    fun issueCoupon(userId: Long, couponTemplateId: Long): IssuedCouponModel {
        if (couponSoldOutCacheManager.isSoldOut(couponTemplateId)) {
            throw CoreException(
                errorType = ErrorType.BAD_REQUEST,
                customMessage = CouponTemplateModel.SOLD_OUT_MESSAGE,
            )
        }

        try {
            return issuedCouponService.issue(userId, couponTemplateId)
        } catch (e: CoreException) {
            if (e.customMessage == CouponTemplateModel.SOLD_OUT_MESSAGE) {
                couponSoldOutCacheManager.markSoldOut(couponTemplateId)
            }
            throw e
        }
    }

    @Transactional(readOnly = true)
    fun getMyIssuedCoupons(userId: Long, pageable: Pageable): Page<IssuedCouponInfo> {
        val issuedCoupons = issuedCouponService.getMyIssuedCoupons(userId, pageable)
        val templateIds = issuedCoupons.content.map { it.couponTemplateId }.distinct()
        val templates = couponTemplateService.getByIds(templateIds)

        return issuedCoupons.map { issued ->
            val template = templates[issued.couponTemplateId]
            IssuedCouponInfo.from(
                model = issued,
                couponName = template?.name,
                couponType = template?.type,
                couponValue = template?.value,
            )
        }
    }
}
