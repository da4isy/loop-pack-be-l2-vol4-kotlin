package com.loopers.application.order

import com.loopers.domain.brand.BrandService
import com.loopers.domain.order.OrderCreationService
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
    private val orderCreationService: OrderCreationService,
    private val paymentClient: PaymentClient,
) {

    /**
     * 상품 조회 → 주문 생성 + 재고 차감 → 결제 → 저장
     */
    @Transactional
    fun createOrder(userId: Long, items: List<OrderItemCommand>): OrderDetailInfo {
        // 1. 상품 · 브랜드 조회
        val products = items.map { productService.getProduct(it.productId) }
        val brands = brandService.getBrandsByIds(products.map { it.brandId }.distinct())

        // 2. 주문 생성 + 재고 차감 (도메인 서비스)
        val order = orderCreationService.buildOrder(
            userId = userId,
            products = products,
            brands = brands,
            quantities = items.map { it.quantity },
        )

        // 3. 결제
        val paymentResult = paymentClient.pay(order.totalPrice)
        if (!paymentResult.success) {
            throw CoreException(
                errorType = ErrorType.INTERNAL_ERROR,
                customMessage = "결제에 실패했습니다.",
            )
        }

        // 4. 저장
        val savedOrder = orderService.createOrder(order)
        return OrderDetailInfo.from(savedOrder)
    }
}
