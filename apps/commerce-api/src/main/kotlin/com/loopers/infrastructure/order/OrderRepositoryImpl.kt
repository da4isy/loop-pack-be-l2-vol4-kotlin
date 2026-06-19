package com.loopers.infrastructure.order

import com.loopers.domain.order.OrderModel
import com.loopers.domain.order.OrderRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.time.ZonedDateTime

@Component
class OrderRepositoryImpl(
    private val orderJpaRepository: OrderJpaRepository,
) : OrderRepository {

    override fun save(order: OrderModel): OrderModel = orderJpaRepository.save(order)

    override fun findById(id: Long): OrderModel? = orderJpaRepository.findByIdWithItems(id)

    override fun findAllByUserId(
        userId: Long,
        startAt: ZonedDateTime?,
        endAt: ZonedDateTime?,
        pageable: Pageable,
    ): Page<OrderModel> {
        return when {
            startAt != null && endAt != null ->
                orderJpaRepository.findAllByUserIdAndCreatedAtBetween(userId, startAt, endAt, pageable)
            startAt != null ->
                orderJpaRepository.findAllByUserIdAndCreatedAtAfter(userId, startAt, pageable)
            endAt != null ->
                orderJpaRepository.findAllByUserIdAndCreatedAtBefore(userId, endAt, pageable)
            else ->
                orderJpaRepository.findAllByUserIdOrderByCreatedAtDesc(userId, pageable)
        }
    }

    override fun findAll(pageable: Pageable): Page<OrderModel> =
        orderJpaRepository.findAll(pageable)
}
