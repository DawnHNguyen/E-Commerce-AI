package com.ptit.core.home

import com.ptit.domain.entity.common.UserDomainEntity
import com.ptit.domain.entity.product.CategoryDomainEntity
import com.ptit.domain.entity.product.ProductDomainEntity
import org.junit.Assert.*
import org.junit.Test

class HomeUiStateTest {

    // Mock Data
    private val mockUser = UserDomainEntity(
        id = "user1",
        name = "Test Shop Owner",
        phone = "0123456789",
        email = "shop@example.com",
        address = "123 Shop Street",
        shop = UserDomainEntity.Shop(
            name = "Test Shop",
            address = "123 Shop Street",
            phone = "0123456789",
            description = "Test Shop Description",
            avatar = "shop_avatar.jpg"
        )
    )

    private val mockTrendingProduct1 = ProductDomainEntity(
        id = "trending1",
        name = "Trending Product 1",
        price = 100000,
        priceBeforeDiscount = 120000,
        image = "trending1.jpg",
        images = listOf("trending1.jpg", "trending1_2.jpg"),
        quantity = 10,
        rating = 4.5f,
        sold = 100,
        view = 200,
        description = "Trending Product 1 description",
        category = CategoryDomainEntity(),
        shop = mockUser,
        createdAt = "2023-01-01",
        updatedAt = "2023-01-02"
    )

    private val mockTrendingProduct2 = ProductDomainEntity(
        id = "trending2",
        name = "Trending Product 2",
        price = 200000,
        priceBeforeDiscount = 250000,
        image = "trending2.jpg",
        images = listOf("trending2.jpg"),
        quantity = 5,
        rating = 4.8f,
        sold = 80,
        view = 150,
        description = "Trending Product 2 description",
        category = CategoryDomainEntity(),
        shop = mockUser,
        createdAt = "2023-01-01",
        updatedAt = "2023-01-02"
    )

    private val mockRecommendedProduct1 = ProductDomainEntity(
        id = "recommended1",
        name = "Recommended Product 1",
        price = 150000,
        priceBeforeDiscount = 180000,
        image = "recommended1.jpg",
        images = listOf("recommended1.jpg"),
        quantity = 15,
        rating = 4.2f,
        sold = 60,
        view = 120,
        description = "Recommended Product 1 description",
        category = CategoryDomainEntity(),
        shop = mockUser,
        createdAt = "2023-01-01",
        updatedAt = "2023-01-02"
    )

    private val mockRecommendedProduct2 = ProductDomainEntity(
        id = "recommended2",
        name = "Recommended Product 2",
        price = 300000,
        priceBeforeDiscount = 350000,
        image = "recommended2.jpg",
        images = listOf("recommended2.jpg"),
        quantity = 8,
        rating = 4.7f,
        sold = 40,
        view = 90,
        description = "Recommended Product 2 description",
        category = CategoryDomainEntity(),
        shop = mockUser,
        createdAt = "2023-01-01",
        updatedAt = "2023-01-02"
    )

    @Test
    fun `default state has expected values`() {
        val state = HomeUiState()

        assertTrue(state.trendingProducts.isEmpty())
        assertTrue(state.recommendedProducts.isEmpty())
        assertFalse(state.isLoadingTrending)
        assertFalse(state.isLoadingRecommended)
        assertNull(state.errorTrending)
        assertNull(state.errorRecommended)
    }

    @Test
    fun `copy method maintains all fields and allows partial updates`() {
        val initialState = HomeUiState(
            trendingProducts = listOf(mockTrendingProduct1),
            recommendedProducts = listOf(mockRecommendedProduct1),
            isLoadingTrending = true,
            isLoadingRecommended = true,
            errorTrending = "Trending error",
            errorRecommended = "Recommended error"
        )

        val updatedState = initialState.copy(
            isLoadingTrending = false,
            trendingProducts = listOf(mockTrendingProduct1, mockTrendingProduct2)
        )

        // Updated fields
        assertFalse(updatedState.isLoadingTrending)
        assertEquals(2, updatedState.trendingProducts.size)
        assertEquals(mockTrendingProduct1, updatedState.trendingProducts[0])
        assertEquals(mockTrendingProduct2, updatedState.trendingProducts[1])

        // Unchanged fields
        assertEquals(1, updatedState.recommendedProducts.size)
        assertEquals(mockRecommendedProduct1, updatedState.recommendedProducts[0])
        assertTrue(updatedState.isLoadingRecommended)
        assertEquals("Trending error", updatedState.errorTrending)
        assertEquals("Recommended error", updatedState.errorRecommended)
    }

    @Test
    fun `state with trending products data`() {
        val trendingProducts = listOf(mockTrendingProduct1, mockTrendingProduct2)
        val state = HomeUiState(
            trendingProducts = trendingProducts,
            isLoadingTrending = false,
            errorTrending = null
        )

        assertEquals(2, state.trendingProducts.size)
        assertFalse(state.isLoadingTrending)
        assertNull(state.errorTrending)
        assertTrue(state.trendingProducts.contains(mockTrendingProduct1))
        assertTrue(state.trendingProducts.contains(mockTrendingProduct2))
    }

    @Test
    fun `state with recommended products data`() {
        val recommendedProducts = listOf(mockRecommendedProduct1, mockRecommendedProduct2)
        val state = HomeUiState(
            recommendedProducts = recommendedProducts,
            isLoadingRecommended = false,
            errorRecommended = null
        )

        assertEquals(2, state.recommendedProducts.size)
        assertFalse(state.isLoadingRecommended)
        assertNull(state.errorRecommended)
        assertTrue(state.recommendedProducts.contains(mockRecommendedProduct1))
        assertTrue(state.recommendedProducts.contains(mockRecommendedProduct2))
    }

    @Test
    fun `state handles loading states correctly`() {
        val loadingState = HomeUiState(
            isLoadingTrending = true,
            isLoadingRecommended = true
        )

        assertTrue(loadingState.isLoadingTrending)
        assertTrue(loadingState.isLoadingRecommended)
        assertNull(loadingState.errorTrending)
        assertNull(loadingState.errorRecommended)
    }

    @Test
    fun `state handles error states correctly`() {
        val errorState = HomeUiState(
            isLoadingTrending = false,
            isLoadingRecommended = false,
            errorTrending = "Failed to load trending products",
            errorRecommended = "Failed to load recommended products"
        )

        assertFalse(errorState.isLoadingTrending)
        assertFalse(errorState.isLoadingRecommended)
        assertEquals("Failed to load trending products", errorState.errorTrending)
        assertEquals("Failed to load recommended products", errorState.errorRecommended)
    }

    @Test
    fun `state handles mixed loading and error states`() {
        val mixedState = HomeUiState(
            trendingProducts = listOf(mockTrendingProduct1),
            recommendedProducts = emptyList(),
            isLoadingTrending = false,
            isLoadingRecommended = true,
            errorTrending = null,
            errorRecommended = "Recommended error"
        )

        assertEquals(1, mixedState.trendingProducts.size)
        assertTrue(mixedState.recommendedProducts.isEmpty())
        assertFalse(mixedState.isLoadingTrending)
        assertTrue(mixedState.isLoadingRecommended)
        assertNull(mixedState.errorTrending)
        assertEquals("Recommended error", mixedState.errorRecommended)
    }

    @Test
    fun `state handles empty product lists`() {
        val emptyState = HomeUiState(
            trendingProducts = emptyList(),
            recommendedProducts = emptyList(),
            isLoadingTrending = false,
            isLoadingRecommended = false,
            errorTrending = null,
            errorRecommended = null
        )

        assertTrue(emptyState.trendingProducts.isEmpty())
        assertTrue(emptyState.recommendedProducts.isEmpty())
        assertFalse(emptyState.isLoadingTrending)
        assertFalse(emptyState.isLoadingRecommended)
        assertNull(emptyState.errorTrending)
        assertNull(emptyState.errorRecommended)
    }
}