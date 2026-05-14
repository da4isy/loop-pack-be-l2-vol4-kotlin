package com.loopers.domain.user

import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType
import com.ninjasquad.springmockk.MockkBean
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ContextConfiguration(classes = [UserService::class])
@ExtendWith(SpringExtension::class)
class UserServiceIntegrationTest @Autowired constructor(
    private val userService: UserService,
) {

    @MockkBean
    lateinit var userRepository: UserRepository

    @Nested
    inner class Signup {

        @Test
        fun throw_whenLoginIdDuplicated() {
            // given
            val command = SignupCommand(
                loginId = "da4isy",
                password = "Daisyyyy1@@!",
                name = "정다희",
                birthDate = "1995-12-03",
                email = "dahee.jeong123@example.com",
            )
            val savedUser = UserModel(
                loginId = command.loginId,
                password = command.password,
                name = command.name,
                birthDate = command.birthDate,
                email = command.email,
            )
            every { userRepository.existsByLoginId(command.loginId) } returns false andThen true
            every { userRepository.save(any()) } returns savedUser
            userService.signup(command)

            // when & then
            shouldThrow<CoreException> { userService.signup(command) }
                .errorType shouldBe ErrorType.CONFLICT
        }

        @Test
        fun throw_whenPasswordViolatesRule() {
            // given
            val command = SignupCommand(
                loginId = "da4isy",
                password = "abc",
                name = "정다희",
                birthDate = "1995-12-03",
                email = "dahee.jeong123@example.com",
            )
            every { userRepository.existsByLoginId(any()) } returns false

            // when & then
            shouldThrow<CoreException> { userService.signup(command) }
                .errorType shouldBe ErrorType.BAD_REQUEST
        }

        @Test
        fun throw_whenLoginIdViolatesRule() {
            // given
            val command = SignupCommand(
                loginId = "da4isy!",
                password = "Daisyyyy1@@!",
                name = "정다희",
                birthDate = "1995-12-03",
                email = "dahee.jeong123@example.com",
            )
            val savedUser = UserModel(
                loginId = command.loginId,
                password = command.password,
                name = command.name,
                birthDate = command.birthDate,
                email = command.email,
            )
            every { userRepository.existsByLoginId(any()) } returns false
            every { userRepository.save(any()) } returns savedUser

            // when & then
            shouldThrow<CoreException> { userService.signup(command) }
                .errorType shouldBe ErrorType.BAD_REQUEST
        }

        @Test
        fun throw_whenEmailViolatesRule() {
            // given
            val command = SignupCommand(
                loginId = "da4isy",
                password = "Daisyyyy1@@!",
                name = "정다희",
                birthDate = "1995-12-03",
                email = "invalid-email",
            )
            val savedUser = UserModel(
                loginId = command.loginId,
                password = command.password,
                name = command.name,
                birthDate = command.birthDate,
                email = command.email,
            )
            every { userRepository.existsByLoginId(any()) } returns false
            every { userRepository.save(any()) } returns savedUser

            // when & then
            shouldThrow<CoreException> { userService.signup(command) }
                .errorType shouldBe ErrorType.BAD_REQUEST
        }
    }
}
