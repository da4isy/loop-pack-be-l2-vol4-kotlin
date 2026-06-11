package com.loopers.domain.brand

import com.loopers.domain.BaseEntity
import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "brands")
class BrandModel(
    name: String,
) : BaseEntity() {

    @Column(name = "name", nullable = false)
    var name: String = name
        protected set

    init {
        if (name.isBlank()) {
            throw CoreException(errorType = ErrorType.BAD_REQUEST, customMessage = "브랜드 이름은 비어있을 수 없습니다.")
        }
    }

    fun update(name: String) {
        if (name.isBlank()) {
            throw CoreException(errorType = ErrorType.BAD_REQUEST, customMessage = "브랜드 이름은 비어있을 수 없습니다.")
        }
        this.name = name
    }

    fun isDeleted(): Boolean = deletedAt != null
}
