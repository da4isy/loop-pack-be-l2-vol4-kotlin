package com.loopers.domain.user

import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType
import java.time.LocalDate
import java.time.format.DateTimeParseException

class BirthDate(value: String) {
    val value: String

    init {
        val parsed = try {
            LocalDate.parse(value)
        } catch (e: DateTimeParseException) {
            throw CoreException(
                errorType = ErrorType.BAD_REQUEST,
                customMessage = "올바른 생년월일 형식이 아닙니다 (yyyy-MM-dd).",
            )
        }
        if (parsed.isAfter(LocalDate.now())) {
            throw CoreException(
                errorType = ErrorType.BAD_REQUEST,
                customMessage = "생년월일은 미래일 수 없습니다.",
            )
        }
        this.value = value
    }
}
