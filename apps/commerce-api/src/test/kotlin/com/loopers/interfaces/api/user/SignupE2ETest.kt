package com.loopers.interfaces.api.user

import com.loopers.interfaces.api.ApiResponse
import com.loopers.utils.DatabaseCleanUp
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SignupE2ETest @Autowired constructor(
    private val testRestTemplate: TestRestTemplate,
    private val databaseCleanUp: DatabaseCleanUp,
) {

    @AfterEach
    fun tearDown() {
        databaseCleanUp.truncateAllTables()
    }

    @Nested
    inner class Signup {

        @Test
        fun success_whenValidRequest() {
            // given
            val request = UserDto.SignupRequest(
                loginId = "da4isy",
                password = "Daisyyyy1@@!",
                name = "정다희",
                birthDate = "1995-12-03",
                email = "dahee.jeong123@example.com",
            )

            // when
            val responseType =
                object : ParameterizedTypeReference<ApiResponse<UserDto.SignupResponse>>() {}
            val response = testRestTemplate.exchange(
                "/api/v1/users",
                HttpMethod.POST,
                HttpEntity(request),
                responseType,
            )

            // then
            response.statusCode shouldBe HttpStatus.OK
            response.body?.data?.loginId shouldBe "da4isy"
            response.body?.data?.name shouldBe "정다희"
        }

        @Test
        fun returnsConflict_whenLoginIdAlreadyExists() {
            // given
            val request = UserDto.SignupRequest(
                loginId = "da4isy",
                password = "Daisyyyy1@@!",
                name = "정다희",
                birthDate = "1995-12-03",
                email = "dahee.jeong123@example.com",
            )
            val responseType =
                object : ParameterizedTypeReference<ApiResponse<UserDto.SignupResponse>>() {}
            testRestTemplate.exchange("/api/v1/users", HttpMethod.POST, HttpEntity(request), responseType)

            // when
            val response = testRestTemplate.exchange(
                "/api/v1/users",
                HttpMethod.POST,
                HttpEntity(request),
                responseType,
            )

            // then
            response.statusCode shouldBe HttpStatus.CONFLICT
            response.body?.meta?.result shouldBe ApiResponse.Metadata.Result.FAIL
        }
    }
}
