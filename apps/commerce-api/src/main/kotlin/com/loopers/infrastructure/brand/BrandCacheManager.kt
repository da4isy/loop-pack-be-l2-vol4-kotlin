package com.loopers.infrastructure.brand

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.loopers.config.redis.RedisConfig
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration
import kotlin.random.Random

@Component
class BrandCacheManager(
    @Qualifier(RedisConfig.REDIS_TEMPLATE_MASTER)
    private val redisTemplate: RedisTemplate<String, String>,
    private val objectMapper: ObjectMapper,
) {

    companion object {
        private const val DETAIL_KEY_PREFIX = "brand:detail:"
        private val DETAIL_BASE_TTL = Duration.ofMinutes(30)
        private const val DETAIL_JITTER_SECONDS = 60L
    }

    private val localCache: Cache<Long, BrandCacheInfo> = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofMinutes(10))
        .maximumSize(200)
        .build()

    fun getDetail(brandId: Long): BrandCacheInfo? {
        localCache.getIfPresent(brandId)?.let { return it }

        return try {
            val key = "$DETAIL_KEY_PREFIX$brandId"
            val cached = redisTemplate.opsForValue().get(key) ?: return null
            val info = objectMapper.readValue(cached, BrandCacheInfo::class.java)
            localCache.put(brandId, info)
            info
        } catch (e: Exception) {
            null
        }
    }

    fun putDetail(brandId: Long, brand: com.loopers.domain.brand.BrandModel) {
        val info = BrandCacheInfo.from(brand)
        localCache.put(brandId, info)

        try {
            val key = "$DETAIL_KEY_PREFIX$brandId"
            val json = objectMapper.writeValueAsString(info)
            redisTemplate.opsForValue().set(key, json, detailTtl())
        } catch (_: Exception) {
        }
    }

    fun evictDetail(brandId: Long) {
        localCache.invalidate(brandId)

        try {
            redisTemplate.delete("$DETAIL_KEY_PREFIX$brandId")
        } catch (_: Exception) {
        }
    }

    private fun detailTtl(): Duration =
        DETAIL_BASE_TTL.plusSeconds(Random.nextLong(-DETAIL_JITTER_SECONDS, DETAIL_JITTER_SECONDS + 1))
}

data class BrandCacheInfo(
    val id: Long = 0,
    val name: String = "",
) {
    companion object {
        fun from(brand: com.loopers.domain.brand.BrandModel): BrandCacheInfo =
            BrandCacheInfo(id = brand.id, name = brand.name)
    }
}
