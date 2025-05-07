package com.ptit.data.repository

import android.util.Log
import com.ptit.data.mapping.toDomainEntity
import com.ptit.data.remote.api.RecommendApi // Thêm import
import com.ptit.data.remote.datasource.ProductRemoteDataSource
import com.ptit.data.remote.dto.product.CategoryDto
import com.ptit.domain.entity.product.CategoryDomainEntity
import com.ptit.domain.entity.product.ProductDomainEntity
import com.ptit.domain.repository.ProductRepository
import com.ptit.domain.utils.Resource
import com.ptit.domain.utils.map
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val remoteDataSource: ProductRemoteDataSource,
    private val recommendApi: RecommendApi // Inject RecommendApi
) : ProductRepository {

    override suspend fun getProductDetail(productId: String): Resource<ProductDomainEntity> {
        return remoteDataSource.getProductDetail(productId).map { it.toDomainEntity() }
    }

    override suspend fun getProductsByShop(): Resource<List<ProductDomainEntity>> {
        return remoteDataSource.getProductsByShop().map { response -> response.map { it.toDomainEntity() } }
    }

    // Implement function mới
    override suspend fun getSimilarProducts(productId: String, amount: Int): Resource<List<ProductDomainEntity>> {
        return remoteDataSource.getSimilarProducts(productId, amount).map { productDtoList ->
            productDtoList.map { it.toDomainEntity() }
        }
    }

    override suspend fun getAllProducts(): Resource<List<ProductDomainEntity>> {
        return remoteDataSource.getAllProducts(limit = 1000).map { listProductResponse ->
            listProductResponse.products?.map { it.toDomainEntity() } ?: emptyList()
        }
    }

    override suspend fun getCategories(): Resource<List<CategoryDomainEntity>> {
        return remoteDataSource.getCategories().map { response ->
            response.map { categoryDto ->
                (categoryDto as CategoryDto).toDomainEntity()
            }
        }
    }

    override suspend fun createProduct(name: String, description: String, price: Int, priceBeforeDiscount: Int, quantity: Int, images: List<String>, image: String, category: String): Resource<ProductDomainEntity> {
        return remoteDataSource.createProduct(name, description, price, priceBeforeDiscount, quantity, images, image, category).map { it.toDomainEntity() }
    }

    override suspend fun updateProduct(productId: String, name: String, description: String, price: Int, priceBeforeDiscount: Int, quantity: Int, images: List<String>, image: String, category: String): Resource<ProductDomainEntity> {
        return remoteDataSource.updateProduct(productId, name, description, price, priceBeforeDiscount, quantity, images, image, category).map { it.toDomainEntity() }
    }

    override suspend fun deleteProduct(productId: String): Resource<Unit> {
        return remoteDataSource.deleteProduct(productId).map { it }
    }

}