package com.loopers.domain.user

import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType

class Password(value: String, birthDate: String) {
    val value: String

    init {
        if (value.length !in MIN_LENGTH..MAX_LENGTH) {
            throw CoreException(
                errorType = ErrorType.BAD_REQUEST,
                customMessage = "비밀번호는 $MIN_LENGTH~${MAX_LENGTH}자여야 합니다.",
            )
        }
        if (!value.matches(ALLOWED_PATTERN)) {
            throw CoreException(
                errorType = ErrorType.BAD_REQUEST,
                customMessage = "비밀번호에 허용되지 않은 문자가 포함되어 있습니다.",
            )
        }
        if (value.contains(birthDate.replace("-", ""))) {
            throw CoreException(
                errorType = ErrorType.BAD_REQUEST,
                customMessage = "비밀번호에 생년월일을 포함할 수 없습니다.",
            )
        }
        this.value = value
    }

    companion object {
        private const val MIN_LENGTH = 8
        private const val MAX_LENGTH = 16
        private val ALLOWED_PATTERN = Regex("""^[a-zA-Z0-9!@#$%^&*()_+\-=]+$""")
    }
}
