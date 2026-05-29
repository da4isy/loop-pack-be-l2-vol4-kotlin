package com.loopers.application.order

import com.loopers.domain.order.OrderItemModel
import com.loopers.domain.order.OrderModel
import java.time.ZonedDateTime

data class OrderDetailInfo(
    val orderId: Long,
    val totalPrice: Long,
    val createdAt: ZonedDateTime,
    val items: List<OrderItemInfo>,
) {

    data class OrderItemInfo(
        val productId: Long,
        val productName: String,
        val productPrice: Long,
        val brandName: String,
        val quantity: Long,
    ) {
        companion object {
            fun from(item: OrderItemModel): OrderItemInfo {
                return OrderItemInfo(
                    productId = item.productId,
                    productName = item.productName,
                    productPrice = item.productPrice,
                    brandName = item.brandName,
                    quantity = item.quantity,
                )
            }
        }
    }

    companion object {
        fun from(order: OrderModel): OrderDetailInfo {
            return OrderDetailInfo(
                orderId = order.id,
                totalPrice = order.totalPrice,
                createdAt = order.createdAt,
                items = order.orderItems.map { OrderItemInfo.from(it) },
            )
        }
    }
}
