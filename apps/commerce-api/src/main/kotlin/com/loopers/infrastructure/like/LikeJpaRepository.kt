package com.loopers.infrastructure.like

import com.loopers.domain.like.LikeModel
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface LikeJpaRepository : JpaRepository<LikeModel, Long> {
    fun existsByUserIdAndProductId(userId: Long, productId: Long): Boolean
    fun deleteByUserIdAndProductId(userId: Long, productId: Long): Long
    fun countByProductId(productId: Long): Long
    fun findAllByUserIdOrderByCreatedAtDesc(userId: Long, pageable: Pageable): Page<LikeModel>

    @Query(
        "SELECT l.productId, COUNT(l) FROM LikeModel l" +
            " WHERE l.productId IN :productIds GROUP BY l.productId",
    )
    fun countGroupByProductIds(productIds: List<Long>): List<Array<Any>>
}
