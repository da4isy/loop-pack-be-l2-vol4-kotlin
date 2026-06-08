package com.loopers.domain.order

import com.loopers.domain.brand.BrandModel
import com.loopers.domain.product.ProductModel
import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType
import org.springframework.stereotype.Component

/**
 * Domain Service — 주문 생성 협력 로직.
 * 스냅샷 빌드 + 재고 차감을 도메인 레벨에서 처리.
 */
@Component
class OrderCreationService {

    fun buildOrder(
        userId: Long,
        products: List<ProductModel>,
        brands: Map<Long, BrandModel>,
        quantities: List<Long>,
    ): OrderModel {
        val orderItems = products.mapIndexed { index, product ->
            val brand = brands[product.brandId]
                ?: throw CoreException(
                    errorType = ErrorType.NOT_FOUND,
                    customMessage = "브랜드 정보를 찾을 수 없습니다.",
                )
            product.decreaseStock(quantities[index])
            OrderItemModel(
                productId = product.id,
                productName = product.name,
                productPrice = product.price,
                brandName = brand.name,
                quantity = quantities[index],
            )
        }
        return OrderModel.create(userId = userId, items = orderItems)
    }
}
