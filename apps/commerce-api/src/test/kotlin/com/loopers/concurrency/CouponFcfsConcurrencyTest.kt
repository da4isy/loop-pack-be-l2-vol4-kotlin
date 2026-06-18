package com.loopers.concurrency

import com.loopers.interfaces.api.ApiResponse
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
class CouponFcfsConcurrencyTest @Autowired constructor(
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
        // 유저 20명 생성
        repeat(20) { i ->
            val signup = UserDto.SignupRequest(
                loginId = "fcfsuser$i",
                password = "Password1@@!",
                name = "선착순유저$i",
                birthDate = "1995-01-01",
                email = "fcfs$i@example.com",
            )
            testRestTemplate.exchange(
                "/api/v1/users",
                HttpMethod.POST,
                HttpEntity(signup),
                object : ParameterizedTypeReference<ApiResponse<Any>>() {},
            )
        }

        // 선착순 쿠폰 템플릿 (총 10장만 발급 가능)
        jdbcTemplate.execute(
            "INSERT INTO coupon_templates" +
                " (id, name, type, value, min_order_amount, total_quantity, issued_count, expired_at, created_at, updated_at)" +
                " VALUES (1, '선착순 5000원 할인', 'FIXED', 5000, NULL, 10, 0, '2027-12-31 23:59:59', NOW(), NOW())",
        )
    }

    private fun authHeaders(index: Int) = HttpHeaders().apply {
        add("X-Loopers-LoginId", "fcfsuser$index")
        add("X-Loopers-LoginPw", "Password1@@!")
    }

    @DisplayName("선착순 쿠폰 10장에 20명이 동시 발급 요청하면, 10명만 성공하고 issuedCount는 10이어야 한다")
    @Test
    fun concurrentFcfsCouponIssue_onlyLimitedSucceed() {
        val threadCount = 20
        val readyLatch = CountDownLatch(threadCount)
        val startLatch = CountDownLatch(1)
        val doneLatch = CountDownLatch(threadCount)
        val executor = Executors.newFixedThreadPool(threadCount)
        val successCount = AtomicInteger(0)
        val failCount = AtomicInteger(0)

        repeat(threadCount) { i ->
            executor.submit {
                try {
                    readyLatch.countDown()
                    startLatch.await()

                    val response = testRestTemplate.exchange(
                        "/api/v1/coupons/1/issue",
                        HttpMethod.POST,
                        HttpEntity<Any>(authHeaders(i)),
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
                    doneLatch.countDown()
                }
            }
        }

        readyLatch.await()
        startLatch.countDown()
        doneLatch.await()
        executor.shutdown()

        // 발급된 쿠폰 수 확인
        val issuedCount = jdbcTemplate.queryForObject(
            "SELECT issued_count FROM coupon_templates WHERE id = 1",
            Long::class.java,
        )
        val actualIssuedCoupons = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM issued_coupons WHERE coupon_template_id = 1",
            Long::class.java,
        )

        successCount.get() shouldBe 10
        failCount.get() shouldBe 10
        issuedCount shouldBe 10
        actualIssuedCoupons shouldBe 10
    }

    @DisplayName("수량 무제한 쿠폰(totalQuantity=null)은 모든 요청이 성공해야 한다")
    @Test
    fun concurrentUnlimitedCouponIssue_allSucceed() {
        // 무제한 쿠폰 템플릿
        jdbcTemplate.execute(
            "INSERT INTO coupon_templates" +
                " (id, name, type, value, min_order_amount, total_quantity, issued_count, expired_at, created_at, updated_at)" +
                " VALUES (2, '무제한 10% 할인', 'RATE', 10, NULL, NULL, 0, '2027-12-31 23:59:59', NOW(), NOW())",
        )

        val threadCount = 10
        val readyLatch = CountDownLatch(threadCount)
        val startLatch = CountDownLatch(1)
        val doneLatch = CountDownLatch(threadCount)
        val executor = Executors.newFixedThreadPool(threadCount)
        val successCount = AtomicInteger(0)

        repeat(threadCount) { i ->
            executor.submit {
                try {
                    readyLatch.countDown()
                    startLatch.await()

                    val response = testRestTemplate.exchange(
                        "/api/v1/coupons/2/issue",
                        HttpMethod.POST,
                        HttpEntity<Any>(authHeaders(i)),
                        object : ParameterizedTypeReference<ApiResponse<Any>>() {},
                    )
                    if (response.statusCode.is2xxSuccessful) {
                        successCount.incrementAndGet()
                    }
                } catch (_: Exception) {
                } finally {
                    doneLatch.countDown()
                }
            }
        }

        readyLatch.await()
        startLatch.countDown()
        doneLatch.await()
        executor.shutdown()

        successCount.get() shouldBe 10

        val actualIssuedCoupons = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM issued_coupons WHERE coupon_template_id = 2",
            Long::class.java,
        )
        actualIssuedCoupons shouldBe 10
    }
}
