package com.loopers.domain.like

import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ContextConfiguration(classes = [LikeService::class])
@ExtendWith(SpringExtension::class)
class LikeServiceIntegrationTest @Autowired constructor(
    private val likeService: LikeService,
) {

    @MockkBean
    lateinit var likeRepository: LikeRepository

    @Nested
    inner class Like {

        @Test
        fun success_whenNotLikedYet() {
            every { likeRepository.existsByUserIdAndProductId(1L, 10L) } returns false
            every { likeRepository.save(any()) } answers { firstArg() }

            likeService.like(1L, 10L) shouldBe true

            verify(exactly = 1) { likeRepository.save(any()) }
        }

        @Test
        fun idempotent_whenAlreadyLiked() {
            every { likeRepository.existsByUserIdAndProductId(1L, 10L) } returns true

            likeService.like(1L, 10L) shouldBe false

            verify(exactly = 0) { likeRepository.save(any()) }
        }
    }

    @Nested
    inner class Unlike {

        @Test
        fun success_whenLiked() {
            every { likeRepository.deleteByUserIdAndProductId(1L, 10L) } returns 1L

            likeService.unlike(1L, 10L) shouldBe true

            verify(exactly = 1) { likeRepository.deleteByUserIdAndProductId(1L, 10L) }
        }

        @Test
        fun returnsFalse_whenNotLiked() {
            every { likeRepository.deleteByUserIdAndProductId(1L, 10L) } returns 0L

            likeService.unlike(1L, 10L) shouldBe false
        }
    }

    @Nested
    inner class CountByProductId {

        @Test
        fun returnsCount() {
            every { likeRepository.countByProductId(10L) } returns 5

            likeService.countByProductId(10L) shouldBe 5
        }
    }

    @Nested
    inner class CountByProductIds {

        @Test
        fun returnsEmptyMap_whenEmptyInput() {
            likeService.countByProductIds(emptyList()) shouldBe emptyMap()
        }

        @Test
        fun returnsCounts() {
            every { likeRepository.countByProductIds(listOf(1L, 2L)) } returns mapOf(1L to 3L, 2L to 7L)

            val result = likeService.countByProductIds(listOf(1L, 2L))
            result[1L] shouldBe 3
            result[2L] shouldBe 7
        }
    }

    @Nested
    inner class GetLikesByUserId {

        @Test
        fun returnsPageOfLikes() {
            val likes = listOf(LikeModel(userId = 1L, productId = 10L))
            val pageable = PageRequest.of(0, 20)
            every { likeRepository.findAllByUserId(1L, pageable) } returns PageImpl(likes, pageable, 1)

            val result = likeService.getLikesByUserId(1L, pageable)
            result.content.size shouldBe 1
            result.totalElements shouldBe 1
        }
    }
}
