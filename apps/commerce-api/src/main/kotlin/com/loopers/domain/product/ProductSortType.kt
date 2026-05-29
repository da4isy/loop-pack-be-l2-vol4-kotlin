package com.loopers.domain.product

import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType

enum class ProductSortType {
    LATEST,
    PRICE_ASC,
    ;

    companion object {
        fun from(value: String): ProductSortType {
            return when (value.lowercase()) {
                "latest" -> LATEST
                "price_asc" -> PRICE_ASC
                else -> throw CoreException(
                    errorType = ErrorType.BAD_REQUEST,
                    customMessage = "지원하지 않는 정렬 조건입니다: $value",
                )
            }
        }
    }
}
