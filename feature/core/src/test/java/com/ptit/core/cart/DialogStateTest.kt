package com.ptit.core.cart // Hoặc package test của bạn

import DialogState
import com.ptit.domain.entity.cart.PurchaseDomainEntity
import com.ptit.domain.entity.common.UserDomainEntity
import com.ptit.domain.entity.product.CategoryDomainEntity
import com.ptit.domain.entity.product.ProductDomainEntity
import org.junit.Assert.*
import org.junit.Test

class DialogStateTest {

    // Mock Data
    private val mockUser = UserDomainEntity(
        id = "user1", name = "Test User", phone = "123", email = "a@b.c", address = "123",
        shop = UserDomainEntity.Shop("Shop", "Addr", "Phone", "Desc", "Ava")
    )
    private val mockProduct = ProductDomainEntity(
        id = "product1", name = "Test Product", price = 100000, priceBeforeDiscount = 120000,
        image = "image.jpg", images = listOf("image.jpg"), quantity = 10,
        rating = 4.5f, sold = 50, view = 100, description = "Desc",
        category = CategoryDomainEntity(), shop = mockUser, createdAt = "2024-01-01", updatedAt = "2024-01-01"
    )
    private val mockPurchase = PurchaseDomainEntity(
        id = "purchase1", buyCount = 1, price = 100000, priceBeforeDiscount = 120000,
        status = 1, user = "user1", product = mockProduct, createdAt = "2024-01-01", updatedAt = "2024-01-01"
    )

    @Test
    fun `Hidden state should be an object`() {
        val state = DialogState.Hidden
        assertNotNull(state)
        assertTrue(state is DialogState.Hidden)
    }

    @Test
    fun `DeleteSingleItem holds the correct item`() {
        val state = DialogState.DeleteSingleItem(mockPurchase)

        assertTrue(state is DialogState.DeleteSingleItem)
        assertEquals(mockPurchase, state.item)
        assertEquals("purchase1", state.item.id)
    }

    @Test
    fun `DeleteMultipleItems holds the correct set of ids`() {
        val itemIds = setOf("purchase1", "purchase2", "purchase3")
        val state = DialogState.DeleteMultipleItems(itemIds)

        assertTrue(state is DialogState.DeleteMultipleItems)
        assertEquals(itemIds, state.itemIds)
        assertEquals(3, state.itemIds.size)
        assertTrue(state.itemIds.contains("purchase2"))
    }

    @Test
    fun `DeleteMultipleItems handles empty set`() {
        val itemIds = emptySet<String>()
        val state = DialogState.DeleteMultipleItems(itemIds)

        assertTrue(state is DialogState.DeleteMultipleItems)
        assertTrue(state.itemIds.isEmpty())
    }

    @Test
    fun `States are distinct`() {
        val hidden = DialogState.Hidden
        val single = DialogState.DeleteSingleItem(mockPurchase)
        val multiple = DialogState.DeleteMultipleItems(setOf("id1"))

        assertNotEquals(hidden, single)
        assertNotEquals(hidden, multiple)
        assertNotEquals(single, multiple)
    }
}