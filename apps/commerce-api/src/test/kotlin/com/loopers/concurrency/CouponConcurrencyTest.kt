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
class CouponConcurrencyTest @Autowired constructor(
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
        // 유저 1명 생성
        val signup = UserDto.SignupRequest(
            loginId = "couponuser",
            password = "Password1@@!",
            name = "쿠폰유저",
            birthDate = "1995-01-01",
            email = "coupon@example.com",
        )
        testRestTemplate.exchange(
            "/api/v1/users",
            HttpMethod.POST,
            HttpEntity(signup),
            object : ParameterizedTypeReference<ApiResponse<Any>>() {},
        )

        // 브랜드 + 재고 충분한 상품
        jdbcTemplate.execute(
            "INSERT INTO brands (id, name, created_at, updated_at)" +
                " VALUES (1, '나이키', NOW(), NOW())",
        )
        jdbcTemplate.execute(
            "INSERT INTO products (id, name, price, stock, brand_id, created_at, updated_at)" +
                " VALUES (1, '에어맥스', 100000, 100, 1, NOW(), NOW())",
        )

        // 쿠폰 템플릿 + 발급 쿠폰 1장
        jdbcTemplate.execute(
            "INSERT INTO coupon_templates (id, name, type, value, min_order_amount, expired_at, created_at, updated_at)" +
                " VALUES (1, '10% 할인', 'RATE', 10, NULL, '2027-12-31 23:59:59', NOW(), NOW())",
        )
        jdbcTemplate.execute(
            "INSERT INTO issued_coupons (id, coupon_template_id, user_id, status, created_at, updated_at)" +
                " VALUES (1, 1, 1, 'AVAILABLE', NOW(), NOW())",
        )
    }

    private fun authHeaders() = HttpHeaders().apply {
        add("X-Loopers-LoginId", "couponuser")
        add("X-Loopers-LoginPw", "Password1@@!")
    }

    @DisplayName("동일한 쿠폰으로 여러 기기에서 동시에 주문해도, 쿠폰은 단 한번만 사용되어야 한다")
    @Test
    fun concurrentOrdersWithSameCoupon_onlyOneSucceeds() {
        val threadCount = 5
        val latch = CountDownLatch(threadCount)
        val executor = Executors.newFixedThreadPool(threadCount)
        val successCount = AtomicInteger(0)
        val failCount = AtomicInteger(0)

        repeat(threadCount) {
            executor.submit {
                try {
                    val request = OrderV1Dto.CreateOrderRequest(
                        items = listOf(
                            OrderV1Dto.CreateOrderRequest.OrderItemRequest(productId = 1, quantity = 1),
                        ),
                        couponId = 1,
                    )
                    val response = testRestTemplate.exchange(
                        "/api/v1/orders",
                        HttpMethod.POST,
                        HttpEntity(request, authHeaders()),
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

        // 쿠폰은 1번만 사용되어야 한다
        val usedCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM issued_coupons WHERE id = 1 AND status = 'USED'",
            Long::class.java,
        )
        val orderCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM orders WHERE coupon_id = 1",
            Long::class.java,
        )

        successCount.get() shouldBe 1
        usedCount shouldBe 1
        orderCount shouldBe 1
    }
}
