package com.loopers.application.like

import com.loopers.domain.like.LikeService
import com.loopers.domain.product.ProductService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class LikeFacade(
    private val likeService: LikeService,
    private val productService: ProductService,
) {

    fun like(userId: Long, productId: Long) {
        productService.getProduct(productId)
        likeService.like(userId, productId)
    }

    fun unlike(userId: Long, productId: Long) {
        likeService.unlike(userId, productId)
    }

    fun getMyLikes(userId: Long, pageable: Pageable): Page<LikeInfo> {
        val likes = likeService.getLikesByUserId(userId, pageable)
        val productIds = likes.content.map { it.productId }.distinct()
        val products = productService.getProductsByIds(productIds)

        return likes.map { like -> LikeInfo.of(like, products[like.productId]) }
    }
}
