
import app.cash.turbine.test
import com.ptit.core.home.HomeViewModel
import com.ptit.domain.entity.common.BaseErrorResponseDomainEntity
import com.ptit.domain.entity.common.UserDomainEntity
import com.ptit.domain.entity.product.CategoryDomainEntity
import com.ptit.domain.entity.product.ProductDomainEntity
import com.ptit.domain.repository.HomeRepository
import com.ptit.domain.repository.ProductRepository
import com.ptit.domain.utils.Resource
import com.ptit.domain.utils.UnknownException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
import org.mockito.kotlin.whenever
import kotlinx.coroutines.delay

@ExperimentalCoroutinesApi
class HomeViewModelTest {

    @Mock
    private lateinit var homeRepository: HomeRepository

    @Mock
    private lateinit var productRepository: ProductRepository

    private val testDispatcher = StandardTestDispatcher()

    // Test data
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

    private val mockTrendingProducts = listOf(mockTrendingProduct1, mockTrendingProduct2)
    private val mockRecommendedProducts = listOf(mockRecommendedProduct1, mockRecommendedProduct2)

    private fun createTestException(message: String): UnknownException {
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

    private fun createViewModelWithMockedData(
        trendingResult: Resource<List<ProductDomainEntity>> = Resource.success(mockTrendingProducts),
        recommendedResult: Resource<List<ProductDomainEntity>> = Resource.success(mockRecommendedProducts)
    ): HomeViewModel {
        runTest {
            whenever(homeRepository.getTrendingProducts(amount = 3)).thenReturn(trendingResult)
            whenever(productRepository.getHomeRecommendations()).thenReturn(recommendedResult)
        }
        return HomeViewModel(
            homeRepository = homeRepository,
            productRepository = productRepository
        )
    }

    @Test
    fun `initial state has correct default values`() = runTest {
        // Given: Mock repositories to return empty results quickly
        val viewModel = createViewModelWithMockedData(
            trendingResult = Resource.success(emptyList()),
            recommendedResult = Resource.success(emptyList())
        )

        // Then: Check initial state before any loading
        val initialState = viewModel.uiState.first()

        assertTrue(initialState.trendingProducts.isEmpty())
        assertTrue(initialState.recommendedProducts.isEmpty())
        // Note: isLoading might be true initially due to init block
    }

    @Test
    fun `fetchTrendingProducts updates state with success data`() = runTest {
        // Given
        val viewModel = createViewModelWithMockedData()

        // When
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertEquals(2, state.trendingProducts.size)
        assertEquals(mockTrendingProduct1, state.trendingProducts[0])
        assertEquals(mockTrendingProduct2, state.trendingProducts[1])
        assertFalse(state.isLoadingTrending)
        assertNull(state.errorTrending)
    }

    @Test
    fun `fetchTrendingProducts handles error correctly`() = runTest {
        // Given
        val errorMessage = "Failed to load trending products"
        val customException = createTestException(errorMessage)
        val viewModel = createViewModelWithMockedData(
            trendingResult = Resource.error(customException)
        )

        // When
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertTrue(state.trendingProducts.isEmpty())
        assertFalse(state.isLoadingTrending)
        assertEquals(errorMessage, state.errorTrending)
    }

    @Test
    fun `fetchTrendingProducts shows loading state during execution`() = runTest {
        // Given
        val viewModel = createViewModelWithMockedData()

        // Then: Test loading state changes
        viewModel.uiState.test {
            // Initial state might have loading = false
            val initialState = awaitItem()

            // Trigger fetch manually to observe loading
            viewModel.fetchTrendingProducts()

            // Should show loading
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoadingTrending)
            assertNull(loadingState.errorTrending)

            testDispatcher.scheduler.advanceUntilIdle()

            // Should show success
            val successState = awaitItem()
            assertFalse(successState.isLoadingTrending)
            assertEquals(2, successState.trendingProducts.size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `fetchHomeRecommendations updates state with success data`() = runTest {
        // Given
        val viewModel = createViewModelWithMockedData()

        // When
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertEquals(2, state.recommendedProducts.size)
        assertEquals(mockRecommendedProduct1, state.recommendedProducts[0])
        assertEquals(mockRecommendedProduct2, state.recommendedProducts[1])
        assertFalse(state.isLoadingRecommended)
        assertNull(state.errorRecommended)
    }

    @Test
    fun `fetchHomeRecommendations handles error correctly`() = runTest {
        // Given
        val errorMessage = "Failed to load recommended products"
        val customException = createTestException(errorMessage)
        val viewModel = createViewModelWithMockedData(
            recommendedResult = Resource.error(customException)
        )

        // When
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertTrue(state.recommendedProducts.isEmpty())
        assertFalse(state.isLoadingRecommended)
        assertEquals(errorMessage, state.errorRecommended)
    }

    @Test
    fun `fetchHomeRecommendations shows loading state during execution`() = runTest {
        // Given
        val viewModel = createViewModelWithMockedData()

        // Then: Test loading state changes
        viewModel.uiState.test {
            // Initial state
            val initialState = awaitItem()

            // Trigger fetch manually to observe loading
            viewModel.fetchHomeRecommendations()

            // Should show loading
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoadingRecommended)
            assertNull(loadingState.errorRecommended)

            testDispatcher.scheduler.advanceUntilIdle()

            // Should show success
            val successState = awaitItem()
            assertFalse(successState.isLoadingRecommended)
            assertEquals(2, successState.recommendedProducts.size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refreshData calls both fetch methods`() = runTest {
        // Given
        val viewModel = createViewModelWithMockedData()

        // When
        viewModel.refreshData()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertEquals(2, state.trendingProducts.size)
        assertEquals(2, state.recommendedProducts.size)
        assertFalse(state.isLoadingTrending)
        assertFalse(state.isLoadingRecommended)
        assertNull(state.errorTrending)
        assertNull(state.errorRecommended)
    }

    @Test
    fun `refreshData handles mixed success and error states`() = runTest {
        // Given: Trending succeeds, Recommended fails
        val errorMessage = "Recommended failed"
        val customException = createTestException(errorMessage)
        val viewModel = createViewModelWithMockedData(
            trendingResult = Resource.success(mockTrendingProducts),
            recommendedResult = Resource.error(customException)
        )

        // When
        viewModel.refreshData()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertEquals(2, state.trendingProducts.size) // Success
        assertTrue(state.recommendedProducts.isEmpty()) // Error - empty list
        assertFalse(state.isLoadingTrending)
        assertFalse(state.isLoadingRecommended)
        assertNull(state.errorTrending) // No error
        assertEquals(errorMessage, state.errorRecommended) // Has error
    }

    @Test
    fun `init block triggers both fetch methods automatically`() = runTest {
        // Given & When: ViewModel creation triggers init
        val viewModel = createViewModelWithMockedData()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: Both methods should have been called automatically
        val state = viewModel.uiState.first()
        assertEquals(2, state.trendingProducts.size)
        assertEquals(2, state.recommendedProducts.size)
        assertFalse(state.isLoadingTrending)
        assertFalse(state.isLoadingRecommended)
    }

    @Test
    fun `fetchTrendingProducts clears previous error on new request`() = runTest {
        // Given: Start with error state
        val errorMessage = "Initial error"
        val customException = createTestException(errorMessage)
        val viewModel = createViewModelWithMockedData(
            trendingResult = Resource.error(customException)
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify error state
        val errorState = viewModel.uiState.first()
        assertEquals(errorMessage, errorState.errorTrending)

        // When: Mock success for retry and fetch again
        whenever(homeRepository.getTrendingProducts(amount = 3))
            .thenReturn(Resource.success(mockTrendingProducts))

        viewModel.fetchTrendingProducts()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: Error should be cleared and success data loaded
        val successState = viewModel.uiState.first()
        assertNull(successState.errorTrending) // Error cleared
        assertEquals(2, successState.trendingProducts.size)
        assertFalse(successState.isLoadingTrending)
    }

    @Test
    fun `fetchHomeRecommendations clears previous error on new request`() = runTest {
        // Given: Start with error state
        val errorMessage = "Initial error"
        val customException = createTestException(errorMessage)
        val viewModel = createViewModelWithMockedData(
            recommendedResult = Resource.error(customException)
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify error state
        val errorState = viewModel.uiState.first()
        assertEquals(errorMessage, errorState.errorRecommended)

        // When: Mock success for retry and fetch again
        whenever(productRepository.getHomeRecommendations())
            .thenReturn(Resource.success(mockRecommendedProducts))

        viewModel.fetchHomeRecommendations()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: Error should be cleared and success data loaded
        val successState = viewModel.uiState.first()
        assertNull(successState.errorRecommended) // Error cleared
        assertEquals(2, successState.recommendedProducts.size)
        assertFalse(successState.isLoadingRecommended)
    }

    @Test
    fun `independent loading states for trending and recommended`() = runTest {
        // Given
        val viewModel = createViewModelWithMockedData()

        // Then: Test that each section can load independently
        viewModel.uiState.test {
            val initialState = awaitItem()

            // Manually trigger only trending fetch
            viewModel.fetchTrendingProducts()

            val trendingLoadingState = awaitItem()
            assertTrue(trendingLoadingState.isLoadingTrending)
            // Recommended loading should not be affected if it's not being fetched simultaneously

            testDispatcher.scheduler.advanceUntilIdle()

            val finalState = awaitItem()
            assertFalse(finalState.isLoadingTrending)
            assertEquals(2, finalState.trendingProducts.size)

            cancelAndIgnoreRemainingEvents()
        }
    }
}