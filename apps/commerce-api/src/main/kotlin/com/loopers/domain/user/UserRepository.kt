package com.loopers.domain.user

interface UserRepository {
    fun save(user: UserModel): UserModel
    fun existsByLoginId(loginId: String): Boolean
}
