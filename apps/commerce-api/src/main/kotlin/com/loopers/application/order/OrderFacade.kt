package com.loopers.application.order

import com.loopers.domain.brand.BrandService
import com.loopers.domain.order.OrderItemModel
import com.loopers.domain.order.OrderModel
import com.loopers.domain.order.OrderService
import com.loopers.domain.order.PaymentClient
import com.loopers.domain.product.ProductService
import com.loopers.domain.user.UserService
import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime

@Component
class OrderFacade(
    private val orderService: OrderService,
    private val productService: ProductService,
    private val brandService: BrandService,
    private val userService: UserService,
    private val paymentClient: PaymentClient,
) {

    @Transactional
    fun createOrder(loginId: String, password: String, items: List<OrderItemCommand>): OrderDetailInfo {
        val user = userService.getMe(loginId, password)

        val products = items.map { item ->
            productService.getProduct(item.productId)
        }
        val brandIds = products.map { it.brandId }.distinct()
        val brands = brandService.getBrandsByIds(brandIds)

        val orderItems = items.mapIndexed { index, item ->
            val product = products[index]
            val brand = brands[product.brandId]
                ?: throw CoreException(
                    errorType = ErrorType.NOT_FOUND,
                    customMessage = "브랜드 정보를 찾을 수 없습니다.",
                )
            product.decreaseStock(item.quantity)
            OrderItemModel(
                productId = product.id,
                productName = product.name,
                productPrice = product.price,
                brandName = brand.name,
                quantity = item.quantity,
            )
        }

        val order = OrderModel.create(userId = user.id, items = orderItems)

        val paymentResult = paymentClient.pay(order.totalPrice)
        if (!paymentResult.success) {
            throw CoreException(
                errorType = ErrorType.INTERNAL_ERROR,
                customMessage = "결제에 실패했습니다.",
            )
        }

        val savedOrder = orderService.createOrder(order)
        return OrderDetailInfo.from(savedOrder)
    }

    @Transactional(readOnly = true)
    fun getMyOrders(
        loginId: String,
        password: String,
        startAt: ZonedDateTime?,
        endAt: ZonedDateTime?,
        pageable: Pageable,
    ): Page<OrderInfo> {
        val user = userService.getMe(loginId, password)
        return orderService.getOrdersByUserId(user.id, startAt, endAt, pageable)
            .map { OrderInfo.from(it) }
    }

    @Transactional(readOnly = true)
    fun getOrderDetail(loginId: String, password: String, orderId: Long): OrderDetailInfo {
        val user = userService.getMe(loginId, password)
        val order = orderService.getOrder(orderId)
        if (order.userId != user.id) {
            throw CoreException(
                errorType = ErrorType.NOT_FOUND,
                customMessage = "존재하지 않는 주문입니다.",
            )
        }
        return OrderDetailInfo.from(order)
    }
}
