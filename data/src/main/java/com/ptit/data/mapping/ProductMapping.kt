package com.ptit.data.mapping

import com.ptit.data.remote.dto.home.ListProductResponse
import com.ptit.data.remote.dto.product.BrandDto
import com.ptit.data.remote.dto.product.CategoryDto
import com.ptit.data.remote.dto.product.ProductDto
import com.ptit.data.remote.dto.product.SKUDto
import com.ptit.data.remote.dto.product.ShopInfoDto
import com.ptit.data.remote.dto.product.VariantDto
import com.ptit.domain.entity.common.UserDomainEntity
import com.ptit.domain.entity.home.ListProductDomainEntity
import com.ptit.domain.entity.product.BrandDomainEntity
import com.ptit.domain.entity.product.CategoryDomainEntity
import com.ptit.domain.entity.product.ProductDomainEntity
import com.ptit.domain.entity.product.SKUDomainEntity
import com.ptit.domain.entity.product.ShopInfoDomainEntity
import com.ptit.domain.entity.product.VariantDomainEntity

fun ProductDto.toDomainEntity() = ProductDomainEntity(
    id = id.orEmpty(),
    name = name.orEmpty(),
    basePrice = basePrice ?: 0,
    virtualPrice = virtualPrice,
    images = images ?: emptyList(),
    variants = variants?.map { it.toDomainEntity() } ?: emptyList(),
    skus = skus?.map { it.toDomainEntity() } ?: emptyList(),
    category = category?.toDomainEntity(),
    brand = brand?.toDomainEntity(),
    createdById = createdById.orEmpty(),
    shopInfo = shopInfo?.toDomainEntity(),
    isPublic = isPublic ?: false,
    publishedAt = publishedAt,
    createdAt = createdAt.orEmpty(),
    updatedAt = updatedAt.orEmpty(),
    sold = sold ?: 0,
    rating = rating ?: 0f,
    view = view ?: 0,
)

fun CategoryDto.toDomainEntity() = CategoryDomainEntity(
    id = id ?: "",
    name = name ?: "",
)

fun VariantDto.toDomainEntity() = VariantDomainEntity(
    name = name,
    options = options
)

fun SKUDto.toDomainEntity() = SKUDomainEntity(
    id = id.orEmpty(),
    value = value,
    price = price,
    stock = stock,
    image = image
)

fun BrandDto.toDomainEntity() = BrandDomainEntity(
    id = id.orEmpty(),
    name = name.orEmpty(),
)

fun ListProductResponse.toDomainEntity() = ListProductDomainEntity(
    products = data?.map { it.toDomainEntity() } ?: emptyList()  // ✅ Đổi từ products thành data
)

fun ShopInfoDto.toDomainEntity() = ShopInfoDomainEntity(
    id = id.orEmpty(),
    name = name.orEmpty(),
    avatar = avatar.orEmpty(),
    productsCount = productsCount ?: 0
)