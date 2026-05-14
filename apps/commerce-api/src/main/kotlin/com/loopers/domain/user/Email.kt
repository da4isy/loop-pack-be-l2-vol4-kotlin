package com.loopers.domain.user

import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType

class Email(value: String) {
    val value: String

    init {
        if (!value.matches(EMAIL_PATTERN)) {
            throw CoreException(
                errorType = ErrorType.BAD_REQUEST,
                customMessage = "올바른 이메일 형식이 아닙니다.",
            )
        }
        this.value = value
    }

    companion object {
        private val EMAIL_PATTERN = Regex("""^[A-Za-z0-9._%+\-]+@[A-Za-z0-9.\-]+\.[A-Za-z]{2,}$""")
    }
}
