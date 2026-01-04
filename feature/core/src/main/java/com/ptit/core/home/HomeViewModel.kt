package com.ptit.core.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ptit.domain.entity.product.ProductDomainEntity
import com.ptit.domain.entity.recommendation.RecommendedProductDomainEntity
import com.ptit.domain.repository.HomeRepository
import com.ptit.domain.repository.RecommendationRepository
import com.ptit.domain.repository.UserRepository
import com.ptit.domain.utils.Resource
import com.ptit.domain.utils.UnknownException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val recommendationRepository: RecommendationRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    val products: Flow<PagingData<ProductDomainEntity>> =
        homeRepository.paginatedRecommendedProduct()
            .cachedIn(viewModelScope)

    private val _recommendationsState = MutableStateFlow<Resource<List<RecommendedProductDomainEntity>>>(Resource.Loading())
    val recommendationsState: StateFlow<Resource<List<RecommendedProductDomainEntity>>> = _recommendationsState.asStateFlow()

    init {
        fetchRecommendations()
    }

    fun fetchRecommendations() {
        viewModelScope.launch {
            Log.d("HomeViewModel", "=== fetchRecommendations started ===")
            Log.d("HomeViewModel", "Setting state to Loading...")
            _recommendationsState.emit(Resource.Loading())
            Log.d("HomeViewModel", "Current state after loading: ${_recommendationsState.value}")

            // Get current user ID
            val userProfile = userRepository.getUserProfile()
            Log.d("HomeViewModel", "User profile result: $userProfile")
            val userId = when (userProfile) {
                is Resource.Success -> {
                    Log.d("HomeViewModel", "User ID: ${userProfile.data.id}")
                    userProfile.data.id
                }
                else -> {
                    Log.e("HomeViewModel", "Failed to get user profile")
                    null
                }
            }

            if (userId != null) {
                Log.d("HomeViewModel", "Fetching recommendations for userId: $userId with limit: 30")
                val result = recommendationRepository.getRecommendations(userId, limit = 30)
                Log.d("HomeViewModel", "Repository result type: ${result::class.java.simpleName}")
                Log.d("HomeViewModel", "Repository result: $result")

                if (result is Resource.Success) {
                    Log.d("HomeViewModel", "✅ Success! Data size: ${result.data.size}")
                    result.data.forEachIndexed { index, product ->
                        Log.d("HomeViewModel", "  Product $index: ${product.name}")
                    }
                }

                Log.d("HomeViewModel", "Setting state to result...")
                _recommendationsState.emit(result)
                Log.d("HomeViewModel", "State after setting: ${_recommendationsState.value}")
            } else {
                Log.e("HomeViewModel", "❌ User not found, setting error state")
                _recommendationsState.emit(Resource.Error(UnknownException(null, "User not found", "recommendations")))
            }

            Log.d("HomeViewModel", "=== fetchRecommendations completed ===")
        }
    }
}