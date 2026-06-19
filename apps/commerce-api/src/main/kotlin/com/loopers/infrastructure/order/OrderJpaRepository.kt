package com.loopers.infrastructure.order

import com.loopers.domain.order.OrderModel
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.ZonedDateTime

interface OrderJpaRepository : JpaRepository<OrderModel, Long> {

    @EntityGraph(attributePaths = ["orderItems"])
    @Query("SELECT o FROM OrderModel o WHERE o.id = :id")
    fun findByIdWithItems(id: Long): OrderModel?

    @EntityGraph(attributePaths = ["orderItems"])
    fun findAllByUserIdOrderByCreatedAtDesc(userId: Long, pageable: Pageable): Page<OrderModel>

    @EntityGraph(attributePaths = ["orderItems"])
    @Query(
        "SELECT o FROM OrderModel o WHERE o.userId = :userId" +
            " AND o.createdAt >= :startAt AND o.createdAt <= :endAt" +
            " ORDER BY o.createdAt DESC",
    )
    fun findAllByUserIdAndCreatedAtBetween(
        userId: Long,
        startAt: ZonedDateTime,
        endAt: ZonedDateTime,
        pageable: Pageable,
    ): Page<OrderModel>

    @EntityGraph(attributePaths = ["orderItems"])
    @Query(
        "SELECT o FROM OrderModel o WHERE o.userId = :userId" +
            " AND o.createdAt >= :startAt ORDER BY o.createdAt DESC",
    )
    fun findAllByUserIdAndCreatedAtAfter(
        userId: Long,
        startAt: ZonedDateTime,
        pageable: Pageable,
    ): Page<OrderModel>

    @EntityGraph(attributePaths = ["orderItems"])
    @Query(
        "SELECT o FROM OrderModel o WHERE o.userId = :userId" +
            " AND o.createdAt <= :endAt ORDER BY o.createdAt DESC",
    )
    fun findAllByUserIdAndCreatedAtBefore(
        userId: Long,
        endAt: ZonedDateTime,
        pageable: Pageable,
    ): Page<OrderModel>
}
