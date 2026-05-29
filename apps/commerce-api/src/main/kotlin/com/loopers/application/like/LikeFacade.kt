package com.loopers.application.like

import com.loopers.domain.like.LikeService
import com.loopers.domain.product.ProductService
import com.loopers.domain.user.UserService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class LikeFacade(
    private val likeService: LikeService,
    private val productService: ProductService,
    private val userService: UserService,
) {

    @Transactional
    fun like(loginId: String, password: String, productId: Long) {
        val user = userService.getMe(loginId, password)
        productService.getProduct(productId)
        likeService.like(user.id, productId)
    }

    @Transactional
    fun unlike(loginId: String, password: String, productId: Long) {
        val user = userService.getMe(loginId, password)
        likeService.unlike(user.id, productId)
    }

    @Transactional(readOnly = true)
    fun getMyLikes(loginId: String, password: String, pageable: Pageable): Page<LikeInfo> {
        val user = userService.getMe(loginId, password)
        val likes = likeService.getLikesByUserId(user.id, pageable)
        val productIds = likes.content.map { it.productId }.distinct()
        val products = productService.getProductsByIds(productIds)

        return likes.map { like ->
            LikeInfo.of(like, products[like.productId])
        }
    }
}
