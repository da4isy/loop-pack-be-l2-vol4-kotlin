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

    /**
     * 비관적 락으로 상품 조회 — 주문 시 재고 차감용
     */
    @Transactional
    fun getProductWithLock(id: Long): ProductModel {
        val product = productRepository.findByIdWithLock(id)
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

    @Transactional
    fun create(name: String, price: Long, stock: Long, brandId: Long): ProductModel {
        return productRepository.save(
            ProductModel(name = name, price = price, stock = stock, brandId = brandId),
        )
    }

    @Transactional
    fun update(id: Long, name: String, price: Long, stock: Long): ProductModel {
        val product = getProduct(id)
        product.update(name, price, stock)
        return productRepository.save(product)
    }

    @Transactional
    fun delete(id: Long) {
        val product = getProduct(id)
        product.delete()
        productRepository.save(product)
    }

    @Transactional
    fun deleteByBrandId(brandId: Long) {
        val products = productRepository.findAll(
            brandId,
            ProductSortType.LATEST,
            org.springframework.data.domain.Pageable.unpaged(),
        )
        products.content.forEach { product ->
            product.delete()
            productRepository.save(product)
        }
    }
}
