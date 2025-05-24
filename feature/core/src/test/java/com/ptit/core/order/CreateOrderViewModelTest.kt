package com.ptit.core.order


import androidx.compose.ui.text.input.TextFieldValue
import app.cash.turbine.test
import com.ptit.domain.entity.cart.PurchaseDomainEntity
import com.ptit.domain.entity.common.BaseErrorResponseDomainEntity
import com.ptit.domain.entity.common.UserDomainEntity
import com.ptit.domain.entity.order.CreateOrderResponseDomainEntity
import com.ptit.domain.entity.product.CategoryDomainEntity
import com.ptit.domain.entity.product.ProductDomainEntity
import com.ptit.domain.repository.OrderRepository
import com.ptit.domain.repository.PurchaseRepository
import com.ptit.domain.repository.UserRepository
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
class CreateOrderViewModelTest {


    @Mock
    private lateinit var purchaseRepository: PurchaseRepository


    @Mock
    private lateinit var orderRepository: OrderRepository


    @Mock
    private lateinit var userRepository: UserRepository


    // ViewModel will be created in each test
    // private lateinit var viewModel: CreateOrderViewModel


    private val testDispatcher = StandardTestDispatcher()


    // Test data (remains the same)
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
    private val mockProduct1 = ProductDomainEntity( /* ... as before ... */
        id = "product1", name = "Product 1", price = 100000, priceBeforeDiscount = 120000, image = "image1.jpg", images = listOf("image1.jpg", "image1_2.jpg"), quantity = 10, rating = 4.5f, sold = 50, view = 100, description = "Product 1 description", category = CategoryDomainEntity(), shop = mockUser, createdAt = "2023-01-01", updatedAt = "2023-01-02"
    )
    private val mockProduct2 = ProductDomainEntity( /* ... as before ... */
        id = "product2", name = "Product 2", price = 200000, priceBeforeDiscount = 250000, image = "image2.jpg", images = listOf("image2.jpg"), quantity = 5, rating = 4.0f, sold = 30, view = 80, description = "Product 2 description", category = CategoryDomainEntity(), shop = mockUser, createdAt = "2023-01-01", updatedAt = "2023-01-02"
    )
    private val mockPurchase1 = PurchaseDomainEntity( /* ... as before ... */
        id = "purchase1", buyCount = 2, price = 100000, priceBeforeDiscount = 120000, status = 1, user = "user1", product = mockProduct1, createdAt = "2023-01-01", updatedAt = "2023-01-02"
    )
    private val mockPurchase2 = PurchaseDomainEntity( /* ... as before ... */
        id = "purchase2", buyCount = 1, price = 200000, priceBeforeDiscount = 250000, status = 1, user = "user1", product = mockProduct2, createdAt = "2023-01-01", updatedAt = "2023-01-02"
    )
    private val mockOrderResponse = CreateOrderResponseDomainEntity( /* ... as before ... */
        orderId = "order123", totalAmount = 400000
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
        // No ViewModel creation or default mocking here
    }


    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }


    // Helper to create ViewModel and mock default success user profile
    private fun createViewModelWithMockedUser(userResult: Resource<UserDomainEntity> = Resource.success(mockUser)): CreateOrderViewModel {
        runTest {
            whenever(userRepository.getUserProfile()).thenReturn(userResult)
        }
        return CreateOrderViewModel(
            purchaseRepository = purchaseRepository,
            orderRepository = orderRepository,
            userRepository = userRepository
        )
    }


    @Test
    fun `initial state has correct default values`() = runTest {
        // Given: Mock user profile to fail so init doesn't update fields
        val viewModel = createViewModelWithMockedUser(Resource.error(createTestException("No load")))


        // Then: Check state *before* advancing (though init might run briefly)
        val state = viewModel.orderState.first()


        assertFalse(state.isLoading)
        assertNull(state.error)
        assertTrue(state.selectedItems.isEmpty())
        assertEquals("", state.name.text) // Should be empty
        assertEquals("", state.phone.text) // Should be empty
        assertEquals("", state.address.text) // Should be empty
        assertEquals("", state.note.text)
        assertFalse(state.isNameEditing)
        assertFalse(state.isPhoneEditing)
        assertFalse(state.isAddressEditing)
        assertFalse(state.nameEdited)
        assertFalse(state.phoneEdited)
        assertFalse(state.addressEdited)
        assertFalse(state.isUserInfoLoaded) // Should be false
    }


    @Test
    fun `loadUserProfile updates state with user info on success`() = runTest {
        // Given
        val viewModel = createViewModelWithMockedUser()


        // When
        testDispatcher.scheduler.advanceUntilIdle()


        // Then
        val state = viewModel.orderState.first()
        assertEquals(mockUser.name, state.name.text)
        assertEquals(mockUser.phone, state.phone.text)
        assertEquals(mockUser.address, state.address.text)
        assertTrue(state.isUserInfoLoaded)
    }


    @Test
    fun `loadUserProfile emits error event on failure`() = runTest {
        // Given
        val errorMessage = "Failed to load user profile"
        val customException = createTestException(errorMessage)
        val viewModel = createViewModelWithMockedUser(Resource.error(customException))


        // Then
        viewModel.orderEvents.test {
            testDispatcher.scheduler.advanceUntilIdle() // Ensure init coroutine runs
            val event = awaitItem()
            assertTrue(event is OrderEvent.ShowError)
            assertEquals("Tải thông tin user thất bại: $errorMessage", (event as OrderEvent.ShowError).message)
            cancelAndIgnoreRemainingEvents()
        }
    }


    @Test
    fun `setSelectedItemIds loads purchases successfully`() = runTest {
        // Given
        val viewModel = createViewModelWithMockedUser()
        val itemIds = listOf("purchase1", "purchase2")
        whenever(purchaseRepository.getPurchaseById("purchase1")).thenReturn(Resource.success(mockPurchase1))
        whenever(purchaseRepository.getPurchaseById("purchase2")).thenReturn(Resource.success(mockPurchase2))


        // When
        viewModel.setSelectedItemIds(itemIds)
        testDispatcher.scheduler.advanceUntilIdle()


        // Then
        val state = viewModel.orderState.first()
        assertFalse(state.isLoading)
        assertEquals(2, state.selectedItems.size)
        assertTrue(state.selectedItems.contains(mockPurchase1))
        assertTrue(state.selectedItems.contains(mockPurchase2))
    }


    @Test
    fun `setSelectedItemIds emits error when no items provided`() = runTest {
        // Given
        val viewModel = createViewModelWithMockedUser()


        // Then
        viewModel.orderEvents.test {
            // When
            viewModel.setSelectedItemIds(emptyList())
            testDispatcher.scheduler.advanceUntilIdle() // Advance here


            // Then
            val event = awaitItem()
            assertTrue(event is OrderEvent.ShowError)
            assertEquals("Không sản phẩm nào được chọn", (event as OrderEvent.ShowError).message)
            cancelAndIgnoreRemainingEvents()
        }
    }


    @Test
    fun `setSelectedItemIds handles purchase loading error`() = runTest {
        // Given
        val viewModel = createViewModelWithMockedUser()
        val itemIds = listOf("purchase1")
        val errorMessage = "Failed to load purchase"
        val customException = createTestException(errorMessage)
        whenever(purchaseRepository.getPurchaseById("purchase1")).thenReturn(Resource.error(customException))


        // Then
        viewModel.orderEvents.test {
            // When
            viewModel.setSelectedItemIds(itemIds)
            testDispatcher.scheduler.advanceUntilIdle()


            // Then
            val event = awaitItem()
            assertTrue(event is OrderEvent.ShowError)
            assertEquals("Load sản phẩm thất bại: $errorMessage", (event as OrderEvent.ShowError).message)
            cancelAndIgnoreRemainingEvents()
        }


        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.orderState.first()
        assertFalse(state.isLoading)
        assertEquals("Load sản phẩm thất bại", state.error)
    }


    // ... (Keep updateName, updatePhone, updateAddress, updateNote tests - they should work)
    @Test fun `updateName updates state correctly`() = runTest {
        val viewModel = createViewModelWithMockedUser()
        val newName = TextFieldValue("New Name")
        viewModel.updateName(newName)
        val state = viewModel.orderState.first()
        assertEquals(newName, state.name)
        assertTrue(state.nameEdited)
    }
    @Test fun `updatePhone updates state correctly`() = runTest {
        val viewModel = createViewModelWithMockedUser()
        val newPhone = TextFieldValue("0987654321")
        viewModel.updatePhone(newPhone)
        val state = viewModel.orderState.first()
        assertEquals(newPhone, state.phone)
        assertTrue(state.phoneEdited)
    }
    @Test fun `updateAddress updates state correctly`() = runTest {
        val viewModel = createViewModelWithMockedUser()
        val newAddress = TextFieldValue("New Address")
        viewModel.updateAddress(newAddress)
        val state = viewModel.orderState.first()
        assertEquals(newAddress, state.address)
        assertTrue(state.addressEdited)
    }
    @Test fun `updateNote updates state correctly`() = runTest {
        val viewModel = createViewModelWithMockedUser()
        val newNote = TextFieldValue("Special instructions")
        viewModel.updateNote(newNote)
        val state = viewModel.orderState.first()
        assertEquals(newNote, state.note)
    }
    @Test fun `toggleNameEditing changes editing state`() = runTest {
        val viewModel = createViewModelWithMockedUser()
        viewModel.toggleNameEditing()
        val state1 = viewModel.orderState.first()
        assertTrue(state1.isNameEditing)
        viewModel.toggleNameEditing()
        val state2 = viewModel.orderState.first()
        assertFalse(state2.isNameEditing)
    }
    @Test fun `togglePhoneEditing changes editing state`() = runTest {
        val viewModel = createViewModelWithMockedUser()
        viewModel.togglePhoneEditing()
        val state = viewModel.orderState.first()
        assertTrue(state.isPhoneEditing)
    }
    @Test fun `toggleAddressEditing changes editing state`() = runTest {
        val viewModel = createViewModelWithMockedUser()
        viewModel.toggleAddressEditing()
        val state = viewModel.orderState.first()
        assertTrue(state.isAddressEditing)
    }
    // ...


    @Test
    fun `createOrder fails when no items selected`() = runTest {
        // Given
        val viewModel = createViewModelWithMockedUser()
        testDispatcher.scheduler.advanceUntilIdle() // Ensure user is loaded


        // Then
        viewModel.orderEvents.test {
            // When
            viewModel.createOrder()


            // Then
            val event = awaitItem()
            assertTrue(event is OrderEvent.ShowError)
            assertEquals("Không sản phẩm nào được chọn để thanh toán", (event as OrderEvent.ShowError).message)
            cancelAndIgnoreRemainingEvents()
        }
    }


    @Test
    fun `createOrder fails when required fields are empty`() = runTest {
        // Given
        val viewModel = createViewModelWithMockedUser()
        setupOrderWithItems(viewModel) // Pass viewModel
        testDispatcher.scheduler.advanceUntilIdle()


        viewModel.updateName(TextFieldValue(""))
        viewModel.updatePhone(TextFieldValue(""))
        viewModel.updateAddress(TextFieldValue(""))


        // Then
        viewModel.orderEvents.test {
            // When
            viewModel.createOrder()


            // Then
            val event = awaitItem()
            assertTrue(event is OrderEvent.ShowError)
            assertEquals("Vui lòng điền đầy đủ thông tin", (event as OrderEvent.ShowError).message)
            cancelAndIgnoreRemainingEvents()
        }
    }


    @Test
    fun `createOrder succeeds with valid data`() = runTest {
        // Given
        val viewModel = createViewModelWithMockedUser()
        setupOrderWithValidData(viewModel) // Pass viewModel
        testDispatcher.scheduler.advanceUntilIdle()


        whenever(orderRepository.createOrder(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(Resource.success(mockOrderResponse))


        // Then
        viewModel.orderEvents.test {
            // When
            viewModel.createOrder()
            testDispatcher.scheduler.advanceUntilIdle()


            // Then
            val event = awaitItem()
            assertTrue(event is OrderEvent.OrderCreated)
            assertEquals(mockOrderResponse, (event as OrderEvent.OrderCreated).orderResponse)
            cancelAndIgnoreRemainingEvents()
        }


        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.orderState.first()
        assertFalse(state.isLoading)
    }


    @Test
    fun `createOrder handles repository error`() = runTest {
        // Given
        val viewModel = createViewModelWithMockedUser()
        setupOrderWithValidData(viewModel) // Pass viewModel
        testDispatcher.scheduler.advanceUntilIdle()


        val errorMessage = "Network error"
        val customException = createTestException(errorMessage)
        whenever(orderRepository.createOrder(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(Resource.error(customException))


        // Then
        viewModel.orderEvents.test {
            // When
            viewModel.createOrder()
            testDispatcher.scheduler.advanceUntilIdle()


            // Then
            val event = awaitItem()
            assertTrue(event is OrderEvent.ShowError)
            assertTrue((event as OrderEvent.ShowError).message.contains("Tạo đơn hàng thất bại"))
            cancelAndIgnoreRemainingEvents()
        }


        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.orderState.first()
        assertFalse(state.isLoading)
        assertNotNull(state.error)
    }


    // Update helper functions to accept viewModel
    private suspend fun setupOrderWithItems(viewModel: CreateOrderViewModel) {
        val itemIds = listOf("purchase1")
        whenever(purchaseRepository.getPurchaseById("purchase1")).thenReturn(Resource.success(mockPurchase1))
        viewModel.setSelectedItemIds(itemIds)
    }


    private suspend fun setupOrderWithValidData(viewModel: CreateOrderViewModel) {
        setupOrderWithItems(viewModel)
        viewModel.updateName(TextFieldValue("John Doe"))
        viewModel.updatePhone(TextFieldValue("0123456789"))
        viewModel.updateAddress(TextFieldValue("123 Test Street"))
        viewModel.updateNote(TextFieldValue("Test note"))
    }
}

