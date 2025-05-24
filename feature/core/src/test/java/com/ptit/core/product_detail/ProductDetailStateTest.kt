package com.ptit.presentation.viewmodel

import com.ptit.domain.entity.cart.PurchaseDomainEntity
import com.ptit.domain.entity.common.UserDomainEntity
import com.ptit.domain.entity.product.CategoryDomainEntity
import com.ptit.domain.entity.product.ProductDomainEntity
import org.junit.Assert.*
import org.junit.Test
import com.ptit.presentation.viewmodel.ProductDetailState
import com.ptit.presentation.viewmodel.AddToCartState
import com.ptit.presentation.viewmodel.SimilarProductsState

class ProductDetailStateTest {

    // Mock Data
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

    // ProductDetailState Tests
    @Test
    fun `ProductDetailState Initial has correct default values`() {
        val state = ProductDetailState.Initial

        assertTrue(state is ProductDetailState.Initial)
//        assertFalse(state is ProductDetailState.Loading)
//        assertFalse(state is ProductDetailState.Success)
//        assertFalse(state is ProductDetailState.Error)
    }

    @Test
    fun `ProductDetailState Loading has correct state`() {
        val state = ProductDetailState.Loading

        assertTrue(state is ProductDetailState.Loading)
//        assertFalse(state is ProductDetailState.Initial)
//        assertFalse(state is ProductDetailState.Success)
//        assertFalse(state is ProductDetailState.Error)
    }

    @Test
    fun `ProductDetailState Success contains correct product data`() {
        val state = ProductDetailState.Success(mockProduct)

        assertTrue(state is ProductDetailState.Success)
        assertEquals(mockProduct, state.product)
        assertEquals("product1", state.product.id)
        assertEquals("Test Product", state.product.name)
        assertEquals(100000, state.product.price)
        assertEquals(10, state.product.quantity)
        assertEquals(4.5f, state.product.rating, 0.01f)
    }

    @Test
    fun `ProductDetailState Error contains correct error message`() {
        val errorMessage = "Failed to load product details"
        val state = ProductDetailState.Error(errorMessage)

        assertTrue(state is ProductDetailState.Error)
        assertEquals(errorMessage, state.message)
        assertFalse(state.message.isEmpty())
    }

    // AddToCartState Tests
    @Test
    fun `AddToCartState Initial has correct default values`() {
        val state = AddToCartState.Initial

        assertTrue(state is AddToCartState.Initial)
//        assertFalse(state is AddToCartState.Loading)
//        assertFalse(state is AddToCartState.Success)
//        assertFalse(state is AddToCartState.Error)
    }

    @Test
    fun `AddToCartState Loading has correct state`() {
        val state = AddToCartState.Loading

        assertTrue(state is AddToCartState.Loading)
//        assertFalse(state is AddToCartState.Initial)
//        assertFalse(state is AddToCartState.Success)
//        assertFalse(state is AddToCartState.Error)
    }

    @Test
    fun `AddToCartState Success contains correct purchase data`() {
        val state = AddToCartState.Success(mockPurchase)

        assertTrue(state is AddToCartState.Success)
        assertEquals(mockPurchase, state.purchase)
        assertEquals("purchase1", state.purchase.id)
        assertEquals(2, state.purchase.buyCount)
        assertEquals(100000, state.purchase.price)
        assertEquals(mockProduct, state.purchase.product)
    }

    @Test
    fun `AddToCartState Error contains correct error message`() {
        val errorMessage = "Failed to add product to cart"
        val state = AddToCartState.Error(errorMessage)

        assertTrue(state is AddToCartState.Error)
        assertEquals(errorMessage, state.message)
        assertFalse(state.message.isEmpty())
    }

    // SimilarProductsState Tests
    @Test
    fun `SimilarProductsState Initial has correct default values`() {
        val state = SimilarProductsState.Initial

        assertTrue(state is SimilarProductsState.Initial)
//        assertFalse(state is SimilarProductsState.Loading)
//        assertFalse(state is SimilarProductsState.Success)
//        assertFalse(state is SimilarProductsState.Error)
    }

    @Test
    fun `SimilarProductsState Loading has correct state`() {
        val state = SimilarProductsState.Loading

        assertTrue(state is SimilarProductsState.Loading)
//        assertFalse(state is SimilarProductsState.Initial)
//        assertFalse(state is SimilarProductsState.Success)
//        assertFalse(state is SimilarProductsState.Error)
    }

    @Test
    fun `SimilarProductsState Success contains correct products list`() {
        val state = SimilarProductsState.Success(mockSimilarProducts)

        assertTrue(state is SimilarProductsState.Success)
        assertEquals(mockSimilarProducts, state.products)
        assertEquals(2, state.products.size)
        assertEquals("Similar Product 1", state.products[0].name)
        assertEquals("Similar Product 2", state.products[1].name)
    }

    @Test
    fun `SimilarProductsState Success handles empty products list`() {
        val emptyProducts = emptyList<ProductDomainEntity>()
        val state = SimilarProductsState.Success(emptyProducts)

        assertTrue(state is SimilarProductsState.Success)
        assertEquals(emptyProducts, state.products)
        assertTrue(state.products.isEmpty())
    }

    @Test
    fun `SimilarProductsState Error contains correct error message`() {
        val errorMessage = "Failed to load similar products"
        val state = SimilarProductsState.Error(errorMessage)

        assertTrue(state is SimilarProductsState.Error)
        assertEquals(errorMessage, state.message)
        assertFalse(state.message.isEmpty())
    }

    // State Equality Tests
    @Test
    fun `ProductDetailState Success equality works correctly`() {
        val state1 = ProductDetailState.Success(mockProduct)
        val state2 = ProductDetailState.Success(mockProduct)
        val state3 = ProductDetailState.Success(mockSimilarProducts[0])

        assertEquals(state1, state2)
        assertNotEquals(state1, state3)
    }

    @Test
    fun `ProductDetailState Error equality works correctly`() {
        val state1 = ProductDetailState.Error("Error message")
        val state2 = ProductDetailState.Error("Error message")
        val state3 = ProductDetailState.Error("Different error message")

        assertEquals(state1, state2)
        assertNotEquals(state1, state3)
    }

    @Test
    fun `AddToCartState Success equality works correctly`() {
        val state1 = AddToCartState.Success(mockPurchase)
        val state2 = AddToCartState.Success(mockPurchase)

        assertEquals(state1, state2)
    }

    @Test
    fun `SimilarProductsState Success equality works correctly`() {
        val state1 = SimilarProductsState.Success(mockSimilarProducts)
        val state2 = SimilarProductsState.Success(mockSimilarProducts)
        val state3 = SimilarProductsState.Success(emptyList())

        assertEquals(state1, state2)
        assertNotEquals(state1, state3)
    }

    // State Transition Tests
    @Test
    fun `states can transition properly`() {
        var currentState: ProductDetailState = ProductDetailState.Initial
        assertTrue(currentState is ProductDetailState.Initial)

        currentState = ProductDetailState.Loading
        assertTrue(currentState is ProductDetailState.Loading)

        currentState = ProductDetailState.Success(mockProduct)
        assertTrue(currentState is ProductDetailState.Success)
        assertEquals(mockProduct, (currentState as ProductDetailState.Success).product)

        currentState = ProductDetailState.Error("Network error")
        assertTrue(currentState is ProductDetailState.Error)
        assertEquals("Network error", (currentState as ProductDetailState.Error).message)
    }
}