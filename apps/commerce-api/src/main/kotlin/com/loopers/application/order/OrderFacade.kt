package com.loopers.application.order

import com.loopers.domain.brand.BrandService
import com.loopers.domain.order.OrderItemModel
import com.loopers.domain.order.OrderModel
import com.loopers.domain.order.OrderService
import com.loopers.domain.order.PaymentClient
import com.loopers.domain.product.ProductService
import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class OrderFacade(
    private val orderService: OrderService,
    private val productService: ProductService,
    private val brandService: BrandService,
    private val paymentClient: PaymentClient,
) {

    @Transactional
    fun createOrder(userId: Long, items: List<OrderItemCommand>): OrderDetailInfo {
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

        val order = OrderModel.create(userId = userId, items = orderItems)

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
}
