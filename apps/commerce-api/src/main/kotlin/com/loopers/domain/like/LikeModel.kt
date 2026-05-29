package com.loopers.domain.like

import com.loopers.domain.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(
    name = "likes",
    indexes = [Index(name = "idx_likes_user_product", columnList = "user_id, product_id")],
)
class LikeModel(
    userId: Long,
    productId: Long,
) : BaseEntity() {

    @Column(name = "user_id", nullable = false)
    var userId: Long = userId
        protected set

    @Column(name = "product_id", nullable = false)
    var productId: Long = productId
        protected set
}
