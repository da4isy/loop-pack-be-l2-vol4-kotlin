package com.loopers.domain.order

import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime

@Component
class OrderService(
    private val orderRepository: OrderRepository,
) {

    @Transactional
    fun createOrder(order: OrderModel): OrderModel {
        return orderRepository.save(order)
    }

    @Transactional(readOnly = true)
    fun getOrder(id: Long): OrderModel {
        return orderRepository.findById(id)
            ?: throw CoreException(
                errorType = ErrorType.NOT_FOUND,
                customMessage = "존재하지 않는 주문입니다.",
            )
    }

    @Transactional(readOnly = true)
    fun getOrderByIdAndUserId(id: Long, userId: Long): OrderModel {
        val order = getOrder(id)
        if (order.userId != userId) {
            throw CoreException(
                errorType = ErrorType.NOT_FOUND,
                customMessage = "존재하지 않는 주문입니다.",
            )
        }
        return order
    }

    @Transactional(readOnly = true)
    fun getOrdersByUserId(
        userId: Long,
        startAt: ZonedDateTime?,
        endAt: ZonedDateTime?,
        pageable: Pageable,
    ): Page<OrderModel> {
        return orderRepository.findAllByUserId(userId, startAt, endAt, pageable)
    }
}
