package com.loopers.domain.order

import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType
import com.ninjasquad.springmockk.MockkBean
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ContextConfiguration(classes = [OrderService::class])
@ExtendWith(SpringExtension::class)
class OrderServiceIntegrationTest @Autowired constructor(
    private val orderService: OrderService,
) {

    @MockkBean
    lateinit var orderRepository: OrderRepository

    private fun createOrder(userId: Long = 1L): OrderModel {
        val item = OrderItemModel(
            productId = 10L,
            productName = "테스트 상품",
            productPrice = 10000,
            brandName = "나이키",
            quantity = 2,
        )
        return OrderModel.create(userId = userId, items = listOf(item))
    }

    @Nested
    inner class CreateOrder {

        @Test
        fun success() {
            val order = createOrder()
            every { orderRepository.save(any()) } returns order

            val result = orderService.createOrder(order)
            result.totalPrice shouldBe 20000
        }
    }

    @Nested
    inner class GetOrder {

        @Test
        fun success_whenOrderExists() {
            val order = createOrder()
            every { orderRepository.findById(1L) } returns order

            val result = orderService.getOrder(1L)
            result.userId shouldBe 1L
        }

        @Test
        fun throw_whenOrderNotFound() {
            every { orderRepository.findById(999L) } returns null

            shouldThrow<CoreException> { orderService.getOrder(999L) }
                .errorType shouldBe ErrorType.NOT_FOUND
        }
    }

    @Nested
    inner class GetOrdersByUserId {

        @Test
        fun returnsPageOfOrders() {
            val orders = listOf(createOrder())
            val pageable = PageRequest.of(0, 20)
            every {
                orderRepository.findAllByUserId(1L, null, null, pageable)
            } returns PageImpl(orders, pageable, 1)

            val result = orderService.getOrdersByUserId(1L, null, null, pageable)
            result.content.size shouldBe 1
            result.totalElements shouldBe 1
        }
    }
}
