package com.loopers.domain.coupon

import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class CouponTemplateService(
    private val couponTemplateRepository: CouponTemplateRepository,
) {

    @Transactional
    fun create(couponTemplate: CouponTemplateModel): CouponTemplateModel {
        return couponTemplateRepository.save(couponTemplate)
    }

    @Transactional(readOnly = true)
    fun getById(id: Long): CouponTemplateModel {
        return couponTemplateRepository.findById(id)
            ?: throw CoreException(errorType = ErrorType.NOT_FOUND, customMessage = "존재하지 않는 쿠폰입니다.")
    }

    @Transactional(readOnly = true)
    fun getAll(pageable: Pageable): Page<CouponTemplateModel> {
        return couponTemplateRepository.findAll(pageable)
    }

    @Transactional
    fun update(
        id: Long,
        name: String,
        value: Long,
        minOrderAmount: Long?,
        expiredAt: java.time.ZonedDateTime,
    ): CouponTemplateModel {
        val template = getById(id)
        template.update(name, value, minOrderAmount, expiredAt)
        return couponTemplateRepository.save(template)
    }

    /**
     * 비관적 락으로 템플릿 조회 — 선착순 쿠폰 발급 시 수량 차감용
     */
    @Transactional
    fun getByIdWithLock(id: Long): CouponTemplateModel {
        return couponTemplateRepository.findByIdWithLock(id)
            ?: throw CoreException(errorType = ErrorType.NOT_FOUND, customMessage = "존재하지 않는 쿠폰입니다.")
    }

    @Transactional(readOnly = true)
    fun getByIds(ids: List<Long>): Map<Long, CouponTemplateModel> {
        if (ids.isEmpty()) return emptyMap()
        return ids.mapNotNull { couponTemplateRepository.findById(it) }.associateBy { it.id }
    }

    @Transactional
    fun delete(id: Long) {
        val template = getById(id)
        template.delete()
        couponTemplateRepository.save(template)
    }
}
