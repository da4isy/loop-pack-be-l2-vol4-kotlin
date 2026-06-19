package com.loopers.infrastructure.like

import com.loopers.domain.like.LikeModel
import com.loopers.domain.like.LikeRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class LikeRepositoryImpl(
    private val likeJpaRepository: LikeJpaRepository,
) : LikeRepository {

    override fun save(like: LikeModel): LikeModel = likeJpaRepository.save(like)

    override fun existsByUserIdAndProductId(userId: Long, productId: Long): Boolean =
        likeJpaRepository.existsByUserIdAndProductId(userId, productId)

    @Transactional
    override fun deleteByUserIdAndProductId(userId: Long, productId: Long): Long {
        return likeJpaRepository.deleteByUserIdAndProductId(userId, productId)
    }

    override fun countByProductId(productId: Long): Long =
        likeJpaRepository.countByProductId(productId)

    override fun countByProductIds(productIds: List<Long>): Map<Long, Long> {
        return likeJpaRepository.countGroupByProductIds(productIds)
            .associate { (it[0] as Long) to (it[1] as Long) }
    }

    override fun findAllByUserId(userId: Long, pageable: Pageable): Page<LikeModel> =
        likeJpaRepository.findAllByUserIdOrderByCreatedAtDesc(userId, pageable)
}
