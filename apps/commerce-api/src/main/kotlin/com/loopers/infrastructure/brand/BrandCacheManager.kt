package com.loopers.infrastructure.brand

import com.fasterxml.jackson.databind.ObjectMapper
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

    fun getDetail(brandId: Long): BrandCacheInfo? {
        return try {
            val key = "$DETAIL_KEY_PREFIX$brandId"
            val cached = redisTemplate.opsForValue().get(key) ?: return null
            objectMapper.readValue(cached, BrandCacheInfo::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun putDetail(brandId: Long, info: BrandCacheInfo) {
        try {
            val key = "$DETAIL_KEY_PREFIX$brandId"
            val json = objectMapper.writeValueAsString(info)
            redisTemplate.opsForValue().set(key, json, detailTtl())
        } catch (_: Exception) {
        }
    }

    fun evictDetail(brandId: Long) {
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
