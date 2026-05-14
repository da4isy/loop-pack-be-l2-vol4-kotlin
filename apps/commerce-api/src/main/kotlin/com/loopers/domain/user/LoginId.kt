package com.loopers.domain.user

import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType

class LoginId(value: String) {
    val value: String

    init {
        if (value.length !in MIN_LENGTH..MAX_LENGTH) {
            throw CoreException(
                errorType = ErrorType.BAD_REQUEST,
                customMessage = "로그인 ID 는 $MIN_LENGTH~${MAX_LENGTH}자여야 합니다.",
            )
        }
        if (!value.matches(ALLOWED_PATTERN)) {
            throw CoreException(
                errorType = ErrorType.BAD_REQUEST,
                customMessage = "로그인 ID 는 영문/숫자만 허용됩니다.",
            )
        }
        this.value = value
    }

    companion object {
        private const val MIN_LENGTH = 4
        private const val MAX_LENGTH = 20
        private val ALLOWED_PATTERN = Regex("""^[a-zA-Z0-9]+$""")
    }
}
