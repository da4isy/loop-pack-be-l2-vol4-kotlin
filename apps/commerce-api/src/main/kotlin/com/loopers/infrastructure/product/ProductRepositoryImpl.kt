package com.loopers.infrastructure.product

import com.loopers.domain.product.ProductModel
import com.loopers.domain.product.ProductRepository
import com.loopers.domain.product.ProductSortType
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class ProductRepositoryImpl(
    private val productJpaRepository: ProductJpaRepository,
) : ProductRepository {
    override fun save(product: ProductModel): ProductModel = productJpaRepository.save(product)

    override fun findById(id: Long): ProductModel? = productJpaRepository.findByIdOrNull(id)

    override fun findByIdWithLock(id: Long): ProductModel? = productJpaRepository.findByIdWithLock(id)

    override fun findAllByIds(ids: List<Long>): List<ProductModel> = productJpaRepository.findAllById(ids)

    override fun findAll(brandId: Long?, sortType: ProductSortType, pageable: Pageable): Page<ProductModel> {
        if (sortType == ProductSortType.LIKES_DESC) {
            return if (brandId != null) {
                productJpaRepository.findAllByBrandIdOrderByLikeCountDesc(brandId, pageable)
            } else {
                productJpaRepository.findAllOrderByLikeCountDesc(pageable)
            }
        }

        val sort = when (sortType) {
            ProductSortType.LATEST -> Sort.by(Sort.Direction.DESC, "createdAt")
            ProductSortType.PRICE_ASC -> Sort.by(Sort.Direction.ASC, "price")
            ProductSortType.LIKES_DESC -> throw IllegalStateException("이미 처리됨")
        }
        val sortedPageable = PageRequest.of(pageable.pageNumber, pageable.pageSize, sort)

        return if (brandId != null) {
            productJpaRepository.findAllByBrandIdAndDeletedAtIsNull(brandId, sortedPageable)
        } else {
            productJpaRepository.findAllByDeletedAtIsNull(sortedPageable)
        }
    }
}
