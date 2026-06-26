package com.loopers.concurrency

import com.loopers.interfaces.api.ApiResponse
import com.loopers.interfaces.api.order.OrderV1Dto
import com.loopers.interfaces.api.user.UserDto
import com.loopers.utils.DatabaseCleanUp
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.jdbc.core.JdbcTemplate
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StockConcurrencyTest @Autowired constructor(
    private val testRestTemplate: TestRestTemplate,
    private val databaseCleanUp: DatabaseCleanUp,
    private val jdbcTemplate: JdbcTemplate,
) {

    @AfterEach
    fun tearDown() {
        databaseCleanUp.truncateAllTables()
    }

    @BeforeEach
    fun setUp() {
        // 유저 10명 생성
        repeat(10) { i ->
            val signup = UserDto.SignupRequest(
                loginId = "user$i",
                password = "Password1@@!",
                name = "유저$i",
                birthDate = "1995-01-01",
                email = "user$i@example.com",
            )
            testRestTemplate.exchange(
                "/api/v1/users",
                HttpMethod.POST,
                HttpEntity(signup),
                object : ParameterizedTypeReference<ApiResponse<Any>>() {},
            )
        }

        // 브랜드 + 재고 10개 상품
        jdbcTemplate.execute(
            "INSERT INTO brands (id, name, created_at, updated_at)" +
                " VALUES (1, '나이키', NOW(), NOW())",
        )
        jdbcTemplate.execute(
            "INSERT INTO products (id, name, price, stock, brand_id, like_count, created_at, updated_at)" +
                " VALUES (1, '에어맥스', 129000, 10, 1, 0, NOW(), NOW())",
        )
    }

    private fun authHeaders(index: Int) = HttpHeaders().apply {
        add("X-Loopers-LoginId", "user$index")
        add("X-Loopers-LoginPw", "Password1@@!")
    }

    @DisplayName("동시에 10명이 재고 10개인 상품을 1개씩 주문하면, 재고가 0이 되어야 한다")
    @Test
    fun concurrentOrders_stockShouldBeZero() {
        val threadCount = 10
        val latch = CountDownLatch(threadCount)
        val executor = Executors.newFixedThreadPool(threadCount)
        val successCount = AtomicInteger(0)
        val failCount = AtomicInteger(0)

        repeat(threadCount) { i ->
            executor.submit {
                try {
                    val request = OrderV1Dto.CreateOrderRequest(
                        items = listOf(
                            OrderV1Dto.CreateOrderRequest.OrderItemRequest(productId = 1, quantity = 1),
                        ),
                    )
                    val response = testRestTemplate.exchange(
                        "/api/v1/orders",
                        HttpMethod.POST,
                        HttpEntity(request, authHeaders(i)),
                        object : ParameterizedTypeReference<ApiResponse<Any>>() {},
                    )
                    if (response.statusCode.is2xxSuccessful) {
                        successCount.incrementAndGet()
                    } else {
                        failCount.incrementAndGet()
                    }
                } catch (e: Exception) {
                    failCount.incrementAndGet()
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()
        executor.shutdown()

        val stock = jdbcTemplate.queryForObject(
            "SELECT stock FROM products WHERE id = 1",
            Long::class.java,
        )

        stock shouldBe 0
        successCount.get() shouldBe 10
    }

    @DisplayName("재고 5개인 상품에 10명이 동시 주문하면, 5명만 성공하고 재고는 0이어야 한다")
    @Test
    fun concurrentOrders_insufficientStock_partialSuccess() {
        // 재고를 5개로 변경
        jdbcTemplate.execute("UPDATE products SET stock = 5 WHERE id = 1")

        val threadCount = 10
        val latch = CountDownLatch(threadCount)
        val executor = Executors.newFixedThreadPool(threadCount)
        val successCount = AtomicInteger(0)
        val failCount = AtomicInteger(0)

        repeat(threadCount) { i ->
            executor.submit {
                try {
                    val request = OrderV1Dto.CreateOrderRequest(
                        items = listOf(
                            OrderV1Dto.CreateOrderRequest.OrderItemRequest(productId = 1, quantity = 1),
                        ),
                    )
                    val response = testRestTemplate.exchange(
                        "/api/v1/orders",
                        HttpMethod.POST,
                        HttpEntity(request, authHeaders(i)),
                        object : ParameterizedTypeReference<ApiResponse<Any>>() {},
                    )
                    if (response.statusCode.is2xxSuccessful) {
                        successCount.incrementAndGet()
                    } else {
                        failCount.incrementAndGet()
                    }
                } catch (e: Exception) {
                    failCount.incrementAndGet()
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()
        executor.shutdown()

        val stock = jdbcTemplate.queryForObject(
            "SELECT stock FROM products WHERE id = 1",
            Long::class.java,
        )

        stock shouldBe 0
        successCount.get() shouldBe 5
        failCount.get() shouldBe 5
    }
}
