package com.loopers.infrastructure.product

import com.loopers.domain.product.ProductModel
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ProductJpaRepository : JpaRepository<ProductModel, Long> {
    fun findAllByDeletedAtIsNull(pageable: Pageable): Page<ProductModel>
    fun findAllByBrandIdAndDeletedAtIsNull(brandId: Long, pageable: Pageable): Page<ProductModel>

    @Query(
        "SELECT p FROM ProductModel p LEFT JOIN LikeModel l ON p.id = l.productId" +
            " WHERE p.deletedAt IS NULL GROUP BY p.id ORDER BY COUNT(l) DESC, p.createdAt DESC",
        countQuery = "SELECT COUNT(p) FROM ProductModel p WHERE p.deletedAt IS NULL",
    )
    fun findAllOrderByLikeCountDesc(pageable: Pageable): Page<ProductModel>

    @Query(
        "SELECT p FROM ProductModel p LEFT JOIN LikeModel l ON p.id = l.productId" +
            " WHERE p.deletedAt IS NULL AND p.brandId = :brandId" +
            " GROUP BY p.id ORDER BY COUNT(l) DESC, p.createdAt DESC",
        countQuery = "SELECT COUNT(p) FROM ProductModel p" +
            " WHERE p.deletedAt IS NULL AND p.brandId = :brandId",
    )
    fun findAllByBrandIdOrderByLikeCountDesc(brandId: Long, pageable: Pageable): Page<ProductModel>
}
