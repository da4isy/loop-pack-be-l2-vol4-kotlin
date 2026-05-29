package com.loopers.domain.like

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface LikeRepository {
    fun save(like: LikeModel): LikeModel
    fun existsByUserIdAndProductId(userId: Long, productId: Long): Boolean
    fun deleteByUserIdAndProductId(userId: Long, productId: Long)
    fun countByProductId(productId: Long): Long
    fun countByProductIds(productIds: List<Long>): Map<Long, Long>
    fun findAllByUserId(userId: Long, pageable: Pageable): Page<LikeModel>
}
