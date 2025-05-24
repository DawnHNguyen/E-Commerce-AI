package com.ptit.core.order

import app.cash.turbine.test
import com.ptit.domain.entity.cart.PurchaseDomainEntity
import com.ptit.domain.entity.common.BaseErrorResponseDomainEntity
import com.ptit.domain.entity.common.UserDomainEntity
import com.ptit.domain.entity.order.OrderDomainEntity
import com.ptit.domain.entity.product.CategoryDomainEntity
import com.ptit.domain.entity.product.ProductDomainEntity
import com.ptit.domain.repository.OrderRepository
import com.ptit.domain.utils.CustomException
import com.ptit.domain.utils.Resource
import com.ptit.domain.utils.UnknownException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class OrderDetailViewModelTest {

    @Mock
    private lateinit var orderRepository: OrderRepository

    private lateinit var viewModel: OrderDetailViewModel

    private val testDispatcher = StandardTestDispatcher()

    // Mock Data
    private val mockUser = UserDomainEntity(
        id = "user1",
        name = "John Doe",
        phone = "0123456789",
        email = "john@example.com",
        address = "123 Test Street",
        shop = UserDomainEntity.Shop(name = "Test Shop", address = "123 Test Street", phone = "0123456789", description = "Test Shop Description", avatar = "shop_avatar.jpg")
    )
    private val mockProduct1 = ProductDomainEntity(
        id = "product1", name = "Product 1", price = 100000, priceBeforeDiscount = 120000, image = "image1.jpg", images = listOf("image1.jpg"), quantity = 10, rating = 4.5f, sold = 50, view = 100, description = "Desc 1", category = CategoryDomainEntity(), shop = mockUser, createdAt = "2023-01-01", updatedAt = "2023-01-02"
    )
    private val mockProduct2 = ProductDomainEntity(
        id = "product2", name = "Product 2", price = 200000, priceBeforeDiscount = 250000, image = "image2.jpg", images = listOf("image2.jpg"), quantity = 5, rating = 4.0f, sold = 30, view = 80, description = "Desc 2", category = CategoryDomainEntity(), shop = mockUser, createdAt = "2023-01-01", updatedAt = "2023-01-02"
    )
    private val mockPurchase1 = PurchaseDomainEntity(
        id = "purchase1", buyCount = 2, price = 100000, priceBeforeDiscount = 120000, status = 1, user = "user1", product = mockProduct1, createdAt = "2023-01-01", updatedAt = "2023-01-02"
    )
    private val mockPurchase2 = PurchaseDomainEntity(
        id = "purchase2", buyCount = 1, price = 200000, priceBeforeDiscount = 250000, status = 1, user = "user1", product = mockProduct2, createdAt = "2023-01-01", updatedAt = "2023-01-02"
    )
    private val mockOrder = OrderDomainEntity(
        id = "order123", fullName = "John Doe", phone = "0123456789", address = "123 Test Street", note = "Test note", shippingFee = 15000,  status = "Pending", purchases = listOf(mockPurchase1, mockPurchase2), createdAt = "2025-05-24T10:00:00.000Z", updatedAt = "2025-05-24T10:05:00.000Z"
    )

    private fun createTestException(message: String): CustomException {
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
        viewModel = OrderDetailViewModel(orderRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadOrderDetails emits loading then success when repository returns success`() = runTest {
        val orderId = "order123"
        whenever(orderRepository.getOrderById(orderId)).thenReturn(Resource.success(mockOrder))

        viewModel.orderState.test {
            assertTrue(awaitItem() is Resource.Idle)

            viewModel.loadOrderDetails(orderId)

            assertTrue(awaitItem() is Resource.Loading)

            testDispatcher.scheduler.advanceUntilIdle()

            val successState = awaitItem()
            assertTrue(successState is Resource.Success)
            assertEquals(mockOrder, (successState as Resource.Success).data)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadOrderDetails emits loading then error when repository returns error`() = runTest {
        val orderId = "order123"
        val exception = createTestException("Failed to load")
        whenever(orderRepository.getOrderById(orderId)).thenReturn(Resource.error(exception))

        viewModel.orderState.test {
            assertTrue(awaitItem() is Resource.Idle)

            viewModel.loadOrderDetails(orderId)

            assertTrue(awaitItem() is Resource.Loading)

            testDispatcher.scheduler.advanceUntilIdle()

            val errorState = awaitItem()
            assertTrue(errorState is Resource.Error)
            assertEquals(exception, (errorState as Resource.Error).error)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `payOrder emits loading then success when repository returns success`() = runTest {
        val orderId = "order123"
        val tokenId = "token123"
        whenever(orderRepository.payOrder(orderId, tokenId)).thenReturn(Resource.success(Unit))

        viewModel.payOrderState.test {
            assertTrue(awaitItem() is Resource.Idle)

            viewModel.payOrder(orderId, tokenId)

            assertTrue(awaitItem() is Resource.Loading)

            testDispatcher.scheduler.advanceUntilIdle()

            val successState = awaitItem()
            assertTrue(successState is Resource.Success)
            assertEquals(Unit, (successState as Resource.Success).data)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `payOrder emits loading then error when repository returns error`() = runTest {
        val orderId = "order123"
        val tokenId = "token123"
        val exception = createTestException("Payment failed")
        whenever(orderRepository.payOrder(orderId, tokenId)).thenReturn(Resource.error(exception))

        viewModel.payOrderState.test {
            assertTrue(awaitItem() is Resource.Idle)

            viewModel.payOrder(orderId, tokenId)

            assertTrue(awaitItem() is Resource.Loading)

            testDispatcher.scheduler.advanceUntilIdle()

            val errorState = awaitItem()
            assertTrue(errorState is Resource.Error)
            assertEquals(exception, (errorState as Resource.Error).error)

            cancelAndIgnoreRemainingEvents()
        }
    }
}