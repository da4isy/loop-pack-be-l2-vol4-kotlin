package com.loopers.application.order

import com.loopers.domain.brand.BrandService
import com.loopers.domain.coupon.CouponTemplateService
import com.loopers.domain.coupon.IssuedCouponService
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
    private val issuedCouponService: IssuedCouponService,
    private val couponTemplateService: CouponTemplateService,
) {

    /**
     * 상품 조회 → 쿠폰 검증 → 주문 생성 + 재고 차감 → 결제 → 저장
     */
    @Transactional
    fun createOrder(userId: Long, items: List<OrderItemCommand>, couponId: Long?): OrderDetailInfo {
        // 1. 상품 조회 (비관적 락 — 재고 차감 동시성 방지) · 브랜드 조회
        val products = items.map { productService.getProductWithLock(it.productId) }
        val brands = brandService.getBrandsByIds(products.map { it.brandId }.distinct())

        // 2. 주문 생성 + 재고 차감 (도메인 서비스)
        val order = orderCreationService.buildOrder(
            userId = userId,
            products = products,
            brands = brands,
            quantities = items.map { it.quantity },
        )

        // 3. 쿠폰 검증 + 할인 적용
        var discountAmount = 0L
        if (couponId != null) {
            val issuedCoupon = issuedCouponService.getById(couponId)
            issuedCoupon.validateOwner(userId)

            val template = couponTemplateService.getById(issuedCoupon.couponTemplateId)
            val originalPrice = order.calculateTotalPrice()
            template.validateUsable(originalPrice)

            discountAmount = template.calculateDiscount(originalPrice)
            issuedCoupon.use()

            order.applyCoupon(couponId = issuedCoupon.id, discountAmount = discountAmount)
        }

        // 4. 결제
        val paymentResult = paymentClient.pay(order.totalPrice)
        if (!paymentResult.success) {
            throw CoreException(
                errorType = ErrorType.INTERNAL_ERROR,
                customMessage = "결제에 실패했습니다.",
            )
        }

        // 5. 저장
        val savedOrder = orderService.createOrder(order)
        return OrderDetailInfo.from(savedOrder)
    }
}
