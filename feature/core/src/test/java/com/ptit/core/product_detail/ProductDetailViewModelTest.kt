package com.ptit.presentation.viewmodel

import app.cash.turbine.test
import com.ptit.domain.entity.cart.PurchaseDomainEntity
import com.ptit.domain.entity.common.BaseErrorResponseDomainEntity
import com.ptit.domain.entity.common.UserDomainEntity
import com.ptit.domain.entity.product.CategoryDomainEntity
import com.ptit.domain.entity.product.ProductDomainEntity
import com.ptit.domain.repository.ProductRepository
import com.ptit.domain.repository.PurchaseRepository
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
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class ProductDetailViewModelTest {

    @Mock
    private lateinit var productRepository: ProductRepository

    @Mock
    private lateinit var purchaseRepository: PurchaseRepository

    private val testDispatcher = StandardTestDispatcher()

    // Test data
    private val mockUser = UserDomainEntity(
        id = "user1",
        name = "John Doe",
        phone = "0123456789",
        email = "john@example.com",
        address = "123 Test Street",
        shop = UserDomainEntity.Shop(
            name = "Test Shop",
            address = "123 Test Street",
            phone = "0123456789",
            description = "Test Shop Description",
            avatar = "shop_avatar.jpg"
        )
    )

    private val mockProduct = ProductDomainEntity(
        id = "product1",
        name = "Test Product",
        price = 100000,
        priceBeforeDiscount = 120000,
        image = "image1.jpg",
        images = listOf("image1.jpg", "image1_2.jpg"),
        quantity = 10,
        rating = 4.5f,
        sold = 50,
        view = 100,
        description = "Test product description",
        category = CategoryDomainEntity(),
        shop = mockUser,
        createdAt = "2023-01-01",
        updatedAt = "2023-01-02"
    )

    private val mockSimilarProducts = listOf(
        ProductDomainEntity(
            id = "product2",
            name = "Similar Product 1",
            price = 90000,
            priceBeforeDiscount = 110000,
            image = "image2.jpg",
            images = listOf("image2.jpg"),
            quantity = 5,
            rating = 4.0f,
            sold = 25,
            view = 60,
            description = "Similar product 1 description",
            category = CategoryDomainEntity(),
            shop = mockUser,
            createdAt = "2023-01-01",
            updatedAt = "2023-01-02"
        ),
        ProductDomainEntity(
            id = "product3",
            name = "Similar Product 2",
            price = 110000,
            priceBeforeDiscount = 130000,
            image = "image3.jpg",
            images = listOf("image3.jpg"),
            quantity = 8,
            rating = 4.2f,
            sold = 35,
            view = 75,
            description = "Similar product 2 description",
            category = CategoryDomainEntity(),
            shop = mockUser,
            createdAt = "2023-01-01",
            updatedAt = "2023-01-02"
        )
    )

    private val mockPurchase = PurchaseDomainEntity(
        id = "purchase1",
        buyCount = 2,
        price = 100000,
        priceBeforeDiscount = 120000,
        status = 1,
        user = "user1",
        product = mockProduct,
        createdAt = "2023-01-01",
        updatedAt = "2023-01-02"
    )

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

    private fun createViewModel(): ProductDetailViewModel {
        return ProductDetailViewModel(
            productRepository = productRepository,
            purchaseRepository = purchaseRepository
        )
    }

    @Test
    fun `initial state has correct default values`() = runTest {
        // Given
        val viewModel = createViewModel()

        // Then
        val productState = viewModel.productDetailState.first()
        val addToCartState = viewModel.addToCartState.first()
        val similarProductsState = viewModel.similarProductsState.first()

        assertTrue(productState is ProductDetailState.Initial)
        assertTrue(addToCartState is AddToCartState.Initial)
        assertTrue(similarProductsState is SimilarProductsState.Initial)
    }

    @Test
    fun `getProductDetail loads product successfully and fetches similar products`() = runTest {
        // Given
        val viewModel = createViewModel()
        val productId = "product1"

        whenever(productRepository.getProductDetail(productId))
            .thenReturn(Resource.success(mockProduct))
        whenever(productRepository.getSimilarProducts(productId, 6))
            .thenReturn(Resource.success(mockSimilarProducts))

        // When
        viewModel.getProductDetail(productId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val productState = viewModel.productDetailState.first()
        val similarProductsState = viewModel.similarProductsState.first()

        assertTrue(productState is ProductDetailState.Success)
        assertEquals(mockProduct, (productState as ProductDetailState.Success).product)

        assertTrue(similarProductsState is SimilarProductsState.Success)
        assertEquals(mockSimilarProducts, (similarProductsState as SimilarProductsState.Success).products)
    }

    @Test
    fun `getProductDetail emits loading state initially`() = runTest {
        // Given
        val viewModel = createViewModel()
        val productId = "product1"

        whenever(productRepository.getProductDetail(productId))
            .thenReturn(Resource.success(mockProduct))
        whenever(productRepository.getSimilarProducts(productId, 6))
            .thenReturn(Resource.success(mockSimilarProducts))

        // When & Then
        viewModel.productDetailState.test {
            viewModel.getProductDetail(productId)

            // Skip initial state
            awaitItem() // Initial
            val loadingState = awaitItem() // Loading
            assertTrue(loadingState is ProductDetailState.Loading)

            testDispatcher.scheduler.advanceUntilIdle()
            val successState = awaitItem() // Success
            assertTrue(successState is ProductDetailState.Success)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getProductDetail handles product loading error`() = runTest {
        // Given
        val viewModel = createViewModel()
        val productId = "product1"
        val errorMessage = "Failed to load product"
        val customException = createTestException(errorMessage)

        whenever(productRepository.getProductDetail(productId))
            .thenReturn(Resource.error(customException))

        // When
        viewModel.getProductDetail(productId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val productState = viewModel.productDetailState.first()
        val similarProductsState = viewModel.similarProductsState.first()

        assertTrue(productState is ProductDetailState.Error)
        assertEquals(errorMessage, (productState as ProductDetailState.Error).message)

        assertTrue(similarProductsState is SimilarProductsState.Error)
        assertEquals(errorMessage, (similarProductsState as SimilarProductsState.Error).message)
    }

    @Test
    fun `getProductDetail handles similar products loading error`() = runTest {
        // Given
        val viewModel = createViewModel()
        val productId = "product1"
        val errorMessage = "Failed to load similar products"
        val customException = createTestException(errorMessage)

        whenever(productRepository.getProductDetail(productId))
            .thenReturn(Resource.success(mockProduct))
        whenever(productRepository.getSimilarProducts(productId, 6))
            .thenReturn(Resource.error(customException))

        // When
        viewModel.getProductDetail(productId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val productState = viewModel.productDetailState.first()
        val similarProductsState = viewModel.similarProductsState.first()

        assertTrue(productState is ProductDetailState.Success)
        assertEquals(mockProduct, (productState as ProductDetailState.Success).product)

        assertTrue(similarProductsState is SimilarProductsState.Error)
        assertEquals(errorMessage, (similarProductsState as SimilarProductsState.Error).message)
    }

    @Test
    fun `addToCart adds product to cart successfully`() = runTest {
        // Given
        val viewModel = createViewModel()
        val productId = "product1"
        val buyCount = 2

        whenever(purchaseRepository.addToCart(productId, buyCount))
            .thenReturn(Resource.success(mockPurchase))

        // When
        viewModel.addToCart(productId, buyCount)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val addToCartState = viewModel.addToCartState.first()

        assertTrue(addToCartState is AddToCartState.Success)
        assertEquals(mockPurchase, (addToCartState as AddToCartState.Success).purchase)
    }

    @Test
    fun `addToCart uses default quantity when not specified`() = runTest {
        // Given
        val viewModel = createViewModel()
        val productId = "product1"

        whenever(purchaseRepository.addToCart(productId, 1))
            .thenReturn(Resource.success(mockPurchase))

        // When
        viewModel.addToCart(productId) // No buyCount parameter
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val addToCartState = viewModel.addToCartState.first()

        assertTrue(addToCartState is AddToCartState.Success)
        assertEquals(mockPurchase, (addToCartState as AddToCartState.Success).purchase)
    }

    @Test
    fun `addToCart emits loading state initially`() = runTest {
        // Given
        val viewModel = createViewModel()
        val productId = "product1"

        whenever(purchaseRepository.addToCart(productId, 1))
            .thenReturn(Resource.success(mockPurchase))

        // When & Then
        viewModel.addToCartState.test {
            viewModel.addToCart(productId)

            // Skip initial state
            awaitItem() // Initial
            val loadingState = awaitItem() // Loading
            assertTrue(loadingState is AddToCartState.Loading)

            testDispatcher.scheduler.advanceUntilIdle()
            val successState = awaitItem() // Success
            assertTrue(successState is AddToCartState.Success)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addToCart handles error correctly`() = runTest {
        // Given
        val viewModel = createViewModel()
        val productId = "product1"
        val errorMessage = "Failed to add to cart"
        val customException = createTestException(errorMessage)

        whenever(purchaseRepository.addToCart(productId, 1))
            .thenReturn(Resource.error(customException))

        // When
        viewModel.addToCart(productId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val addToCartState = viewModel.addToCartState.first()

        assertTrue(addToCartState is AddToCartState.Error)
        assertEquals(errorMessage, (addToCartState as AddToCartState.Error).message)
    }

    @Test
    fun `multiple operations can run independently`() = runTest {
        // Given
        val viewModel = createViewModel()
        val productId = "product1"

        whenever(productRepository.getProductDetail(productId))
            .thenReturn(Resource.success(mockProduct))
        whenever(productRepository.getSimilarProducts(productId, 6))
            .thenReturn(Resource.success(mockSimilarProducts))
        whenever(purchaseRepository.addToCart(productId, 1))
            .thenReturn(Resource.success(mockPurchase))

        // When
        viewModel.getProductDetail(productId)
        viewModel.addToCart(productId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val productState = viewModel.productDetailState.first()
        val addToCartState = viewModel.addToCartState.first()
        val similarProductsState = viewModel.similarProductsState.first()

        assertTrue(productState is ProductDetailState.Success)
        assertTrue(addToCartState is AddToCartState.Success)
        assertTrue(similarProductsState is SimilarProductsState.Success)

        assertEquals(mockProduct, (productState as ProductDetailState.Success).product)
        assertEquals(mockPurchase, (addToCartState as AddToCartState.Success).purchase)
        assertEquals(mockSimilarProducts, (similarProductsState as SimilarProductsState.Success).products)
    }

    @Test
    fun `states remain independent when one operation fails`() = runTest {
        // Given
        val viewModel = createViewModel()
        val productId = "product1"
        val errorMessage = "Add to cart failed"
        val customException = createTestException(errorMessage)

        whenever(productRepository.getProductDetail(productId))
            .thenReturn(Resource.success(mockProduct))
        whenever(productRepository.getSimilarProducts(productId, 6))
            .thenReturn(Resource.success(mockSimilarProducts))
        whenever(purchaseRepository.addToCart(productId, 1))
            .thenReturn(Resource.error(customException))

        // When
        viewModel.getProductDetail(productId)
        viewModel.addToCart(productId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val productState = viewModel.productDetailState.first()
        val addToCartState = viewModel.addToCartState.first()
        val similarProductsState = viewModel.similarProductsState.first()

        // Product loading should succeed
        assertTrue(productState is ProductDetailState.Success)
        assertTrue(similarProductsState is SimilarProductsState.Success)

        // Add to cart should fail
        assertTrue(addToCartState is AddToCartState.Error)
        assertEquals(errorMessage, (addToCartState as AddToCartState.Error).message)
    }

    @Test
    fun `similar products loading state is set correctly`() = runTest {
        // Given
        val viewModel = createViewModel()
        val productId = "product1"

        whenever(productRepository.getProductDetail(productId))
            .thenReturn(Resource.success(mockProduct))
        whenever(productRepository.getSimilarProducts(productId, 6))
            .thenReturn(Resource.success(mockSimilarProducts))

        // When & Then
        viewModel.similarProductsState.test {
            viewModel.getProductDetail(productId)

            // Skip initial state
            awaitItem() // Initial
            val loadingState = awaitItem() // Loading
            assertTrue(loadingState is SimilarProductsState.Loading)

            testDispatcher.scheduler.advanceUntilIdle()
            val successState = awaitItem() // Success
            assertTrue(successState is SimilarProductsState.Success)

            cancelAndIgnoreRemainingEvents()
        }
    }
}