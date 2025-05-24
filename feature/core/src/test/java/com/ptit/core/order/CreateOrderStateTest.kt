package com.ptit.core.order

import androidx.compose.ui.text.input.TextFieldValue
import com.ptit.domain.entity.cart.PurchaseDomainEntity
import com.ptit.domain.entity.common.UserDomainEntity
import com.ptit.domain.entity.product.CategoryDomainEntity
import com.ptit.domain.entity.product.ProductDomainEntity
import org.junit.Assert.*
import org.junit.Test

class CreateOrderStateTest {

    // Mock Data based on CreateOrderViewModelTest
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


    @Test
    fun `default state has expected values`() {
        val state = CreateOrderViewModel.OrderFormState()

        assertFalse(state.isLoading)
        assertNull(state.error)
        assertTrue(state.selectedItems.isEmpty())
        assertEquals("", state.name.text)
        assertEquals("", state.phone.text)
        assertEquals("", state.address.text)
        assertEquals("", state.note.text)
        assertFalse(state.isNameEditing)
        assertFalse(state.isPhoneEditing)
        assertFalse(state.isAddressEditing)
        assertFalse(state.nameEdited)
        assertFalse(state.phoneEdited)
        assertFalse(state.addressEdited)
        assertFalse(state.isUserInfoLoaded)
    }

    @Test
    fun `copy method maintains all fields and allows partial updates`() {
        val initialState = CreateOrderViewModel.OrderFormState(
            isLoading = true,
            error = "Test error",
            selectedItems = listOf(mockPurchase1),
            name = TextFieldValue("John Doe"),
            phone = TextFieldValue("0123456789"),
            address = TextFieldValue("123 Test Street"),
            note = TextFieldValue("Test note"),
            isNameEditing = true,
            isPhoneEditing = true,
            isAddressEditing = true,
            nameEdited = true,
            phoneEdited = true,
            addressEdited = true,
            isUserInfoLoaded = true
        )

        val updatedState = initialState.copy(
            isLoading = false,
            name = TextFieldValue("Jane Doe")
        )

        assertFalse(updatedState.isLoading)
        assertEquals("Jane Doe", updatedState.name.text)

        assertEquals("Test error", updatedState.error)
        assertEquals(1, updatedState.selectedItems.size)
        assertEquals(mockPurchase1, updatedState.selectedItems[0])
        assertEquals("0123456789", updatedState.phone.text)
        assertEquals("123 Test Street", updatedState.address.text)
        assertEquals("Test note", updatedState.note.text)
        assertTrue(updatedState.isNameEditing)
        assertTrue(updatedState.isPhoneEditing)
        assertTrue(updatedState.isAddressEditing)
        assertTrue(updatedState.nameEdited)
        assertTrue(updatedState.phoneEdited)
        assertTrue(updatedState.addressEdited)
        assertTrue(updatedState.isUserInfoLoaded)
    }

    @Test
    fun `state with valid data for order creation`() {
        val validState = CreateOrderViewModel.OrderFormState(
            isLoading = false,
            selectedItems = listOf(mockPurchase1, mockPurchase2),
            name = TextFieldValue("John Doe"),
            phone = TextFieldValue("0123456789"),
            address = TextFieldValue("123 Test Street"),
            note = TextFieldValue("Please deliver carefully"),
            isUserInfoLoaded = true
        )

        assertFalse(validState.isLoading)
        assertEquals(2, validState.selectedItems.size)
        assertFalse(validState.name.text.isBlank())
        assertFalse(validState.phone.text.isBlank())
        assertFalse(validState.address.text.isBlank())
        assertTrue(validState.isUserInfoLoaded)

        val expectedSubtotal = (mockPurchase1.price * mockPurchase1.buyCount) +
                (mockPurchase2.price * mockPurchase2.buyCount)
        val actualSubtotal = validState.selectedItems.sumOf { it.price.toDouble() * it.buyCount }
        assertEquals(expectedSubtotal.toDouble(), actualSubtotal, 0.01)
    }

    @Test
    fun `state handles empty selected items correctly`() {
        val stateWithEmptyItems = CreateOrderViewModel.OrderFormState(
            selectedItems = emptyList(),
            name = TextFieldValue("John Doe"),
            phone = TextFieldValue("0123456789"),
            address = TextFieldValue("123 Test Street")
        )

        assertTrue(stateWithEmptyItems.selectedItems.isEmpty())

        val total = stateWithEmptyItems.selectedItems.sumOf { it.price.toDouble() * it.buyCount }
        assertEquals(0.0, total, 0.01)
    }

    @Test
    fun `state handles editing modes correctly`() {
        val editingState = CreateOrderViewModel.OrderFormState(
            isNameEditing = true,
            isPhoneEditing = false,
            isAddressEditing = true,
            nameEdited = true,
            phoneEdited = false,
            addressEdited = true
        )

        assertTrue(editingState.isNameEditing)
        assertFalse(editingState.isPhoneEditing)
        assertTrue(editingState.isAddressEditing)
        assertTrue(editingState.nameEdited)
        assertFalse(editingState.phoneEdited)
        assertTrue(editingState.addressEdited)
    }

    @Test
    fun `state handles loading and error states`() {
        val loadingState = CreateOrderViewModel.OrderFormState(isLoading = true)
        assertTrue(loadingState.isLoading)
        assertNull(loadingState.error)

        val errorState = CreateOrderViewModel.OrderFormState(
            isLoading = false,
            error = "Network error occurred"
        )
        assertFalse(errorState.isLoading)
        assertEquals("Network error occurred", errorState.error)
    }

    @Test
    fun `state TextFieldValue operations work correctly`() {
        val state = CreateOrderViewModel.OrderFormState()

        val nameWithSelection = TextFieldValue(
            text = "John Doe",
            selection = androidx.compose.ui.text.TextRange(0, 4)
        )

        val updatedState = state.copy(name = nameWithSelection)

        assertEquals("John Doe", updatedState.name.text)
        assertEquals(0, updatedState.name.selection.start)
        assertEquals(4, updatedState.name.selection.end)
    }
}