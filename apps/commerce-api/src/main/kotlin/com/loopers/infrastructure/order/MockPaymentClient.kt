package com.loopers.infrastructure.order

import com.loopers.domain.order.PaymentClient
import com.loopers.domain.order.PaymentResult
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class MockPaymentClient : PaymentClient {

    override fun pay(amount: Long): PaymentResult {
        return PaymentResult(
            success = true,
            transactionId = UUID.randomUUID().toString(),
        )
    }
}
