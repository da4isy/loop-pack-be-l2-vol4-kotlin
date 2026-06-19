package com.loopers.application.coupon

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
                customMessage = "쿠폰 발급 수량이 모두 소진되었습니다.",
            )
        }

        try {
            return issuedCouponService.issue(userId, couponTemplateId)
        } catch (e: CoreException) {
            if (e.customMessage?.contains("소진") == true) {
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
