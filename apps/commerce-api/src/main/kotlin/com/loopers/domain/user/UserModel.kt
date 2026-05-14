package com.loopers.domain.user

import com.loopers.domain.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "users")
class UserModel(
    loginId: String,
    password: String,
    name: String,
    birthDate: String,
    email: String,
) : BaseEntity() {

    @Column(name = "login_id", nullable = false, unique = true, length = 20)
    var loginId: String = loginId
        protected set

    @Column(name = "password", nullable = false)
    var password: String = password
        protected set

    @Column(name = "name", nullable = false)
    var name: String = name
        protected set

    @Column(name = "birth_date", nullable = false)
    var birthDate: String = birthDate
        protected set

    @Column(name = "email", nullable = false)
    var email: String = email
        protected set

    fun maskedName(): String = if (name.length <= 1) "*" else name.dropLast(1) + "*"
}
