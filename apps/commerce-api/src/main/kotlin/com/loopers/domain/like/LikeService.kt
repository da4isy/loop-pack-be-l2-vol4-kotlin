package com.loopers.domain.like

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class LikeService(
    private val likeRepository: LikeRepository,
) {

    fun like(userId: Long, productId: Long) {
        if (likeRepository.existsByUserIdAndProductId(userId, productId)) return
        likeRepository.save(LikeModel(userId = userId, productId = productId))
    }

    fun unlike(userId: Long, productId: Long) {
        likeRepository.deleteByUserIdAndProductId(userId, productId)
    }

    fun countByProductId(productId: Long): Long {
        return likeRepository.countByProductId(productId)
    }

    fun countByProductIds(productIds: List<Long>): Map<Long, Long> {
        if (productIds.isEmpty()) return emptyMap()
        return likeRepository.countByProductIds(productIds)
    }

    fun getLikesByUserId(userId: Long, pageable: Pageable): Page<LikeModel> {
        return likeRepository.findAllByUserId(userId, pageable)
    }
}
