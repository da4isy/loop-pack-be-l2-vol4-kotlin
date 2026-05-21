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
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserV1ApiE2ETest @Autowired constructor(
    private val testRestTemplate: TestRestTemplate,
    private val databaseCleanUp: DatabaseCleanUp,
) {

    companion object {
        private const val SIGNUP_ENDPOINT = "/api/v1/users"
        private const val GET_ME_ENDPOINT = "/api/v1/users/me"
        private const val CHANGE_PASSWORD_ENDPOINT = "/api/v1/users/me/password"

        private val DEFAULT_SIGNUP_REQUEST = UserDto.SignupRequest(
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

    private fun signup(request: UserDto.SignupRequest = DEFAULT_SIGNUP_REQUEST) {
        val responseType = object : ParameterizedTypeReference<ApiResponse<UserDto.SignupResponse>>() {}
        testRestTemplate.exchange(
            SIGNUP_ENDPOINT,
            HttpMethod.POST,
            HttpEntity(request),
            responseType,
        )
    }

    private fun authHeaders(loginId: String, password: String) = HttpHeaders().apply {
        add("X-Loopers-LoginId", loginId)
        add("X-Loopers-LoginPw", password)
    }

    @Nested
    inner class Signup {

        @Test
        fun success_whenValidRequest() {
            // when
            val responseType =
                object : ParameterizedTypeReference<ApiResponse<UserDto.SignupResponse>>() {}
            val response = testRestTemplate.exchange(
                SIGNUP_ENDPOINT,
                HttpMethod.POST,
                HttpEntity(DEFAULT_SIGNUP_REQUEST),
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
            signup()

            // when
            val responseType =
                object : ParameterizedTypeReference<ApiResponse<UserDto.SignupResponse>>() {}
            val response = testRestTemplate.exchange(
                SIGNUP_ENDPOINT,
                HttpMethod.POST,
                HttpEntity(DEFAULT_SIGNUP_REQUEST),
                responseType,
            )

            // then
            response.statusCode shouldBe HttpStatus.CONFLICT
            response.body?.meta?.result shouldBe ApiResponse.Metadata.Result.FAIL
        }
    }

    @Nested
    inner class GetMe {

        @Test
        fun success_whenValidCredentials() {
            // given
            signup()

            // when
            val responseType =
                object : ParameterizedTypeReference<ApiResponse<UserDto.GetMeResponse>>() {}
            val response = testRestTemplate.exchange(
                GET_ME_ENDPOINT,
                HttpMethod.GET,
                HttpEntity<Any>(authHeaders("da4isy", "Daisyyyy1@@!")),
                responseType,
            )

            // then
            response.statusCode shouldBe HttpStatus.OK
            response.body?.data?.loginId shouldBe "da4isy"
            response.body?.data?.name shouldBe "정다*"
        }

        @Test
        fun returnsUnauthorized_whenPasswordMismatch() {
            // given
            signup()

            // when
            val responseType =
                object : ParameterizedTypeReference<ApiResponse<UserDto.GetMeResponse>>() {}
            val response = testRestTemplate.exchange(
                GET_ME_ENDPOINT,
                HttpMethod.GET,
                HttpEntity<Any>(authHeaders("da4isy", "wrong-password")),
                responseType,
            )

            // then
            response.statusCode shouldBe HttpStatus.UNAUTHORIZED
            response.body?.meta?.result shouldBe ApiResponse.Metadata.Result.FAIL
        }
    }

    @Nested
    inner class ChangePassword {

        @Test
        fun success_whenValidChange() {
            // given
            signup()

            // when
            val changeRequest = UserDto.ChangePasswordRequest(
                currentPassword = "Daisyyyy1@@!",
                newPassword = "NewPass2@@",
            )
            val responseType = object : ParameterizedTypeReference<ApiResponse<Any>>() {}
            val response = testRestTemplate.exchange(
                CHANGE_PASSWORD_ENDPOINT,
                HttpMethod.PATCH,
                HttpEntity(changeRequest, authHeaders("da4isy", "Daisyyyy1@@!")),
                responseType,
            )

            // then
            response.statusCode shouldBe HttpStatus.OK
            response.body?.meta?.result shouldBe ApiResponse.Metadata.Result.SUCCESS
        }

        @Test
        fun returnsUnauthorized_whenCurrentPasswordMismatch() {
            // given
            signup()

            // when
            val changeRequest = UserDto.ChangePasswordRequest(
                currentPassword = "wrong-current",
                newPassword = "NewPass2@@",
            )
            val responseType = object : ParameterizedTypeReference<ApiResponse<Any>>() {}
            val response = testRestTemplate.exchange(
                CHANGE_PASSWORD_ENDPOINT,
                HttpMethod.PATCH,
                HttpEntity(changeRequest, authHeaders("da4isy", "Daisyyyy1@@!")),
                responseType,
            )

            // then
            response.statusCode shouldBe HttpStatus.UNAUTHORIZED
            response.body?.meta?.result shouldBe ApiResponse.Metadata.Result.FAIL
        }
    }
}
