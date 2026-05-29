package com.loopers.infrastructure.product

import com.loopers.domain.product.ProductModel
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface ProductJpaRepository : JpaRepository<ProductModel, Long> {
    fun findAllByDeletedAtIsNull(pageable: Pageable): Page<ProductModel>
    fun findAllByBrandIdAndDeletedAtIsNull(brandId: Long, pageable: Pageable): Page<ProductModel>
}
