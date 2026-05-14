package com.loopers.domain.user

import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PasswordTest {

    @Nested
    inner class Create {

        @Test
        fun throw_whenLessThan8Chars() {
            shouldThrow<CoreException> { Password("Abc12!", "1995-12-03") }
                .errorType shouldBe ErrorType.BAD_REQUEST
        }

        @Test
        fun throw_whenMoreThan16Chars() {
            shouldThrow<CoreException> { Password("Abcdefghijklmnop1!", "1995-12-03") }
                .errorType shouldBe ErrorType.BAD_REQUEST
        }

        @Test
        fun throw_whenContainsInvalidCharacter() {
            shouldThrow<CoreException> { Password("Abcdef 12!", "1995-12-03") }
                .errorType shouldBe ErrorType.BAD_REQUEST
        }

        @Test
        fun throw_whenContainsBirthDate() {
            shouldThrow<CoreException> { Password("Abc19951203!", "1995-12-03") }
                .errorType shouldBe ErrorType.BAD_REQUEST
        }

        @Test
        fun success_whenValid() {
            val password = Password("Daisyyyy1@@!", "1995-12-03")
            password.value shouldBe "Daisyyyy1@@!"
        }
    }
}
