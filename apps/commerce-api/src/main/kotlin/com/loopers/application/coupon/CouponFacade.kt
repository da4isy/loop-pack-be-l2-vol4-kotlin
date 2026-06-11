package com.loopers.application.coupon

import com.loopers.domain.coupon.CouponTemplateRepository
import com.loopers.domain.coupon.IssuedCouponService
import com.loopers.interfaces.api.coupon.CouponV1Dto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class CouponFacade(
    private val issuedCouponService: IssuedCouponService,
    private val couponTemplateRepository: CouponTemplateRepository,
) {

    /**
     * 내 쿠폰 목록 조회 — IssuedCoupon + CouponTemplate 조합 (cross-domain)
     */
    @Transactional(readOnly = true)
    fun getMyIssuedCoupons(userId: Long, pageable: Pageable): Page<CouponV1Dto.IssuedCouponResponse> {
        val issuedCoupons = issuedCouponService.getMyIssuedCoupons(userId, pageable)
        val templateIds = issuedCoupons.content.map { it.couponTemplateId }.distinct()
        val templates = templateIds.mapNotNull { couponTemplateRepository.findById(it) }.associateBy { it.id }

        return issuedCoupons.map { issued ->
            val template = templates[issued.couponTemplateId]
            CouponV1Dto.IssuedCouponResponse.from(
                model = issued,
                couponName = template?.name,
                couponType = template?.type,
                couponValue = template?.value,
            )
        }
    }
}
