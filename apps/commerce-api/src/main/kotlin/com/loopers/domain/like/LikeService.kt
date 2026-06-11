package com.loopers.domain.like

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class LikeService(
    private val likeRepository: LikeRepository,
) {

    @Transactional
    fun like(userId: Long, productId: Long) {
        if (likeRepository.existsByUserIdAndProductId(userId, productId)) return
        try {
            likeRepository.save(LikeModel(userId = userId, productId = productId))
        } catch (e: DataIntegrityViolationException) {
            // unique constraint 위반 → 이미 좋아요 존재, 멱등 처리
        }
    }

    @Transactional
    fun unlike(userId: Long, productId: Long) {
        likeRepository.deleteByUserIdAndProductId(userId, productId)
    }

    @Transactional(readOnly = true)
    fun countByProductId(productId: Long): Long {
        return likeRepository.countByProductId(productId)
    }

    @Transactional(readOnly = true)
    fun countByProductIds(productIds: List<Long>): Map<Long, Long> {
        if (productIds.isEmpty()) return emptyMap()
        return likeRepository.countByProductIds(productIds)
    }

    @Transactional(readOnly = true)
    fun getLikesByUserId(userId: Long, pageable: Pageable): Page<LikeModel> {
        return likeRepository.findAllByUserId(userId, pageable)
    }
}
