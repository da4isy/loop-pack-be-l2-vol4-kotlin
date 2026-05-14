package com.loopers.domain.user

import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class EmailTest {

    @Nested
    inner class Create {

        @Test
        fun throw_whenMissingAtSign() {
            shouldThrow<CoreException> { Email("dahee.jeong123example.com") }
                .errorType shouldBe ErrorType.BAD_REQUEST
        }

        @Test
        fun throw_whenMissingDomain() {
            shouldThrow<CoreException> { Email("dahee@") }
                .errorType shouldBe ErrorType.BAD_REQUEST
        }

        @Test
        fun throw_whenMissingTld() {
            shouldThrow<CoreException> { Email("dahee@example") }
                .errorType shouldBe ErrorType.BAD_REQUEST
        }

        @Test
        fun success_whenValid() {
            val email = Email("dahee.jeong123@example.com")
            email.value shouldBe "dahee.jeong123@example.com"
        }
    }
}
