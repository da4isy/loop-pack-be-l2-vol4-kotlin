package com.loopers.interfaces.api.coupon

import com.loopers.application.coupon.CouponFacade
import com.loopers.domain.coupon.IssuedCouponService
import com.loopers.domain.user.UserService
import com.loopers.interfaces.api.ApiResponse
import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class CouponV1Controller(
    private val issuedCouponService: IssuedCouponService,
    private val couponFacade: CouponFacade,
    private val userService: UserService,
) {

    @PostMapping("/api/v1/coupons/{couponId}/issue")
    fun issueCoupon(
        @RequestHeader("X-Loopers-LoginId") loginId: String,
        @RequestHeader("X-Loopers-LoginPw") password: String,
        @PathVariable couponId: Long,
    ): ApiResponse<CouponAdminV1Dto.IssuedCouponResponse> {
        val user = userService.getMe(loginId, password)
        val issued = issuedCouponService.issue(user.id, couponId)
        return ApiResponse.success(CouponAdminV1Dto.IssuedCouponResponse.from(issued))
    }

    @GetMapping("/api/v1/users/me/coupons")
    fun getMyCoupons(
        @RequestHeader("X-Loopers-LoginId") loginId: String,
        @RequestHeader("X-Loopers-LoginPw") password: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ApiResponse<*> {
        val user = userService.getMe(loginId, password)
        val pageable = PageRequest.of(page, size)
        return couponFacade.getMyIssuedCoupons(user.id, pageable)
            .let { ApiResponse.success(it) }
    }
}
