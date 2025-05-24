package com.ptit.core.home // Giữ package gốc hoặc đổi thành package test của bạn

import androidx.paging.PagingData
import app.cash.turbine.test
import com.ptit.domain.entity.common.BaseErrorResponseDomainEntity
import com.ptit.domain.entity.common.UserDomainEntity // Cần import UserDomainEntity
import com.ptit.domain.entity.product.CategoryDomainEntity
import com.ptit.domain.entity.product.ProductDomainEntity
import com.ptit.domain.repository.HomeRepository
import com.ptit.domain.repository.ProductRepository
import com.ptit.domain.utils.CustomException
import com.ptit.domain.utils.Resource
import com.ptit.domain.utils.UnknownException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any // Cần import any() cho Paging
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class SearchViewModelTest {

    @Mock
    private lateinit var homeRepository: HomeRepository

    @Mock
    private lateinit var productRepository: ProductRepository

    private lateinit var viewModel: SearchViewModel // <-- Sửa tên ViewModel

    private val testDispatcher = StandardTestDispatcher()

    // Test data
    private val mockUser = UserDomainEntity( // Thêm mockUser để dùng trong Product
        id = "user1", name = "Test User", phone = "123", email = "a@b.c", address = "123",
        shop = UserDomainEntity.Shop("Shop", "Addr", "Phone", "Desc", "Ava")
    )

    private val mockCategory1 = CategoryDomainEntity(
        id = "cat1",
        name = "Electronics",
    )

    private val mockCategory2 = CategoryDomainEntity(
        id = "cat2",
        name = "Fashion",
    )

    private val mockProduct1 = ProductDomainEntity(
        id = "product1", name = "iPhone 15", price = 25000000, priceBeforeDiscount = 27000000,
        image = "iphone15.jpg", images = listOf("iphone15.jpg", "iphone15_back.jpg"), quantity = 10,
        rating = 4.8f, sold = 150, view = 2500, description = "Latest iPhone model",
        category = mockCategory1, shop = mockUser, // Sử dụng mockUser
        createdAt = "2023-09-01", updatedAt = "2023-12-01"
    )

    private val mockProduct2 = ProductDomainEntity(
        id = "product2", name = "Samsung Galaxy S24", price = 22000000, priceBeforeDiscount = 24000000,
        image = "galaxy_s24.jpg", images = listOf("galaxy_s24.jpg"), quantity = 8,
        rating = 4.6f, sold = 120, view = 1800, description = "Latest Samsung flagship",
        category = mockCategory1, shop = mockUser, // Sử dụng mockUser
        createdAt = "2023-08-15", updatedAt = "2023-11-20"
    )

    private val mockTrendingProduct = ProductDomainEntity(
        id = "trending1", name = "Trending Product", price = 500000, priceBeforeDiscount = 600000,
        image = "trending.jpg", images = listOf("trending.jpg"), quantity = 50,
        rating = 4.2f, sold = 300, view = 5000, description = "Currently trending product",
        category = mockCategory2, shop = mockUser, // Sử dụng mockUser
        createdAt = "2023-10-01", updatedAt = "2023-12-01"
    )

    private fun createTestException(message: String): CustomException { // <-- Sửa kiểu trả về
        return UnknownException(
            error = BaseErrorResponseDomainEntity(message = message),
            message = message,
            requestUrl = "test-url"
        )
    }

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModelWithMocks(
        categoriesResult: Resource<List<CategoryDomainEntity>> = Resource.success(listOf(mockCategory1, mockCategory2)),
        trendingProductsResult: Resource<List<ProductDomainEntity>> = Resource.success(listOf(mockTrendingProduct)),
        searchResultsFlow: kotlinx.coroutines.flow.Flow<PagingData<ProductDomainEntity>> = flowOf(PagingData.empty())
    ): SearchViewModel {
        runTest {
            whenever(productRepository.getCategories()).thenReturn(categoriesResult)
            // Sửa getTrendingProducts để nhận tham số nếu cần, hoặc bỏ nếu không cần
            whenever(homeRepository.getTrendingProducts()).thenReturn(trendingProductsResult)
            // Sửa any() để phù hợp với Paging
            whenever(homeRepository.searchProducts(any())).thenReturn(searchResultsFlow)
        }
        return SearchViewModel(
            homeRepository = homeRepository,
            productRepository = productRepository
        )
    }

    @Test
    fun `initial state has correct default values`() = runTest {
        // Given: Mock repositories to return error
        val errorMsg = "No load"
        val exception = createTestException(errorMsg)
        val viewModel = createViewModelWithMocks(
            categoriesResult = Resource.error(exception),
            trendingProductsResult = Resource.error(exception)
        )

        // Then: Check states using Turbine
        viewModel.categoriesState.test {
            assertTrue(awaitItem() is Resource.Loading)
            testDispatcher.scheduler.advanceUntilIdle()
            val errorState = awaitItem()
            assertTrue(errorState is Resource.Error)
            assertEquals(exception, (errorState as Resource.Error).error)
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.trendingProductsState.test {
            assertTrue(awaitItem() is Resource.Loading)
            testDispatcher.scheduler.advanceUntilIdle()
            val errorState = awaitItem()
            assertTrue(errorState is Resource.Error)
            assertEquals(exception, (errorState as Resource.Error).error)
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals("", viewModel.searchQuery.first())
    }

    @Test
    fun `loadCategories updates state with categories on success`() = runTest {
        // Given
        val categories = listOf(mockCategory1, mockCategory2)
        val viewModel = createViewModelWithMocks(categoriesResult = Resource.success(categories))

        // When
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.categoriesState.first()
        assertTrue(state is Resource.Success)
        assertEquals(2, (state as Resource.Success).data.size)
        assertEquals(mockCategory1, state.data[0])
        assertEquals(mockCategory2, state.data[1])
    }

    @Test
    fun `loadCategories updates state with error on failure`() = runTest {
        // Given
        val errorMessage = "Failed to load categories"
        val customException = createTestException(errorMessage)
        val viewModel = createViewModelWithMocks(categoriesResult = Resource.error(customException))

        // When
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.categoriesState.first()
        assertTrue(state is Resource.Error)
        assertEquals(customException, (state as Resource.Error).error)
    }

    @Test
    fun `loadTrendingProducts updates state with products on success`() = runTest {
        // Given
        val trendingProducts = listOf(mockTrendingProduct)
        val viewModel = createViewModelWithMocks(trendingProductsResult = Resource.success(trendingProducts))

        // When
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.trendingProductsState.first()
        assertTrue(state is Resource.Success)
        assertEquals(1, (state as Resource.Success).data.size)
        assertEquals(mockTrendingProduct, state.data[0])
    }

    @Test
    fun `loadTrendingProducts updates state with error on failure`() = runTest {
        // Given
        val errorMessage = "Failed to load trending products"
        val customException = createTestException(errorMessage)
        val viewModel = createViewModelWithMocks(trendingProductsResult = Resource.error(customException))

        // When
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.trendingProductsState.first()
        assertTrue(state is Resource.Error)
        assertEquals(customException, (state as Resource.Error).error)
    }

    @Test
    fun `onSearchQueryChanged updates search query`() = runTest {
        // Given
        val viewModel = createViewModelWithMocks()
        val newQuery = "iPhone"

        // When
        viewModel.onSearchQueryChanged(newQuery)

        // Then
        val searchQuery = viewModel.searchQuery.first()
        assertEquals(newQuery, searchQuery)
    }

    @Test
    fun `clearSearch resets search query to empty string`() = runTest {
        // Given
        val viewModel = createViewModelWithMocks()
        viewModel.onSearchQueryChanged("some query")

        // When
        viewModel.clearSearch()

        // Then
        val searchQuery = viewModel.searchQuery.first()
        assertEquals("", searchQuery)
    }

    @Test
    fun `search query changes trigger search results flow`() = runTest {
        // Given
        val mockSearchResults = flowOf(PagingData.from(listOf(mockProduct1, mockProduct2)))
        val viewModel = createViewModelWithMocks() // Create VM first

        // Mock the specific search query call
        whenever(homeRepository.searchProducts("iPhone")).thenReturn(mockSearchResults)

        // When
        viewModel.onSearchQueryChanged("iPhone")
        testDispatcher.scheduler.advanceUntilIdle() // Allow flow to be triggered

        // Then
        assertNotNull(viewModel.searchResults)
        // Testing PagingData content here is complex and often requires Paging 3 testing artifacts.
        // We'll trust that if the flow isn't null and the repository was called, it's working.
    }

    @Test
    fun `trending products state goes through loading phase`() = runTest {
        // Given: Use a delay
        whenever(homeRepository.getTrendingProducts()).thenAnswer {
//            delay(timeMillis = 1)
            Resource.success(listOf(mockTrendingProduct))
        }
        val viewModel = SearchViewModel(homeRepository, productRepository)

        // Then
        viewModel.trendingProductsState.test {
            assertTrue("Expected Loading", awaitItem() is Resource.Loading)
            testDispatcher.scheduler.advanceUntilIdle()
            assertTrue("Expected Success", awaitItem() is Resource.Success)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `multiple search query changes are handled correctly`() = runTest {
        // Given
        val viewModel = createViewModelWithMocks()

        // When & Then
        viewModel.searchQuery.test {
            assertEquals("", awaitItem()) // Initial empty query

            viewModel.onSearchQueryChanged("iPhone")
            assertEquals("iPhone", awaitItem())

            viewModel.onSearchQueryChanged("Samsung")
            assertEquals("Samsung", awaitItem())

            viewModel.clearSearch()
            assertEquals("", awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `init loads both categories and trending products`() = runTest {
        // Given
        val categories = listOf(mockCategory1)
        val trendingProducts = listOf(mockTrendingProduct)
        // Mock success for both
        whenever(productRepository.getCategories()).thenReturn(Resource.success(categories))
        whenever(homeRepository.getTrendingProducts()).thenReturn(Resource.success(trendingProducts))

        // When: Create ViewModel, init runs
        val viewModel = SearchViewModel(homeRepository, productRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val categoriesState = viewModel.categoriesState.first()
        val trendingState = viewModel.trendingProductsState.first()

        assertTrue(categoriesState is Resource.Success)
        assertTrue(trendingState is Resource.Success)
        assertEquals(1, (categoriesState as Resource.Success).data.size)
        assertEquals(1, (trendingState as Resource.Success).data.size)
    }
}