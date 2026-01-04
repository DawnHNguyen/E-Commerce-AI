package com.ptit.domain.repository

import com.ptit.domain.entity.recommendation.RecommendedProductDomainEntity
import com.ptit.domain.utils.Resource

interface RecommendationRepository {
    suspend fun getRecommendations(userId: String, limit: Int): Resource<List<RecommendedProductDomainEntity>>

    suspend fun getProductRecommendations(productId: String, limit: Int): Resource<List<RecommendedProductDomainEntity>>
}

