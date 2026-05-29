package com.loopers.interfaces.api.like

import com.loopers.interfaces.api.ApiResponse
import com.loopers.interfaces.api.user.UserDto
import com.loopers.utils.DatabaseCleanUp
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LikeV1ApiE2ETest @Autowired constructor(
    private val testRestTemplate: TestRestTemplate,
    private val databaseCleanUp: DatabaseCleanUp,
    private val jdbcTemplate: JdbcTemplate,
) {

    companion object {
        private val SIGNUP_REQUEST = UserDto.SignupRequest(
            loginId = "da4isy",
            password = "Daisyyyy1@@!",
            name = "정다희",
            birthDate = "1995-12-03",
            email = "dahee.jeong123@example.com",
        )
    }

    @AfterEach
    fun tearDown() {
        databaseCleanUp.truncateAllTables()
    }

    @BeforeEach
    fun setUp() {
        // 유저 생성
        val signupType = object : ParameterizedTypeReference<ApiResponse<UserDto.SignupResponse>>() {}
        testRestTemplate.exchange(
            "/api/v1/users",
            HttpMethod.POST,
            HttpEntity(SIGNUP_REQUEST),
            signupType,
        )

        // 브랜드 + 상품 직접 INSERT (Brand/Product 생성 API가 없으므로)
        jdbcTemplate.execute(
            "INSERT INTO brands (id, name, created_at, updated_at)" +
                " VALUES (1, '나이키', NOW(), NOW())",
        )
        jdbcTemplate.execute(
            "INSERT INTO products (id, name, price, stock, brand_id, created_at, updated_at)" +
                " VALUES (1, '에어맥스', 129000, 50, 1, NOW(), NOW())",
        )
        jdbcTemplate.execute(
            "INSERT INTO products (id, name, price, stock, brand_id, created_at, updated_at)" +
                " VALUES (2, '에어포스', 109000, 30, 1, NOW(), NOW())",
        )
    }

    private fun authHeaders() = HttpHeaders().apply {
        add("X-Loopers-LoginId", "da4isy")
        add("X-Loopers-LoginPw", "Daisyyyy1@@!")
    }

    @Nested
    inner class LikeProduct {

        @Test
        fun success_whenValidRequest() {
            val responseType = object : ParameterizedTypeReference<ApiResponse<Any>>() {}
            val response = testRestTemplate.exchange(
                "/api/v1/products/1/likes",
                HttpMethod.POST,
                HttpEntity<Any>(authHeaders()),
                responseType,
            )

            response.statusCode shouldBe HttpStatus.OK
        }

        @Test
        fun idempotent_whenAlreadyLiked() {
            val responseType = object : ParameterizedTypeReference<ApiResponse<Any>>() {}

            // 첫 번째 좋아요
            testRestTemplate.exchange(
                "/api/v1/products/1/likes",
                HttpMethod.POST,
                HttpEntity<Any>(authHeaders()),
                responseType,
            )

            // 두 번째 좋아요 (멱등)
            val response = testRestTemplate.exchange(
                "/api/v1/products/1/likes",
                HttpMethod.POST,
                HttpEntity<Any>(authHeaders()),
                responseType,
            )

            response.statusCode shouldBe HttpStatus.OK
        }

        @Test
        fun returnsNotFound_whenProductNotExists() {
            val responseType = object : ParameterizedTypeReference<ApiResponse<Any>>() {}
            val response = testRestTemplate.exchange(
                "/api/v1/products/999/likes",
                HttpMethod.POST,
                HttpEntity<Any>(authHeaders()),
                responseType,
            )

            response.statusCode shouldBe HttpStatus.NOT_FOUND
        }

        @Test
        fun returnsUnauthorized_whenInvalidCredentials() {
            val badHeaders = HttpHeaders().apply {
                add("X-Loopers-LoginId", "da4isy")
                add("X-Loopers-LoginPw", "wrong-password")
            }
            val responseType = object : ParameterizedTypeReference<ApiResponse<Any>>() {}
            val response = testRestTemplate.exchange(
                "/api/v1/products/1/likes",
                HttpMethod.POST,
                HttpEntity<Any>(badHeaders),
                responseType,
            )

            response.statusCode shouldBe HttpStatus.UNAUTHORIZED
        }
    }

    @Nested
    inner class UnlikeProduct {

        @Test
        fun success_whenLiked() {
            val responseType = object : ParameterizedTypeReference<ApiResponse<Any>>() {}

            // 좋아요 등록
            testRestTemplate.exchange(
                "/api/v1/products/1/likes",
                HttpMethod.POST,
                HttpEntity<Any>(authHeaders()),
                responseType,
            )

            // 좋아요 취소
            val response = testRestTemplate.exchange(
                "/api/v1/products/1/likes",
                HttpMethod.DELETE,
                HttpEntity<Any>(authHeaders()),
                responseType,
            )

            response.statusCode shouldBe HttpStatus.OK
        }

        @Test
        fun idempotent_whenNotLiked() {
            val responseType = object : ParameterizedTypeReference<ApiResponse<Any>>() {}
            val response = testRestTemplate.exchange(
                "/api/v1/products/1/likes",
                HttpMethod.DELETE,
                HttpEntity<Any>(authHeaders()),
                responseType,
            )

            response.statusCode shouldBe HttpStatus.OK
        }
    }

    @Nested
    inner class GetMyLikes {

        @Test
        fun returnsMyLikes() {
            val likeType = object : ParameterizedTypeReference<ApiResponse<Any>>() {}

            // 좋아요 2개 등록
            testRestTemplate.exchange(
                "/api/v1/products/1/likes",
                HttpMethod.POST,
                HttpEntity<Any>(authHeaders()),
                likeType,
            )
            testRestTemplate.exchange(
                "/api/v1/products/2/likes",
                HttpMethod.POST,
                HttpEntity<Any>(authHeaders()),
                likeType,
            )

            // 내 좋아요 목록 조회
            val responseType = object : ParameterizedTypeReference<ApiResponse<Any>>() {}
            val response = testRestTemplate.exchange(
                "/api/v1/users/me/likes",
                HttpMethod.GET,
                HttpEntity<Any>(authHeaders()),
                responseType,
            )

            response.statusCode shouldBe HttpStatus.OK
        }

        @Test
        fun returnsEmpty_whenNoLikes() {
            val responseType = object : ParameterizedTypeReference<ApiResponse<Any>>() {}
            val response = testRestTemplate.exchange(
                "/api/v1/users/me/likes",
                HttpMethod.GET,
                HttpEntity<Any>(authHeaders()),
                responseType,
            )

            response.statusCode shouldBe HttpStatus.OK
        }
    }
}
