package com.ptit.data.repository

import com.ptit.data.mapping.toDomainEntity
import com.ptit.data.remote.datasource.ProductRemoteDataSource
import com.ptit.data.remote.dto.product.CreateCategoryBodyDto
import com.ptit.data.remote.dto.product.CreateProductRequestDto
import com.ptit.data.remote.dto.product.SKUDto
import com.ptit.data.remote.dto.product.UpdateCategoryBodyDto
import com.ptit.data.remote.dto.product.UpdateProductRequestDto
import com.ptit.data.remote.dto.product.VariantDto
import com.ptit.data.remote.mapper.toDomainEntity as categoryToDomainEntity
import com.ptit.domain.entity.product.CategoryDomainEntity
import com.ptit.domain.entity.product.CreateProductRequestDomainEntity
import com.ptit.domain.entity.product.GetAllCategoriesDomainEntity
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
        page: Int,
        limit: Int,
        sortBy: String,
        sortOrder: String,
        searchQuery: String?,
        minPrice: Int?,
        maxPrice: Int?,
        categoryId: String?,
        brandId: String?
    ): Resource<List<ProductDomainEntity>> =
        remoteDataSource.getProductsByShop(createdById, page, limit, sortBy, sortOrder, searchQuery, minPrice, maxPrice, categoryId, brandId).map { res ->
            // use wrapper mapping
            res.data?.map { it.toDomainEntity() } ?: emptyList()
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
            description = request.description,
            publishedAt = request.publishedAt,
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
                    id = null, // Server will generate ID
                    value = skuDomain.value,
                    price = skuDomain.price,
                    stock = skuDomain.stock,
                    image = skuDomain.image
                )
            },
            specifications = request.specifications.map { specDomain ->
                com.ptit.data.remote.dto.product.SpecificationDto(
                    name = specDomain.name,
                    value = specDomain.value
                )
            }
        )
        return remoteDataSource.createProduct(requestDto).map { it.toDomainEntity() }
    }

    override suspend fun updateProduct(productId: String, request: UpdateProductRequestDomainEntity): Resource<ProductDomainEntity> {
        val requestDto = UpdateProductRequestDto(
            name = request.name,
            description = request.description,
            publishedAt = request.publishedAt,
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
                    id = null, // Server will handle ID
                    value = skuDomain.value,
                    price = skuDomain.price,
                    stock = skuDomain.stock,
                    image = skuDomain.image
                )
            },
            specifications = request.specifications.map { specDomain ->
                com.ptit.data.remote.dto.product.SpecificationDto(
                    name = specDomain.name,
                    value = specDomain.value
                )
            }
        )
        return remoteDataSource.updateProduct(productId, requestDto).map { it.toDomainEntity() }
    }

    override suspend fun deleteProduct(productId: String): Resource<Unit> {
        return remoteDataSource.deleteProduct(productId)
    }

    // ==================== CATEGORY ====================

    override suspend fun getAllCategories(parentCategoryId: String?): Resource<GetAllCategoriesDomainEntity> {
        return remoteDataSource.getAllCategories(parentCategoryId).map {
            it.categoryToDomainEntity()
        }
    }

    override suspend fun getCategoryById(categoryId: String): Resource<CategoryDomainEntity> {
        return remoteDataSource.getCategoryById(categoryId).map {
            it.categoryToDomainEntity()
        }
    }

    override suspend fun createCategory(
        name: String,
        logo: String?,
        parentCategoryId: String?
    ): Resource<CategoryDomainEntity> {
        val body = CreateCategoryBodyDto(
            name = name,
            logo = logo,
            parentCategoryId = parentCategoryId
        )
        return remoteDataSource.createCategory(body).map {
            it.categoryToDomainEntity()
        }
    }

    override suspend fun updateCategory(
        categoryId: String,
        name: String,
        logo: String?,
        parentCategoryId: String?
    ): Resource<CategoryDomainEntity> {
        val body = UpdateCategoryBodyDto(
            name = name,
            logo = logo,
            parentCategoryId = parentCategoryId
        )
        return remoteDataSource.updateCategory(categoryId, body).map {
            it.categoryToDomainEntity()
        }
    }

    override suspend fun deleteCategory(categoryId: String): Resource<String> {
        return remoteDataSource.deleteCategory(categoryId).map {
            it.message ?: "Xóa danh mục thành công"
        }
    }
}