package com.loopers.domain.user

import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LoginIdTest {

    @Nested
    inner class Create {

        @Test
        fun throw_whenLessThan4Chars() {
            shouldThrow<CoreException> { LoginId("abc") }
                .errorType shouldBe ErrorType.BAD_REQUEST
        }

        @Test
        fun throw_whenMoreThan20Chars() {
            shouldThrow<CoreException> { LoginId("a".repeat(21)) }
                .errorType shouldBe ErrorType.BAD_REQUEST
        }

        @Test
        fun throw_whenContainsInvalidCharacter() {
            shouldThrow<CoreException> { LoginId("alen!") }
                .errorType shouldBe ErrorType.BAD_REQUEST
        }

        @Test
        fun success_whenValid() {
            val loginId = LoginId("da4isy")
            loginId.value shouldBe "da4isy"
        }
    }
}
