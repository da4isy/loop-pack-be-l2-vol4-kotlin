package com.loopers.domain.product

import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ProductModelTest {

    private fun createProduct(
        name: String = "테스트 상품",
        price: Long = 10000,
        stock: Long = 100,
        brandId: Long = 1L,
    ) = ProductModel(name = name, price = price, stock = stock, brandId = brandId)

    @Nested
    inner class Creation {

        @Test
        fun success_whenValidParams() {
            shouldNotThrow<Exception> { createProduct() }
        }

        @Test
        fun throw_whenNameIsBlank() {
            shouldThrow<CoreException> { createProduct(name = "") }
                .errorType shouldBe ErrorType.BAD_REQUEST
        }

        @Test
        fun throw_whenPriceIsNegative() {
            shouldThrow<CoreException> { createProduct(price = -1) }
                .errorType shouldBe ErrorType.BAD_REQUEST
        }

        @Test
        fun success_whenPriceIsZero() {
            shouldNotThrow<Exception> { createProduct(price = 0) }
        }

        @Test
        fun throw_whenStockIsNegative() {
            shouldThrow<CoreException> { createProduct(stock = -1) }
                .errorType shouldBe ErrorType.BAD_REQUEST
        }
    }

    @Nested
    inner class DecreaseStock {

        @Test
        fun success_whenSufficientStock() {
            val product = createProduct(stock = 10)
            product.decreaseStock(3)
            product.stock shouldBe 7
        }

        @Test
        fun success_whenDecreasingAllStock() {
            val product = createProduct(stock = 5)
            product.decreaseStock(5)
            product.stock shouldBe 0
        }

        @Test
        fun throw_whenInsufficientStock() {
            val product = createProduct(stock = 3)
            shouldThrow<CoreException> { product.decreaseStock(5) }
                .errorType shouldBe ErrorType.BAD_REQUEST
        }

        @Test
        fun throw_whenQuantityIsZero() {
            val product = createProduct(stock = 10)
            shouldThrow<CoreException> { product.decreaseStock(0) }
                .errorType shouldBe ErrorType.BAD_REQUEST
        }

        @Test
        fun throw_whenQuantityIsNegative() {
            val product = createProduct(stock = 10)
            shouldThrow<CoreException> { product.decreaseStock(-1) }
                .errorType shouldBe ErrorType.BAD_REQUEST
        }

        @Test
        fun success_multipleDecrease() {
            val product = createProduct(stock = 10)
            product.decreaseStock(3)
            product.decreaseStock(4)
            product.stock shouldBe 3
        }

        @Test
        fun throw_whenSecondDecreaseExceedsRemaining() {
            val product = createProduct(stock = 10)
            product.decreaseStock(7)
            shouldThrow<CoreException> { product.decreaseStock(5) }
                .errorType shouldBe ErrorType.BAD_REQUEST
        }
    }
}
