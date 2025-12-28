package com.ptit.data.repository

import android.util.Log
import com.ptit.data.remote.api.RecommendationApi
import com.ptit.domain.entity.recommendation.RecommendedProductDomainEntity
import com.ptit.domain.repository.RecommendationRepository
import com.ptit.domain.utils.Resource
import com.ptit.domain.utils.UnknownException
import javax.inject.Inject

class RecommendationRepositoryImpl @Inject constructor(
    private val recommendationApi: RecommendationApi
) : RecommendationRepository {

    override suspend fun getRecommendations(
        userId: String,
        limit: Int
    ): Resource<List<RecommendedProductDomainEntity>> {
        Log.d("RecommendationRepo", "getRecommendations called for userId: $userId, limit: $limit")
        return try {
            val response = recommendationApi.getRecommendations(userId, limit)

            Log.d("RecommendationRepo", "✅ API Response Success")
            Log.d("RecommendationRepo", "Status code: ${response.statusCode}")
            Log.d("RecommendationRepo", "Total: ${response.data.total}")
            Log.d("RecommendationRepo", "Message: ${response.data.message}")

            val productsList = response.data.data
            Log.d("RecommendationRepo", "Products list size: ${productsList.size}")

            val recommendations = productsList.mapIndexed { index, dto ->
                Log.d("RecommendationRepo", "Mapping product $index: ${dto.name}")
                RecommendedProductDomainEntity(
                    id = dto.id,
                    name = dto.name,
                    description = dto.description,
                    basePrice = dto.basePrice,
                    virtualPrice = dto.virtualPrice,
                    images = dto.images,
                    categoryId = dto.categoryId,
                    brandId = dto.brandId,
                    score = dto.score,
                    brandName = dto.brand.name,
                    brandLogo = dto.brand.logo,
                    categoryName = dto.category?.name
                )
            }
            Log.d("RecommendationRepo", "✅ Successfully mapped ${recommendations.size} recommendations")
            Resource.Success(recommendations)
        } catch (e: Exception) {
            Log.e("RecommendationRepo", "❌ Exception in getRecommendations: ${e.message}", e)
            Log.e("RecommendationRepo", "Exception type: ${e::class.java.simpleName}")
            Resource.Error(UnknownException(null, e.message ?: "Unknown error", "recommendations"))
        }
    }
}

