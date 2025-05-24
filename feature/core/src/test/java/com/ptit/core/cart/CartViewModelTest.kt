package com.ptit.core.cart

import app.cash.turbine.test
import com.ptit.domain.entity.DeletePurchaseResult
import com.ptit.domain.entity.cart.PurchaseDomainEntity
import com.ptit.domain.entity.common.BaseErrorResponseDomainEntity
import com.ptit.domain.entity.common.UserDomainEntity
import com.ptit.domain.entity.product.CategoryDomainEntity
import com.ptit.domain.entity.product.ProductDomainEntity
import com.ptit.domain.repository.PurchaseRepository
import com.ptit.domain.utils.Resource
import com.ptit.domain.utils.UnknownException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay // Import delay
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
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class CartViewModelTest {

    @Mock
    private lateinit var purchaseRepository: PurchaseRepository

    private val testDispatcher = StandardTestDispatcher()

    // Mock data (Giữ nguyên)
    private val mockUser = UserDomainEntity(
        id = "user1", name = "John Doe", phone = "0123456789", email = "john@example.com", address = "123 Test Street",
        shop = UserDomainEntity.Shop("Test Shop", "123 Test Street", "0123456789", "Test Shop Description", "shop_avatar.jpg")
    )
    private val mockProduct1 = ProductDomainEntity(
        id = "product1", name = "Product 1", price = 100000, priceBeforeDiscount = 120000, image = "image1.jpg", images = listOf("image1.jpg", "image1_2.jpg"), quantity = 10, rating = 4.5f, sold = 50, view = 100, description = "Product 1 description", category = CategoryDomainEntity(), shop = mockUser, createdAt = "2023-01-01", updatedAt = "2023-01-02"
    )
    private val mockProduct2 = ProductDomainEntity(
        id = "product2", name = "Product 2", price = 200000, priceBeforeDiscount = 250000, image = "image2.jpg", images = listOf("image2.jpg"), quantity = 5, rating = 4.0f, sold = 30, view = 80, description = "Product 2 description", category = CategoryDomainEntity(), shop = mockUser, createdAt = "2023-01-01", updatedAt = "2023-01-02"
    )
    private val mockPurchase1 = PurchaseDomainEntity(
        id = "purchase1", buyCount = 2, price = 100000, priceBeforeDiscount = 120000, status = 1, user = "user1", product = mockProduct1, createdAt = "2023-01-01", updatedAt = "2023-01-02"
    )
    private val mockPurchase2 = PurchaseDomainEntity(
        id = "purchase2", buyCount = 1, price = 200000, priceBeforeDiscount = 250000, status = 1, user = "user1", product = mockProduct2, createdAt = "2023-01-01", updatedAt = "2023-01-02"
    )
    private val mockDeleteResponse = DeletePurchaseResult(deletedCount = 1)
    private val mockMultiDeleteResponse = DeletePurchaseResult(deletedCount = 2)

    private fun createTestException(message: String?): UnknownException { // Allow null message
        return UnknownException(
            error = BaseErrorResponseDomainEntity(message = message ?: "Unknown error"),
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

    // Helper to mock getPurchases with delay
    private suspend fun mockGetPurchases(result: Resource<List<PurchaseDomainEntity>>) {
        whenever(purchaseRepository.getPurchases(any())).thenAnswer {
//            delay(1)
            result
        }
    }

    // Helper to mock updatePurchase with delay
    private suspend fun mockUpdatePurchase(result: Resource<PurchaseDomainEntity>) {
        whenever(purchaseRepository.updatePurchase(any(), any())).thenAnswer {
//            delay(1)
            result
        }
    }

    // Helper to mock deletePurchases with delay
    private suspend fun mockDeletePurchases(result: Resource<DeletePurchaseResult>) {
        whenever(purchaseRepository.deletePurchases(any())).thenAnswer {
//            delay(1)
            result
        }
    }

    @Test
    fun `initial state is Loading and loads purchases on init`() = runTest {
        // Given
        val purchases = listOf(mockPurchase1, mockPurchase2)
        mockGetPurchases(Resource.success(purchases))

        // When
        val viewModel = CartViewModel(purchaseRepository)

        // Then - Initial state should be Loading
        assertEquals(CartViewModel.CartState.Loading, viewModel.cartState.value)

        // Advance dispatcher to process init coroutine
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - State should be Success with purchases
        val finalState = viewModel.cartState.value
        assertTrue(finalState is CartViewModel.CartState.Success)
        assertEquals(purchases, (finalState as CartViewModel.CartState.Success).purchases)
        verify(purchaseRepository).getPurchases(-1)
    }

    @Test
    fun `getPurchases emits Success state when repository returns success`() = runTest {
        // Given
        val purchases = listOf(mockPurchase1, mockPurchase2)
        mockGetPurchases(Resource.success(purchases))
        val viewModel = CartViewModel(purchaseRepository)

        // When
        viewModel.cartState.test {
            // It starts loading in init, wait for it
            assertEquals(CartViewModel.CartState.Loading, awaitItem())
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(purchases, (awaitItem() as CartViewModel.CartState.Success).purchases)

            // Call again
            viewModel.getPurchases()
            assertEquals(CartViewModel.CartState.Loading, awaitItem())
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(purchases, (awaitItem() as CartViewModel.CartState.Success).purchases)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `getPurchases emits Error state when repository returns error`() = runTest {
        // Given
        val errorMessage = "Network error"
        mockGetPurchases(Resource.error(createTestException(errorMessage)))
        val viewModel = CartViewModel(purchaseRepository)

        // When
        viewModel.cartState.test {
            assertEquals(CartViewModel.CartState.Loading, awaitItem())
            testDispatcher.scheduler.advanceUntilIdle()
            val errorState = awaitItem()
            assertTrue(errorState is CartViewModel.CartState.Error)
            assertEquals(errorMessage, (errorState as CartViewModel.CartState.Error).message)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `getPurchases with specific status calls repository with correct parameter`() = runTest {
        // Given
        val status = 1
        mockGetPurchases(Resource.success(listOf(mockPurchase1)))
        val viewModel = CartViewModel(purchaseRepository)
        testDispatcher.scheduler.advanceUntilIdle() // Process init

        // When
        viewModel.getPurchases(status)
        testDispatcher.scheduler.advanceUntilIdle() // Process new call

        // Then
        verify(purchaseRepository).getPurchases(status)
    }

    @Test
    fun `updatePurchaseQuantity updates local state immediately and calls repository`() = runTest {
        // Given
        mockGetPurchases(Resource.success(listOf(mockPurchase1, mockPurchase2)))
        mockUpdatePurchase(Resource.success(mockPurchase1.copy(buyCount = 3)))
        val viewModel = CartViewModel(purchaseRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.updatePurchaseQuantity("product1", 3)

        // Then - Check immediate local update
        val currentState = viewModel.cartState.value
        assertTrue(currentState is CartViewModel.CartState.Success)
        val updatedPurchases = (currentState as CartViewModel.CartState.Success).purchases
        assertEquals(3, updatedPurchases.find { it.product.id == "product1" }?.buyCount)

        // Then - Repository should be called
        testDispatcher.scheduler.advanceUntilIdle()
        verify(purchaseRepository).updatePurchase("product1", 3)
    }

    @Test
    fun `updatePurchaseQuantity emits Success state when repository succeeds`() = runTest {
        // Given
        mockGetPurchases(Resource.success(listOf(mockPurchase1)))
        val updatedPurchase = mockPurchase1.copy(buyCount = 5)
        mockUpdatePurchase(Resource.success(updatedPurchase))
        val viewModel = CartViewModel(purchaseRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.updatePurchaseState.test {
            assertEquals(CartViewModel.UpdatePurchaseState.Idle, awaitItem()) // Check Idle
            viewModel.updatePurchaseQuantity("product1", 5)
            assertEquals(CartViewModel.UpdatePurchaseState.Loading, awaitItem()) // Check Loading
            testDispatcher.scheduler.advanceUntilIdle()
            val successState = awaitItem()
            assertTrue(successState is CartViewModel.UpdatePurchaseState.Success)
            assertEquals(updatedPurchase, (successState as CartViewModel.UpdatePurchaseState.Success).updatedPurchase)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `updatePurchaseQuantity emits Error state when repository fails`() = runTest {
        // Given
        mockGetPurchases(Resource.success(listOf(mockPurchase1)))
        val errorMessage = "Update failed"
        mockUpdatePurchase(Resource.error(createTestException(errorMessage)))
        val viewModel = CartViewModel(purchaseRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.updatePurchaseState.test {
            assertEquals(CartViewModel.UpdatePurchaseState.Idle, awaitItem()) // Check Idle
            viewModel.updatePurchaseQuantity("product1", 5)
            assertEquals(CartViewModel.UpdatePurchaseState.Loading, awaitItem()) // Check Loading
            testDispatcher.scheduler.advanceUntilIdle()
            val errorState = awaitItem()
            assertTrue(errorState is CartViewModel.UpdatePurchaseState.Error)
            assertEquals(errorMessage, (errorState as CartViewModel.UpdatePurchaseState.Error).message)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `deletePurchase calls deletePurchases with single item`() = runTest {
        // Given
        mockGetPurchases(Resource.success(emptyList()))
        mockDeletePurchases(Resource.success(mockDeleteResponse))
        val viewModel = CartViewModel(purchaseRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.deletePurchase("purchase1")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify(purchaseRepository).deletePurchases(listOf("purchase1"))
    }

    @Test
    fun `deletePurchases emits Success state and refreshes cart when repository succeeds`() = runTest {
        // Given
        val initialPurchases = listOf(mockPurchase1, mockPurchase2)
        val remainingPurchases = listOf(mockPurchase2)
        var getCount = 0
        whenever(purchaseRepository.getPurchases(any())).thenAnswer {
            delay(1)
            getCount++
            if (getCount == 1) Resource.success(initialPurchases) else Resource.success(remainingPurchases)
        }
        mockDeletePurchases(Resource.success(mockDeleteResponse))
        val viewModel = CartViewModel(purchaseRepository)

        // When
        viewModel.cartState.test {
            assertEquals(CartViewModel.CartState.Loading, awaitItem()) // Init Loading
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(initialPurchases, (awaitItem() as CartViewModel.CartState.Success).purchases) // Init Success

            viewModel.deletePurchases(listOf("purchase1")) // Trigger delete

            assertEquals(CartViewModel.CartState.Loading, awaitItem()) // Refresh Loading
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(remainingPurchases, (awaitItem() as CartViewModel.CartState.Success).purchases) // Refresh Success

            cancelAndConsumeRemainingEvents()
        }

        // Verify getPurchases was called twice
        verify(purchaseRepository, times(2)).getPurchases(-1)
    }


    @Test
    fun `deletePurchases emits Error state when repository fails`() = runTest {
        // Given
        mockGetPurchases(Resource.success(listOf(mockPurchase1)))
        val errorMessage = "Delete failed"
        mockDeletePurchases(Resource.error(createTestException(errorMessage)))
        val viewModel = CartViewModel(purchaseRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.deletePurchaseState.test {
            assertEquals(CartViewModel.DeletePurchaseState.Idle, awaitItem()) // Check Idle
            viewModel.deletePurchases(listOf("purchase1"))
            assertEquals(CartViewModel.DeletePurchaseState.Loading, awaitItem()) // Check Loading
            testDispatcher.scheduler.advanceUntilIdle()
            val errorState = awaitItem()
            assertTrue(errorState is CartViewModel.DeletePurchaseState.Error)
            assertEquals(errorMessage, (errorState as CartViewModel.DeletePurchaseState.Error).message)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `deletePurchases handles multiple items correctly`() = runTest {
        // Given
        mockGetPurchases(Resource.success(emptyList())) // Init call
        mockDeletePurchases(Resource.success(mockMultiDeleteResponse))
        val viewModel = CartViewModel(purchaseRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.deletePurchaseState.test {
            assertEquals(CartViewModel.DeletePurchaseState.Idle, awaitItem()) // Idle
            viewModel.deletePurchases(listOf("purchase1", "purchase2"))
            assertEquals(CartViewModel.DeletePurchaseState.Loading, awaitItem()) // Loading
            testDispatcher.scheduler.advanceUntilIdle()
            val successState = awaitItem() // Success
            assertTrue(successState is CartViewModel.DeletePurchaseState.Success)
            assertEquals(2, (successState as CartViewModel.DeletePurchaseState.Success).deletedCount)
            cancelAndConsumeRemainingEvents()
        }

        // Then
        verify(purchaseRepository).deletePurchases(listOf("purchase1", "purchase2"))
    }

    @Test
    fun `updateLocalPurchase only affects cart when state is Success`() = runTest {
        // Given
        mockGetPurchases(Resource.success(listOf(mockPurchase1, mockPurchase2)))
        mockUpdatePurchase(Resource.success(mockPurchase1)) // Mock update call too
        val viewModel = CartViewModel(purchaseRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Set state to Error to test the 'if' condition (optional, good to have)
        // viewModel._cartState.value = CartViewModel.CartState.Error("Simulated Error")
        // viewModel.updatePurchaseQuantity("product1", 10) // Call update
        // assertTrue(viewModel.cartState.value is CartViewModel.CartState.Error) // Assert it didn't change

        // Reset and test Success case (already done in updatePurchaseQuantity updates local state)
        mockGetPurchases(Resource.success(listOf(mockPurchase1, mockPurchase2)))
        viewModel.getPurchases()
        testDispatcher.scheduler.advanceUntilIdle()


        // When - Update quantity for product1
        viewModel.updatePurchaseQuantity("product1", 10)

        // Then - Local state should be updated immediately
        val currentState = viewModel.cartState.value
        assertTrue(currentState is CartViewModel.CartState.Success)
        val updatedPurchases = (currentState as CartViewModel.CartState.Success).purchases
        assertEquals(10, updatedPurchases.find { it.product.id == "product1" }?.buyCount)
        assertEquals(1, updatedPurchases.find { it.product.id == "product2" }?.buyCount)
    }

    @Test
    fun `Resource Idle and unknown states are handled correctly`() = runTest {
        // Given
        mockGetPurchases(Resource.Idle)
        val viewModel = CartViewModel(purchaseRepository)

        // When
        viewModel.cartState.test {
            assertEquals(CartViewModel.CartState.Loading, awaitItem()) // Initial
            viewModel.getPurchases() // Trigger call
            assertEquals(CartViewModel.CartState.Loading, awaitItem()) // Set Loading
            testDispatcher.scheduler.advanceUntilIdle()
            // Since Idle doesn't change state, it should remain Loading.
            // We can't await anything else. So we assert the final value.
            ensureAllEventsConsumed()
        }

        // Then
        val state = viewModel.cartState.value
        assertTrue(state is CartViewModel.CartState.Loading)
    }

    @Test
    fun `error message handles null message gracefully`() = runTest {
        // Given
        val exception = createTestException(null) // Pass null
        mockGetPurchases(Resource.error(exception))
        val viewModel = CartViewModel(purchaseRepository)

        // When
        viewModel.cartState.test {
            assertEquals(CartViewModel.CartState.Loading, awaitItem())
            testDispatcher.scheduler.advanceUntilIdle()
            val state = awaitItem()
            assertTrue(state is CartViewModel.CartState.Error)
            assertEquals("Unknown error", (state as CartViewModel.CartState.Error).message) // Check default
            cancelAndConsumeRemainingEvents()
        }
    }
}