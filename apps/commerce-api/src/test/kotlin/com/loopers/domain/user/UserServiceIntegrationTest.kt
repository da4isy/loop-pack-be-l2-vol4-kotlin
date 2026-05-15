package com.loopers.domain.user

import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType
import com.ninjasquad.springmockk.MockkBean
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.slot
import org.junit.jupiter.api.BeforeEach
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

    @MockkBean
    lateinit var passwordEncoder: PasswordEncoder

    @BeforeEach
    fun setUp() {
        every { passwordEncoder.encode(any()) } answers { "encoded-${firstArg<String>()}" }
        every { passwordEncoder.matches(any(), any()) } answers {
            "encoded-${firstArg<String>()}" == secondArg<String>()
        }
    }

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
        fun throw_whenBirthDateInFuture() {
            // given
            val command = SignupCommand(
                loginId = "da4isy",
                password = "Daisyyyy1@@!",
                name = "정다희",
                birthDate = "9999-12-31",
                email = "dahee.jeong123@example.com",
            )
            every { userRepository.existsByLoginId(any()) } returns false

            // when & then
            shouldThrow<CoreException> { userService.signup(command) }
                .errorType shouldBe ErrorType.BAD_REQUEST
        }

        @Test
        fun password_isEncoded_whenSignup() {
            // given
            val command = SignupCommand(
                loginId = "da4isy",
                password = "Daisyyyy1@@!",
                name = "정다희",
                birthDate = "1995-12-03",
                email = "dahee.jeong123@example.com",
            )
            val savedSlot = slot<UserModel>()
            every { userRepository.existsByLoginId(any()) } returns false
            every { userRepository.save(capture(savedSlot)) } answers { savedSlot.captured }

            // when
            userService.signup(command)

            // then
            savedSlot.captured.password shouldNotBe command.password
        }
    }

    @Nested
    inner class GetMe {

        @Test
        fun throw_whenPasswordMismatch() {
            // given
            val savedUser = UserModel(
                loginId = "da4isy",
                password = "encoded-Daisyyyy1@@!",
                name = "정다희",
                birthDate = "1995-12-03",
                email = "dahee.jeong123@example.com",
            )
            every { userRepository.findByLoginId("da4isy") } returns savedUser

            // when & then
            shouldThrow<CoreException> { userService.getMe("da4isy", "wrong-password") }
                .errorType shouldBe ErrorType.UNAUTHORIZED
        }
    }

    @Nested
    inner class ChangePassword {

        @Test
        fun throw_whenCurrentPasswordMismatch() {
            // given
            val savedUser = UserModel(
                loginId = "da4isy",
                password = "encoded-Daisyyyy1@@!",
                name = "정다희",
                birthDate = "1995-12-03",
                email = "dahee.jeong123@example.com",
            )
            every { userRepository.findByLoginId("da4isy") } returns savedUser
            every { userRepository.save(any()) } answers { firstArg() }

            // when & then
            shouldThrow<CoreException> {
                userService.changePassword("da4isy", "wrong-current", "NewPass2@@")
            }.errorType shouldBe ErrorType.UNAUTHORIZED
        }

        @Test
        fun throw_whenNewPasswordSameAsCurrent() {
            // given
            val savedUser = UserModel(
                loginId = "da4isy",
                password = "encoded-Daisyyyy1@@!",
                name = "정다희",
                birthDate = "1995-12-03",
                email = "dahee.jeong123@example.com",
            )
            every { userRepository.findByLoginId("da4isy") } returns savedUser
            every { userRepository.save(any()) } answers { firstArg() }

            // when & then
            shouldThrow<CoreException> {
                userService.changePassword("da4isy", "Daisyyyy1@@!", "Daisyyyy1@@!")
            }.errorType shouldBe ErrorType.BAD_REQUEST
        }
    }
}
