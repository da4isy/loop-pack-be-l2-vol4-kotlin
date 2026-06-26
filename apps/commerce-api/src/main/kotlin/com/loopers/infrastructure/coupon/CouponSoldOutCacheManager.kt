package com.loopers.infrastructure.coupon

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class CouponSoldOutCacheManager {

    private val soldOutCache: Cache<Long, Boolean> = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofSeconds(10))
        .maximumSize(1000)
        .build()

    fun isSoldOut(couponTemplateId: Long): Boolean =
        soldOutCache.getIfPresent(couponTemplateId) == true

    fun markSoldOut(couponTemplateId: Long) {
        soldOutCache.put(couponTemplateId, true)
    }
}
