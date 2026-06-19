package com.loopers.infrastructure.product

import com.loopers.domain.product.ProductModel
import jakarta.persistence.LockModeType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface ProductJpaRepository : JpaRepository<ProductModel, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProductModel p WHERE p.id = :id")
    fun findByIdWithLock(id: Long): ProductModel?
    fun findAllByDeletedAtIsNull(pageable: Pageable): Page<ProductModel>
    fun findAllByBrandIdAndDeletedAtIsNull(brandId: Long, pageable: Pageable): Page<ProductModel>

    fun findAllByDeletedAtIsNullOrderByLikeCountDescCreatedAtDesc(pageable: Pageable): Page<ProductModel>
    fun findAllByBrandIdAndDeletedAtIsNullOrderByLikeCountDescCreatedAtDesc(
        brandId: Long,
        pageable: Pageable,
    ): Page<ProductModel>

    @Modifying
    @Query("UPDATE ProductModel p SET p.likeCount = p.likeCount + 1 WHERE p.id = :id")
    fun incrementLikeCount(id: Long)

    @Modifying
    @Query("UPDATE ProductModel p SET p.likeCount = p.likeCount - 1 WHERE p.id = :id AND p.likeCount > 0")
    fun decrementLikeCount(id: Long)
}
