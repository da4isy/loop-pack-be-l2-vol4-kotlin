package com.loopers.interfaces.api.order

import com.loopers.application.order.OrderFacade
import com.loopers.application.order.OrderInfo
import com.loopers.domain.order.OrderService
import com.loopers.domain.user.UserService
import com.loopers.interfaces.api.ApiResponse
import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType
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
    private val orderService: OrderService,
    private val userService: UserService,
) : OrderV1ApiSpec {

    @PostMapping
    override fun createOrder(
        @RequestHeader("X-Loopers-LoginId") loginId: String,
        @RequestHeader("X-Loopers-LoginPw") password: String,
        @RequestBody request: OrderV1Dto.CreateOrderRequest,
    ): ApiResponse<OrderV1Dto.OrderDetailResponse> {
        val user = userService.getMe(loginId, password)
        return orderFacade.createOrder(user.id, request.toCommands())
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
        val user = userService.getMe(loginId, password)
        val zoneId = ZoneId.of("Asia/Seoul")
        val startZdt = startAt?.let { LocalDate.parse(it).atStartOfDay(zoneId) }
        val endZdt = endAt?.let { LocalDate.parse(it).plusDays(1).atStartOfDay(zoneId).minusNanos(1) }
        val pageable = PageRequest.of(page, size)

        return orderService.getOrdersByUserId(user.id, startZdt, endZdt, pageable)
            .map { OrderInfo.from(it) }
            .map { OrderV1Dto.OrderResponse.from(it) }
            .let { ApiResponse.success(it) }
    }

    @GetMapping("/{orderId}")
    override fun getOrderDetail(
        @RequestHeader("X-Loopers-LoginId") loginId: String,
        @RequestHeader("X-Loopers-LoginPw") password: String,
        @PathVariable orderId: Long,
    ): ApiResponse<OrderV1Dto.OrderDetailResponse> {
        val user = userService.getMe(loginId, password)
        val order = orderService.getOrder(orderId)
        if (order.userId != user.id) {
            throw CoreException(
                errorType = ErrorType.NOT_FOUND,
                customMessage = "존재하지 않는 주문입니다.",
            )
        }
        return com.loopers.application.order.OrderDetailInfo.from(order)
            .let { OrderV1Dto.OrderDetailResponse.from(it) }
            .let { ApiResponse.success(it) }
    }
}
