package com.loopers.domain.coupon

import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class IssuedCouponService(
    private val issuedCouponRepository: IssuedCouponRepository,
    private val couponTemplateService: CouponTemplateService,
) {

    @Transactional
    fun issue(userId: Long, couponTemplateId: Long): IssuedCouponModel {
        val template = couponTemplateService.getById(couponTemplateId)

        if (template.isExpired()) {
            throw CoreException(errorType = ErrorType.BAD_REQUEST, customMessage = "만료된 쿠폰입니다.")
        }
        if (template.isDeleted()) {
            throw CoreException(errorType = ErrorType.NOT_FOUND, customMessage = "존재하지 않는 쿠폰입니다.")
        }
        if (issuedCouponRepository.existsByUserIdAndCouponTemplateId(userId, couponTemplateId)) {
            throw CoreException(errorType = ErrorType.CONFLICT, customMessage = "이미 발급받은 쿠폰입니다.")
        }

        val issuedCoupon = IssuedCouponModel(couponTemplateId = couponTemplateId, userId = userId)
        return issuedCouponRepository.save(issuedCoupon)
    }

    @Transactional(readOnly = true)
    fun getMyIssuedCoupons(userId: Long, pageable: Pageable): Page<IssuedCouponModel> {
        return issuedCouponRepository.findAllByUserId(userId, pageable)
    }

    @Transactional(readOnly = true)
    fun getIssuedCouponsByTemplateId(couponTemplateId: Long, pageable: Pageable): Page<IssuedCouponModel> {
        return issuedCouponRepository.findAllByCouponTemplateId(couponTemplateId, pageable)
    }

    @Transactional(readOnly = true)
    fun getById(id: Long): IssuedCouponModel {
        return issuedCouponRepository.findById(id)
            ?: throw CoreException(errorType = ErrorType.NOT_FOUND, customMessage = "존재하지 않는 발급 쿠폰입니다.")
    }

    /**
     * 비관적 락으로 쿠폰 조회 — 주문 시 동시성 제어용
     */
    @Transactional
    fun getByIdWithLock(id: Long): IssuedCouponModel {
        return issuedCouponRepository.findByIdWithLock(id)
            ?: throw CoreException(errorType = ErrorType.NOT_FOUND, customMessage = "존재하지 않는 발급 쿠폰입니다.")
    }
}
