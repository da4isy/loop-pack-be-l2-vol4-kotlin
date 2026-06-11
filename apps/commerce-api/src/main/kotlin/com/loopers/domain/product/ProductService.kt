package com.loopers.domain.product

import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ProductService(
    private val productRepository: ProductRepository,
) {
    @Transactional(readOnly = true)
    fun getProduct(id: Long): ProductModel {
        val product = productRepository.findById(id)
            ?: throw CoreException(errorType = ErrorType.NOT_FOUND, customMessage = "존재하지 않는 상품입니다.")
        if (product.isDeleted()) {
            throw CoreException(errorType = ErrorType.NOT_FOUND, customMessage = "삭제된 상품입니다.")
        }
        return product
    }

    @Transactional(readOnly = true)
    fun getProducts(brandId: Long?, sortType: ProductSortType, pageable: Pageable): Page<ProductModel> {
        return productRepository.findAll(brandId, sortType, pageable)
    }

    @Transactional(readOnly = true)
    fun getProductsByIds(ids: List<Long>): Map<Long, ProductModel> {
        if (ids.isEmpty()) return emptyMap()
        return productRepository.findAllByIds(ids).associateBy { it.id }
    }
}
