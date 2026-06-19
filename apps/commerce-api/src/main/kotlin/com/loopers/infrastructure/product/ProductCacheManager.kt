package com.loopers.infrastructure.product

import com.fasterxml.jackson.databind.ObjectMapper
import com.loopers.application.product.ProductDetailInfo
import com.loopers.config.redis.RedisConfig
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration
import kotlin.random.Random

@Component
class ProductCacheManager(
    @Qualifier(RedisConfig.REDIS_TEMPLATE_MASTER)
    private val redisTemplate: RedisTemplate<String, String>,
    private val objectMapper: ObjectMapper,
) {

    companion object {
        private const val DETAIL_KEY_PREFIX = "product:detail:"
        private val DETAIL_BASE_TTL = Duration.ofMinutes(5)
        private const val DETAIL_JITTER_SECONDS = 30L
    }

    fun getDetail(productId: Long): ProductDetailInfo? {
        return try {
            val key = "$DETAIL_KEY_PREFIX$productId"
            val cached = redisTemplate.opsForValue().get(key) ?: return null
            objectMapper.readValue(cached, ProductDetailInfo::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun putDetail(productId: Long, info: ProductDetailInfo) {
        try {
            val key = "$DETAIL_KEY_PREFIX$productId"
            val json = objectMapper.writeValueAsString(info)
            redisTemplate.opsForValue().set(key, json, detailTtl())
        } catch (_: Exception) {
        }
    }

    fun evictDetail(productId: Long) {
        try {
            redisTemplate.delete("$DETAIL_KEY_PREFIX$productId")
        } catch (_: Exception) {
        }
    }

    private fun detailTtl(): Duration =
        DETAIL_BASE_TTL.plusSeconds(Random.nextLong(-DETAIL_JITTER_SECONDS, DETAIL_JITTER_SECONDS + 1))
}
