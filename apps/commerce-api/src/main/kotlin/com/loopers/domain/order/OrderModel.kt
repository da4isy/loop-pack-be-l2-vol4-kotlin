package com.loopers.domain.order

import com.loopers.domain.BaseEntity
import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "orders")
class OrderModel(
    userId: Long,
    totalPrice: Long = 0,
) : BaseEntity() {

    @Column(name = "user_id", nullable = false)
    var userId: Long = userId
        protected set

    @Column(name = "total_price", nullable = false)
    var totalPrice: Long = totalPrice
        protected set

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true)
    val orderItems: MutableList<OrderItemModel> = mutableListOf()

    fun addItem(item: OrderItemModel) {
        orderItems.add(item)
        item.order = this
    }

    fun calculateTotalPrice(): Long = orderItems.sumOf { it.itemTotalPrice() }

    companion object {
        fun create(userId: Long, items: List<OrderItemModel>): OrderModel {
            if (items.isEmpty()) {
                throw CoreException(
                    errorType = ErrorType.BAD_REQUEST,
                    customMessage = "주문 항목이 비어있습니다.",
                )
            }
            val order = OrderModel(userId = userId)
            items.forEach { order.addItem(it) }
            order.totalPrice = order.calculateTotalPrice()
            return order
        }
    }
}
