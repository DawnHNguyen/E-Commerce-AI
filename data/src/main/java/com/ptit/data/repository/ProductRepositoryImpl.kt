package com.ptit.data.repository

import com.ptit.data.mapping.toDomainEntity
import com.ptit.data.mapping.toDomainEntity as toListDomainEntity
import com.ptit.data.remote.datasource.ProductRemoteDataSource
import com.ptit.data.remote.dto.product.CreateProductRequestDto
import com.ptit.data.remote.dto.product.SKUDto
import com.ptit.data.remote.dto.product.UpdateProductRequestDto
import com.ptit.data.remote.dto.product.VariantDto
import com.ptit.domain.entity.product.CategoryDomainEntity
import com.ptit.domain.entity.product.CreateProductRequestDomainEntity
import com.ptit.domain.entity.product.ProductDomainEntity
import com.ptit.domain.entity.product.UpdateProductRequestDomainEntity
import com.ptit.domain.repository.ProductRepository
import com.ptit.domain.utils.Resource
import com.ptit.domain.utils.map
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val remoteDataSource: ProductRemoteDataSource,
) : ProductRepository {

    override suspend fun getProductDetail(productId: String): Resource<ProductDomainEntity> {
        return remoteDataSource.getProductDetail(productId).map { it.toDomainEntity() }
    }

    override suspend fun getProductsByShop(
        createdById: String,
        isPublic: Boolean?
    ): Resource<List<ProductDomainEntity>> =
        remoteDataSource.getProductsByShop(createdById, isPublic).map { res ->
            // use wrapper mapping
            res.data?.map { it.toDomainEntity() } ?: emptyList()
        }

    override suspend fun getCategories(): Resource<List<CategoryDomainEntity>> {
        return remoteDataSource.getCategories().map { response ->
            response.map { categoryDto ->
                (categoryDto).toDomainEntity()
            }
        }
    }

    override suspend fun listProducts(
        page: Int,
        limit: Int,
        sortBy: String?,
        orderBy: String?,
        minPrice: Int?,
        maxPrice: Int?,
        name: String?,
        categories: List<String>?,
        brandIds: List<String>?
    ): Resource<List<ProductDomainEntity>> {
        return remoteDataSource.listProducts(
            page,
            limit,
            sortBy,
            orderBy,
            minPrice,
            maxPrice,
            name,
            categories,
            brandIds
        ).map { listProductResponse ->
            listProductResponse.data?.map { it.toDomainEntity() } ?: emptyList()
        }
    }

    override suspend fun createProduct(request: CreateProductRequestDomainEntity): Resource<ProductDomainEntity> {
        val requestDto = CreateProductRequestDto(
            name = request.name,
            basePrice = request.basePrice,
            virtualPrice = request.virtualPrice,
            brandId = request.brandId,
            categoryId = request.categoryId,
            images = request.images,
            variants = request.variants.map { variantDomain ->
                VariantDto(
                    name = variantDomain.name,
                    options = variantDomain.options
                )
            },
            skus = request.skus.map { skuDomain ->
                SKUDto(
                    id = skuDomain.id,
                    price = skuDomain.price,
                    stock =  skuDomain.stock,
                    value = skuDomain.value,
                    image = skuDomain.image
                )
            },
            publishedAt = request.publishedAt,
        )
        return remoteDataSource.createProduct(requestDto).map { it.toDomainEntity() }
    }

    override suspend fun updateProduct(productId: String, request: UpdateProductRequestDomainEntity): Resource<ProductDomainEntity> {
        val requestDto = UpdateProductRequestDto(
            name = request.name,
            basePrice = request.basePrice,
            virtualPrice = request.virtualPrice,
            brandId = request.brandId,
            categoryId = request.categoryId,
            images = request.images,
            variants = request.variants.map { variantDomain ->
                VariantDto(
                    name = variantDomain.name,
                    options = variantDomain.options
                )
            },
            skus = request.skus.map { skuDomain ->
                SKUDto(
                    id = skuDomain.id,
                    price = skuDomain.price,
                    stock =  skuDomain.stock,
                    value = skuDomain.value,
                    image = skuDomain.image
                )
            },
            publishedAt = request.publishedAt,
        )
        return remoteDataSource.updateProduct(productId, requestDto).map { it.toDomainEntity() }
    }


    override suspend fun deleteProduct(productId: String): Resource<Unit> {
        return remoteDataSource.deleteProduct(productId)
    }



}