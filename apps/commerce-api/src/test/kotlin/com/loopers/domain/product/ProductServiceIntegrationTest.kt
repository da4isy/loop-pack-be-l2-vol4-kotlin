package com.loopers.domain.product

import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType
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
import com.ninjasquad.springmockk.MockkBean

@ContextConfiguration(classes = [ProductService::class])
@ExtendWith(SpringExtension::class)
class ProductServiceIntegrationTest @Autowired constructor(
    private val productService: ProductService,
) {

    @MockkBean
    lateinit var productRepository: ProductRepository

    private fun createProduct(
        name: String = "테스트 상품",
        price: Long = 10000,
        stock: Long = 100,
        brandId: Long = 1L,
    ) = ProductModel(name = name, price = price, stock = stock, brandId = brandId)

    @Nested
    inner class GetProduct {

        @Test
        fun success_whenProductExists() {
            val product = createProduct()
            every { productRepository.findById(1L) } returns product

            val result = productService.getProduct(1L)
            result.name shouldBe "테스트 상품"
        }

        @Test
        fun throw_whenProductNotFound() {
            every { productRepository.findById(999L) } returns null

            shouldThrow<CoreException> { productService.getProduct(999L) }
                .errorType shouldBe ErrorType.NOT_FOUND
        }

        @Test
        fun throw_whenProductIsDeleted() {
            val product = createProduct()
            product.delete()
            every { productRepository.findById(1L) } returns product

            shouldThrow<CoreException> { productService.getProduct(1L) }
                .errorType shouldBe ErrorType.NOT_FOUND
        }
    }

    @Nested
    inner class GetProducts {

        @Test
        fun returnsPageOfProducts() {
            val products = listOf(createProduct(name = "상품A"), createProduct(name = "상품B"))
            val pageable = PageRequest.of(0, 20)
            every { productRepository.findAll(null, ProductSortType.LATEST, pageable) } returns
                PageImpl(products, pageable, 2)

            val result = productService.getProducts(null, ProductSortType.LATEST, pageable)
            result.content.size shouldBe 2
            result.totalElements shouldBe 2
        }

        @Test
        fun returnsFilteredByBrandId() {
            val products = listOf(createProduct(brandId = 1L))
            val pageable = PageRequest.of(0, 20)
            every { productRepository.findAll(1L, ProductSortType.LATEST, pageable) } returns
                PageImpl(products, pageable, 1)

            val result = productService.getProducts(1L, ProductSortType.LATEST, pageable)
            result.content.size shouldBe 1
        }
    }
}
