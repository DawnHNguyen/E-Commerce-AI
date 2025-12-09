package com.ptit.core.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.domain.entity.review.CreateReviewMediaDomainEntity
import com.ptit.domain.entity.review.CreateReviewRequestDomainEntity
import com.ptit.domain.entity.review.GetReviewsResponseDomainEntity
import com.ptit.domain.entity.review.ReviewDomainEntity
import com.ptit.domain.entity.review.UpdateReviewRequestDomainEntity
import com.ptit.domain.repository.ReviewRepository
import com.ptit.domain.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val _reviewsState = MutableStateFlow<Resource<GetReviewsResponseDomainEntity>>(Resource.idle())
    val reviewsState = _reviewsState.asStateFlow()

    private val _createReviewState = MutableStateFlow<Resource<ReviewDomainEntity>>(Resource.idle())
    val createReviewState = _createReviewState.asStateFlow()

    private val _updateReviewState = MutableStateFlow<Resource<ReviewDomainEntity>>(Resource.idle())
    val updateReviewState = _updateReviewState.asStateFlow()

    // Map to track which products have been reviewed in an order
    private val _reviewedProducts = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val reviewedProducts = _reviewedProducts.asStateFlow()

    // Map to store existing reviews for products in orders
    private val _existingReviews = MutableStateFlow<Map<String, ReviewDomainEntity>>(emptyMap())
    val existingReviews = _existingReviews.asStateFlow()

    fun checkReviewExists(orderId: String, productId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // Get reviews for the product and check if any matches the orderId
            val response = reviewRepository.getReviews(productId, page = 1, limit = 100)

            android.util.Log.d("ReviewViewModel", "checkReviewExists - orderId: $orderId, productId: $productId")
            android.util.Log.d("ReviewViewModel", "Response type: ${response::class.simpleName}")

            if (response is Resource.Success) {
                android.util.Log.d("ReviewViewModel", "Total reviews: ${response.data.data.size}")
                response.data.data.forEach { review ->
                    android.util.Log.d("ReviewViewModel", "Review orderId: ${review.orderId}, matches: ${review.orderId == orderId}")
                }

                val existingReview = response.data.data.find { review ->
                    review.orderId == orderId
                }
                val key = "${orderId}_${productId}"

                if (existingReview != null) {
                    android.util.Log.d("ReviewViewModel", "✅ Found existing review for key: $key")
                    _reviewedProducts.value = _reviewedProducts.value + (key to true)
                    _existingReviews.value = _existingReviews.value + (key to existingReview)
                } else {
                    android.util.Log.d("ReviewViewModel", "❌ No review found for key: $key")
                    _reviewedProducts.value = _reviewedProducts.value + (key to false)
                }
            } else {
                android.util.Log.e("ReviewViewModel", "Error or not success: ${response}")
                // If error, assume not reviewed to show button (safer UX)
                val key = "${orderId}_${productId}"
                _reviewedProducts.value = _reviewedProducts.value + (key to false)
            }
        }
    }

    fun isProductReviewed(orderId: String, productId: String): Boolean {
        val key = "${orderId}_${productId}"
        return _reviewedProducts.value[key] ?: false
    }

    fun getExistingReview(orderId: String, productId: String): ReviewDomainEntity? {
        val key = "${orderId}_${productId}"
        return _existingReviews.value[key]
    }

    fun getReviews(productId: String, page: Int = 1, limit: Int = 10) {
        if (_reviewsState.value is Resource.Loading) return
        _reviewsState.value = Resource.loading()

        viewModelScope.launch(Dispatchers.IO) {
            val response = reviewRepository.getReviews(productId, page, limit)
            _reviewsState.value = response
        }
    }

    fun createReview(
        content: String,
        rating: Int,
        productId: String,
        orderId: String,
        medias: List<CreateReviewMediaDomainEntity> = emptyList()
    ) {
        if (_createReviewState.value is Resource.Loading) return
        _createReviewState.value = Resource.loading()

        viewModelScope.launch(Dispatchers.IO) {
            val request = CreateReviewRequestDomainEntity(
                content = content,
                rating = rating,
                productId = productId,
                orderId = orderId,
                medias = medias
            )
            val response = reviewRepository.createReview(request)
            _createReviewState.value = response
        }
    }

    fun updateReview(
        reviewId: String,
        content: String,
        rating: Int,
        productId: String,
        orderId: String,
        medias: List<CreateReviewMediaDomainEntity> = emptyList()
    ) {
        if (_updateReviewState.value is Resource.Loading) return
        _updateReviewState.value = Resource.loading()

        viewModelScope.launch(Dispatchers.IO) {
            val request = UpdateReviewRequestDomainEntity(
                content = content,
                rating = rating,
                productId = productId,
                orderId = orderId,
                medias = medias
            )
            val response = reviewRepository.updateReview(reviewId, request)
            _updateReviewState.value = response
        }
    }

    fun resetCreateReviewState() {
        _createReviewState.value = Resource.idle()
    }

    fun resetUpdateReviewState() {
        _updateReviewState.value = Resource.idle()
    }
}

