package com.loopers.interfaces.api.order

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
class OrderV1ApiE2ETest @Autowired constructor(
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
        val signupType = object : ParameterizedTypeReference<ApiResponse<UserDto.SignupResponse>>() {}
        testRestTemplate.exchange(
            "/api/v1/users",
            HttpMethod.POST,
            HttpEntity(SIGNUP_REQUEST),
            signupType,
        )

        jdbcTemplate.execute(
            "INSERT INTO brands (id, name, created_at, updated_at)" +
                " VALUES (1, '나이키', NOW(), NOW())",
        )
        jdbcTemplate.execute(
            "INSERT INTO products (id, name, price, stock, brand_id, like_count, created_at, updated_at)" +
                " VALUES (1, '에어맥스', 129000, 50, 1, 0, NOW(), NOW())",
        )
        jdbcTemplate.execute(
            "INSERT INTO products (id, name, price, stock, brand_id, like_count, created_at, updated_at)" +
                " VALUES (2, '에어포스', 109000, 30, 1, 0, NOW(), NOW())",
        )
    }

    private fun authHeaders() = HttpHeaders().apply {
        add("X-Loopers-LoginId", "da4isy")
        add("X-Loopers-LoginPw", "Daisyyyy1@@!")
    }

    private fun createOrderRequest(
        items: List<OrderV1Dto.CreateOrderRequest.OrderItemRequest>,
    ) = OrderV1Dto.CreateOrderRequest(items = items)

    @Nested
    inner class CreateOrder {

        @Test
        fun success_whenValidRequest() {
            val request = createOrderRequest(
                listOf(
                    OrderV1Dto.CreateOrderRequest.OrderItemRequest(productId = 1, quantity = 2),
                    OrderV1Dto.CreateOrderRequest.OrderItemRequest(productId = 2, quantity = 1),
                ),
            )
            val responseType = object : ParameterizedTypeReference<ApiResponse<OrderV1Dto.OrderDetailResponse>>() {}
            val response = testRestTemplate.exchange(
                "/api/v1/orders",
                HttpMethod.POST,
                HttpEntity(request, authHeaders()),
                responseType,
            )

            response.statusCode shouldBe HttpStatus.OK
            response.body?.data?.totalPrice shouldBe (129000 * 2 + 109000 * 1)
            response.body?.data?.items?.size shouldBe 2
        }

        @Test
        fun returnsBadRequest_whenInsufficientStock() {
            val request = createOrderRequest(
                listOf(
                    OrderV1Dto.CreateOrderRequest.OrderItemRequest(productId = 1, quantity = 999),
                ),
            )
            val responseType = object : ParameterizedTypeReference<ApiResponse<Any>>() {}
            val response = testRestTemplate.exchange(
                "/api/v1/orders",
                HttpMethod.POST,
                HttpEntity(request, authHeaders()),
                responseType,
            )

            response.statusCode shouldBe HttpStatus.BAD_REQUEST
        }

        @Test
        fun returnsNotFound_whenProductNotExists() {
            val request = createOrderRequest(
                listOf(
                    OrderV1Dto.CreateOrderRequest.OrderItemRequest(productId = 999, quantity = 1),
                ),
            )
            val responseType = object : ParameterizedTypeReference<ApiResponse<Any>>() {}
            val response = testRestTemplate.exchange(
                "/api/v1/orders",
                HttpMethod.POST,
                HttpEntity(request, authHeaders()),
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
            val request = createOrderRequest(
                listOf(
                    OrderV1Dto.CreateOrderRequest.OrderItemRequest(productId = 1, quantity = 1),
                ),
            )
            val responseType = object : ParameterizedTypeReference<ApiResponse<Any>>() {}
            val response = testRestTemplate.exchange(
                "/api/v1/orders",
                HttpMethod.POST,
                HttpEntity(request, badHeaders),
                responseType,
            )

            response.statusCode shouldBe HttpStatus.UNAUTHORIZED
        }

        @Test
        fun decreasesStock_afterOrder() {
            val request = createOrderRequest(
                listOf(
                    OrderV1Dto.CreateOrderRequest.OrderItemRequest(productId = 1, quantity = 3),
                ),
            )
            val responseType = object : ParameterizedTypeReference<ApiResponse<Any>>() {}
            testRestTemplate.exchange(
                "/api/v1/orders",
                HttpMethod.POST,
                HttpEntity(request, authHeaders()),
                responseType,
            )

            val stock = jdbcTemplate.queryForObject(
                "SELECT stock FROM products WHERE id = 1",
                Long::class.java,
            )
            stock shouldBe 47
        }
    }

    @Nested
    inner class GetMyOrders {

        @Test
        fun returnsMyOrders() {
            // 주문 생성
            val request = createOrderRequest(
                listOf(
                    OrderV1Dto.CreateOrderRequest.OrderItemRequest(productId = 1, quantity = 1),
                ),
            )
            val createType = object : ParameterizedTypeReference<ApiResponse<Any>>() {}
            testRestTemplate.exchange(
                "/api/v1/orders",
                HttpMethod.POST,
                HttpEntity(request, authHeaders()),
                createType,
            )

            // 목록 조회
            val responseType = object : ParameterizedTypeReference<ApiResponse<Any>>() {}
            val response = testRestTemplate.exchange(
                "/api/v1/orders",
                HttpMethod.GET,
                HttpEntity<Any>(authHeaders()),
                responseType,
            )

            response.statusCode shouldBe HttpStatus.OK
        }

        @Test
        fun returnsEmpty_whenNoOrders() {
            val responseType = object : ParameterizedTypeReference<ApiResponse<Any>>() {}
            val response = testRestTemplate.exchange(
                "/api/v1/orders",
                HttpMethod.GET,
                HttpEntity<Any>(authHeaders()),
                responseType,
            )

            response.statusCode shouldBe HttpStatus.OK
        }
    }

    @Nested
    inner class GetOrderDetail {

        @Test
        fun success_whenValidOrderId() {
            // 주문 생성
            val request = createOrderRequest(
                listOf(
                    OrderV1Dto.CreateOrderRequest.OrderItemRequest(productId = 1, quantity = 2),
                ),
            )
            val createType =
                object : ParameterizedTypeReference<ApiResponse<OrderV1Dto.OrderDetailResponse>>() {}
            val createResponse = testRestTemplate.exchange(
                "/api/v1/orders",
                HttpMethod.POST,
                HttpEntity(request, authHeaders()),
                createType,
            )
            val orderId = createResponse.body?.data?.orderId

            // 상세 조회
            val responseType =
                object : ParameterizedTypeReference<ApiResponse<OrderV1Dto.OrderDetailResponse>>() {}
            val response = testRestTemplate.exchange(
                "/api/v1/orders/$orderId",
                HttpMethod.GET,
                HttpEntity<Any>(authHeaders()),
                responseType,
            )

            response.statusCode shouldBe HttpStatus.OK
            response.body?.data?.items?.size shouldBe 1
            response.body?.data?.items?.get(0)?.productName shouldBe "에어맥스"
        }

        @Test
        fun returnsNotFound_whenOrderNotExists() {
            val responseType = object : ParameterizedTypeReference<ApiResponse<Any>>() {}
            val response = testRestTemplate.exchange(
                "/api/v1/orders/999",
                HttpMethod.GET,
                HttpEntity<Any>(authHeaders()),
                responseType,
            )

            response.statusCode shouldBe HttpStatus.NOT_FOUND
        }
    }
}
