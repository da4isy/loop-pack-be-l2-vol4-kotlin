package com.loopers.interfaces.api.coupon

import com.loopers.domain.coupon.CouponTemplateModel
import com.loopers.domain.coupon.CouponTemplateService
import com.loopers.domain.coupon.IssuedCouponService
import com.loopers.interfaces.api.ApiResponse
import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api-admin/v1/coupons")
class CouponAdminV1Controller(
    private val couponTemplateService: CouponTemplateService,
    private val issuedCouponService: IssuedCouponService,
) {

    @PostMapping
    fun createCouponTemplate(
        @RequestBody request: CouponAdminV1Dto.CreateCouponTemplateRequest,
    ): ApiResponse<CouponAdminV1Dto.CouponTemplateResponse> {
        val template = couponTemplateService.create(
            CouponTemplateModel(
                name = request.name,
                type = request.type,
                value = request.value,
                minOrderAmount = request.minOrderAmount,
                expiredAt = request.expiredAt,
            ),
        )
        return ApiResponse.success(CouponAdminV1Dto.CouponTemplateResponse.from(template))
    }

    @GetMapping
    fun getCouponTemplates(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ApiResponse<*> {
        val pageable = PageRequest.of(page, size)
        return couponTemplateService.getAll(pageable)
            .map { CouponAdminV1Dto.CouponTemplateResponse.from(it) }
            .let { ApiResponse.success(it) }
    }

    @GetMapping("/{couponId}")
    fun getCouponTemplate(
        @PathVariable couponId: Long,
    ): ApiResponse<CouponAdminV1Dto.CouponTemplateResponse> {
        return couponTemplateService.getById(couponId)
            .let { CouponAdminV1Dto.CouponTemplateResponse.from(it) }
            .let { ApiResponse.success(it) }
    }

    @PutMapping("/{couponId}")
    fun updateCouponTemplate(
        @PathVariable couponId: Long,
        @RequestBody request: CouponAdminV1Dto.UpdateCouponTemplateRequest,
    ): ApiResponse<CouponAdminV1Dto.CouponTemplateResponse> {
        return couponTemplateService.update(couponId, request.name, request.value, request.minOrderAmount, request.expiredAt)
            .let { CouponAdminV1Dto.CouponTemplateResponse.from(it) }
            .let { ApiResponse.success(it) }
    }

    @DeleteMapping("/{couponId}")
    fun deleteCouponTemplate(
        @PathVariable couponId: Long,
    ): ApiResponse<Unit> {
        couponTemplateService.delete(couponId)
        return ApiResponse.success(Unit)
    }

    @GetMapping("/{couponId}/issues")
    fun getIssuedCoupons(
        @PathVariable couponId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ApiResponse<*> {
        val pageable = PageRequest.of(page, size)
        return issuedCouponService.getIssuedCouponsByTemplateId(couponId, pageable)
            .map { CouponAdminV1Dto.IssuedCouponResponse.from(it) }
            .let { ApiResponse.success(it) }
    }
}
