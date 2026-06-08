package com.loopers.domain.order

import com.loopers.domain.BaseEntity
import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "order_items")
class OrderItemModel(
    productId: Long,
    productName: String,
    productPrice: Long,
    brandName: String,
    quantity: Long,
) : BaseEntity() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    var order: OrderModel? = null
        internal set

    @Column(name = "product_id", nullable = false)
    var productId: Long = productId
        protected set

    @Column(name = "product_name", nullable = false)
    var productName: String = productName
        protected set

    @Column(name = "product_price", nullable = false)
    var productPrice: Long = productPrice
        protected set

    @Column(name = "brand_name", nullable = false)
    var brandName: String = brandName
        protected set

    @Column(name = "quantity", nullable = false)
    var quantity: Long = quantity
        protected set

    init {
        if (quantity <= 0) {
            throw CoreException(
                errorType = ErrorType.BAD_REQUEST,
                customMessage = "주문 수량은 0보다 커야 합니다.",
            )
        }
    }

    fun itemTotalPrice(): Long = productPrice * quantity
}
