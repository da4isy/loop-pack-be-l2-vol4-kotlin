package com.loopers.application.order

import com.loopers.domain.order.OrderModel
import java.time.ZonedDateTime

data class OrderInfo(
    val orderId: Long,
    val totalPrice: Long,
    val itemCount: Int,
    val createdAt: ZonedDateTime,
) {
    companion object {
        fun from(order: OrderModel): OrderInfo {
            return OrderInfo(
                orderId = order.id,
                totalPrice = order.totalPrice,
                itemCount = order.orderItems.size,
                createdAt = order.createdAt,
            )
        }
    }
}
