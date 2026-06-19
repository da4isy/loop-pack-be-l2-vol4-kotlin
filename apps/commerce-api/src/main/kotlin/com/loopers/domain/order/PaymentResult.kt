package com.loopers.domain.order

import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType

data class PaymentResult(
    val success: Boolean,
    val transactionId: String,
) {
    fun ensureSuccess() {
        if (!success) {
            throw CoreException(
                errorType = ErrorType.INTERNAL_ERROR,
                customMessage = "결제에 실패했습니다.",
            )
        }
    }
}
