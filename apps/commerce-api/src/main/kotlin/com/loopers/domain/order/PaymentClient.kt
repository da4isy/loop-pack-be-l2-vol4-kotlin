package com.loopers.domain.order

interface PaymentClient {
    fun pay(amount: Long): PaymentResult
}
