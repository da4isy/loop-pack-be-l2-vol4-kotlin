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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LikeConcurrencyTest @Autowired constructor(
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
                loginId = "likeuser$i",
                password = "Password1@@!",
                name = "좋아요유저$i",
                birthDate = "1995-01-01",
                email = "like$i@example.com",
            )
            testRestTemplate.exchange(
                "/api/v1/users",
                HttpMethod.POST,
                HttpEntity(signup),
                object : ParameterizedTypeReference<ApiResponse<Any>>() {},
            )
        }

        // 브랜드 + 상품
        jdbcTemplate.execute(
            "INSERT INTO brands (id, name, created_at, updated_at)" +
                " VALUES (1, '나이키', NOW(), NOW())",
        )
        jdbcTemplate.execute(
            "INSERT INTO products (id, name, price, stock, brand_id, like_count, created_at, updated_at)" +
                " VALUES (1, '에어맥스', 129000, 50, 1, 0, NOW(), NOW())",
        )
    }

    private fun authHeaders(index: Int) = HttpHeaders().apply {
        add("X-Loopers-LoginId", "likeuser$index")
        add("X-Loopers-LoginPw", "Password1@@!")
    }

    @DisplayName("동시에 10명이 같은 상품에 좋아요를 누르면, 좋아요 수가 정확히 10이어야 한다")
    @Test
    fun concurrentLikes_countShouldBeExact() {
        val threadCount = 10
        val latch = CountDownLatch(threadCount)
        val executor = Executors.newFixedThreadPool(threadCount)

        repeat(threadCount) { i ->
            executor.submit {
                try {
                    testRestTemplate.exchange(
                        "/api/v1/products/1/likes",
                        HttpMethod.POST,
                        HttpEntity<Any>(authHeaders(i)),
                        object : ParameterizedTypeReference<ApiResponse<Any>>() {},
                    )
                } catch (_: Exception) {
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()
        executor.shutdown()

        val likeCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM likes WHERE product_id = 1",
            Long::class.java,
        )

        likeCount shouldBe 10
    }

    @DisplayName("같은 유저가 동시에 같은 상품에 좋아요를 여러 번 눌러도, 좋아요는 1개만 존재해야 한다")
    @Test
    fun concurrentDuplicateLikes_shouldBeIdempotent() {
        val threadCount = 5
        val latch = CountDownLatch(threadCount)
        val executor = Executors.newFixedThreadPool(threadCount)

        // 같은 유저(index 0)가 동시에 5번 좋아요
        repeat(threadCount) {
            executor.submit {
                try {
                    testRestTemplate.exchange(
                        "/api/v1/products/1/likes",
                        HttpMethod.POST,
                        HttpEntity<Any>(authHeaders(0)),
                        object : ParameterizedTypeReference<ApiResponse<Any>>() {},
                    )
                } catch (_: Exception) {
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()
        executor.shutdown()

        val likeCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM likes WHERE product_id = 1 AND user_id = 1",
            Long::class.java,
        )

        likeCount shouldBe 1
    }
}
