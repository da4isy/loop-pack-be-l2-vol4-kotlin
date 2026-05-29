package com.loopers.interfaces.api.order

import com.loopers.application.order.OrderFacade
import com.loopers.interfaces.api.ApiResponse
import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.ZoneId

@RestController
@RequestMapping("/api/v1/orders")
class OrderV1Controller(
    private val orderFacade: OrderFacade,
) : OrderV1ApiSpec {

    @PostMapping
    override fun createOrder(
        @RequestHeader("X-Loopers-LoginId") loginId: String,
        @RequestHeader("X-Loopers-LoginPw") password: String,
        @RequestBody request: OrderV1Dto.CreateOrderRequest,
    ): ApiResponse<OrderV1Dto.OrderDetailResponse> {
        return orderFacade.createOrder(loginId, password, request.toCommands())
            .let { OrderV1Dto.OrderDetailResponse.from(it) }
            .let { ApiResponse.success(it) }
    }

    @GetMapping
    override fun getMyOrders(
        @RequestHeader("X-Loopers-LoginId") loginId: String,
        @RequestHeader("X-Loopers-LoginPw") password: String,
        @RequestParam(required = false) startAt: String?,
        @RequestParam(required = false) endAt: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ApiResponse<*> {
        val zoneId = ZoneId.of("Asia/Seoul")
        val startZdt = startAt?.let { LocalDate.parse(it).atStartOfDay(zoneId) }
        val endZdt = endAt?.let { LocalDate.parse(it).plusDays(1).atStartOfDay(zoneId).minusNanos(1) }
        val pageable = PageRequest.of(page, size)

        return orderFacade.getMyOrders(loginId, password, startZdt, endZdt, pageable)
            .map { OrderV1Dto.OrderResponse.from(it) }
            .let { ApiResponse.success(it) }
    }

    @GetMapping("/{orderId}")
    override fun getOrderDetail(
        @RequestHeader("X-Loopers-LoginId") loginId: String,
        @RequestHeader("X-Loopers-LoginPw") password: String,
        @PathVariable orderId: Long,
    ): ApiResponse<OrderV1Dto.OrderDetailResponse> {
        return orderFacade.getOrderDetail(loginId, password, orderId)
            .let { OrderV1Dto.OrderDetailResponse.from(it) }
            .let { ApiResponse.success(it) }
    }
}
