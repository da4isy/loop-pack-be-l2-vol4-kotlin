package com.loopers.domain.user

import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

class BirthDateTest {

    @Nested
    inner class Create {

        @Test
        fun throw_whenFormatInvalid() {
            shouldThrow<CoreException> { BirthDate("1995/12/03") }
                .errorType shouldBe ErrorType.BAD_REQUEST
        }

        @Test
        fun throw_whenInFuture() {
            val tomorrow = LocalDate.now().plusDays(1)
            shouldThrow<CoreException> { BirthDate(tomorrow.toString()) }
                .errorType shouldBe ErrorType.BAD_REQUEST
        }

        @Test
        fun success_whenValid() {
            val birthDate = BirthDate("1995-12-03")
            birthDate.value shouldBe "1995-12-03"
        }
    }
}
