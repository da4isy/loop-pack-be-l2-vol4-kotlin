package com.loopers.domain.brand

import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BrandModelTest {

    @Nested
    inner class Creation {

        @Test
        fun success_whenValidName() {
            shouldNotThrow<Exception> { BrandModel(name = "나이키") }
        }

        @Test
        fun throw_whenNameIsBlank() {
            shouldThrow<CoreException> { BrandModel(name = "") }
                .errorType shouldBe ErrorType.BAD_REQUEST
        }

        @Test
        fun throw_whenNameIsWhitespace() {
            shouldThrow<CoreException> { BrandModel(name = "   ") }
                .errorType shouldBe ErrorType.BAD_REQUEST
        }
    }

    @Nested
    inner class SoftDelete {

        @Test
        fun isNotDeleted_byDefault() {
            val brand = BrandModel(name = "나이키")
            brand.isDeleted() shouldBe false
        }

        @Test
        fun isDeleted_afterDelete() {
            val brand = BrandModel(name = "나이키")
            brand.delete()
            brand.isDeleted() shouldBe true
        }

        @Test
        fun isNotDeleted_afterRestore() {
            val brand = BrandModel(name = "나이키")
            brand.delete()
            brand.restore()
            brand.isDeleted() shouldBe false
        }
    }
}
