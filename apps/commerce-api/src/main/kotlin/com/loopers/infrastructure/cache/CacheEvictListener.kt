package com.loopers.infrastructure.cache

import com.loopers.infrastructure.brand.BrandCacheManager
import com.loopers.infrastructure.product.ProductCacheManager
import com.loopers.support.cache.BrandCacheEvictEvent
import com.loopers.support.cache.ProductCacheEvictEvent
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class CacheEvictListener(
    private val productCacheManager: ProductCacheManager,
    private val brandCacheManager: BrandCacheManager,
) {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onProductEvict(event: ProductCacheEvictEvent) {
        productCacheManager.evictDetail(event.productId)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onBrandEvict(event: BrandCacheEvictEvent) {
        brandCacheManager.evictDetail(event.brandId)
    }
}
