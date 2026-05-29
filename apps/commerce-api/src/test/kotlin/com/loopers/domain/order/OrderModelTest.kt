package com.loopers.domain.order

import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OrderModelTest {

    private fun createItem(
        productId: Long = 1L,
        productName: String = "테스트 상품",
        productPrice: Long = 10000,
        brandName: String = "나이키",
        quantity: Long = 2,
    ) = OrderItemModel(
        productId = productId,
        productName = productName,
        productPrice = productPrice,
        brandName = brandName,
        quantity = quantity,
    )

    @Nested
    inner class OrderItemCreation {

        @Test
        fun success_whenValidQuantity() {
            shouldNotThrow<Exception> { createItem(quantity = 1) }
        }

        @Test
        fun throw_whenQuantityIsZero() {
            shouldThrow<CoreException> { createItem(quantity = 0) }
                .errorType shouldBe ErrorType.BAD_REQUEST
        }

        @Test
        fun throw_whenQuantityIsNegative() {
            shouldThrow<CoreException> { createItem(quantity = -1) }
                .errorType shouldBe ErrorType.BAD_REQUEST
        }
    }

    @Nested
    inner class OrderCreation {

        @Test
        fun success_whenValidItems() {
            val items = listOf(createItem(productPrice = 10000, quantity = 2))
            val order = OrderModel.create(userId = 1L, items = items)

            order.userId shouldBe 1L
            order.orderItems.size shouldBe 1
            order.totalPrice shouldBe 20000
        }

        @Test
        fun throw_whenItemsEmpty() {
            shouldThrow<CoreException> { OrderModel.create(userId = 1L, items = emptyList()) }
                .errorType shouldBe ErrorType.BAD_REQUEST
        }
    }

    @Nested
    inner class CalculateTotalPrice {

        @Test
        fun calculatesSum_withSingleItem() {
            val items = listOf(createItem(productPrice = 5000, quantity = 3))
            val order = OrderModel.create(userId = 1L, items = items)

            order.calculateTotalPrice() shouldBe 15000
        }

        @Test
        fun calculatesSum_withMultipleItems() {
            val items = listOf(
                createItem(productId = 1L, productPrice = 10000, quantity = 2),
                createItem(productId = 2L, productPrice = 5000, quantity = 3),
            )
            val order = OrderModel.create(userId = 1L, items = items)

            order.calculateTotalPrice() shouldBe 35000
        }
    }

    @Nested
    inner class ItemTotalPrice {

        @Test
        fun calculatesCorrectly() {
            val item = createItem(productPrice = 12000, quantity = 3)
            item.itemTotalPrice() shouldBe 36000
        }
    }

    @Nested
    inner class Snapshot {

        @Test
        fun preservesProductInfoAtOrderTime() {
            val item = createItem(
                productName = "에어맥스 90",
                productPrice = 129000,
                brandName = "나이키",
                quantity = 1,
            )
            val order = OrderModel.create(userId = 1L, items = listOf(item))

            val snapshot = order.orderItems[0]
            snapshot.productName shouldBe "에어맥스 90"
            snapshot.productPrice shouldBe 129000
            snapshot.brandName shouldBe "나이키"
        }
    }
}
