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
    fun like(userId: Long, productId: Long): Boolean {
        if (likeRepository.existsByUserIdAndProductId(userId, productId)) return false
        return try {
            likeRepository.save(LikeModel(userId = userId, productId = productId))
            true
        } catch (e: DataIntegrityViolationException) {
            false
        }
    }

    @Transactional
    fun unlike(userId: Long, productId: Long): Boolean {
        return likeRepository.deleteByUserIdAndProductId(userId, productId) > 0
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
