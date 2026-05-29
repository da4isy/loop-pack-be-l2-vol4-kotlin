package com.loopers.domain.product

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ProductRepository {
    fun save(product: ProductModel): ProductModel
    fun findById(id: Long): ProductModel?
    fun findAllByIds(ids: List<Long>): List<ProductModel>
    fun findAll(brandId: Long?, sortType: ProductSortType, pageable: Pageable): Page<ProductModel>
}
