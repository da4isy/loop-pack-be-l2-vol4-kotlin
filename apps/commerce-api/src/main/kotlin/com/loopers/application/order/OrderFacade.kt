package com.loopers.application.order

import com.loopers.domain.brand.BrandService
import com.loopers.domain.coupon.CouponTemplateService
import com.loopers.domain.coupon.IssuedCouponService
import com.loopers.domain.order.OrderCreationService
import com.loopers.domain.order.OrderService
import com.loopers.domain.order.PaymentClient
import com.loopers.domain.product.ProductService
import com.loopers.infrastructure.product.ProductCacheManager
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
    private val productCacheManager: ProductCacheManager,
) {

    /**
     * 상품 조회 → 쿠폰 검증 → 주문 생성 + 재고 차감 → 결제 → 저장
     */
    @Transactional
    fun createOrder(userId: Long, items: List<OrderItemCommand>, couponId: Long?): OrderDetailInfo {
        val products = items.map { productService.getProductWithLock(it.productId) }
        val brands = brandService.getBrandsByIds(products.map { it.brandId }.distinct())

        val order = orderCreationService.buildOrder(
            userId = userId,
            products = products,
            brands = brands,
            quantities = items.map { it.quantity },
        )

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

        paymentClient.pay(order.totalPrice).ensureSuccess()

        val savedOrder = orderService.createOrder(order)

        items.forEach { productCacheManager.evictDetail(it.productId) }

        return OrderDetailInfo.from(savedOrder)
    }
}
