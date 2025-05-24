package com.ptit.core.product

import android.net.Uri
import app.cash.turbine.test
import com.ptit.domain.entity.product.CategoryDomainEntity
import com.ptit.domain.entity.product.ProductDomainEntity
import com.ptit.domain.repository.FileUploadRepository
import com.ptit.domain.repository.ProductRepository
import com.ptit.domain.utils.BadRequestException
import com.ptit.domain.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import org.mockito.kotlin.argThat
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class ProductViewModelTest {

    @Mock
    private lateinit var productRepository: ProductRepository

    @Mock
    private lateinit var fileUploadRepository: FileUploadRepository

    @Mock
    private lateinit var mockUri: Uri

    @Mock
    private lateinit var mockUriList: List<Uri>


    private lateinit var viewModel: ProductViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val validProductName = "Test Product"
    private val productNameTooLong = "a".repeat(51)
    private val validProductDescription = "Valid description."
    private val productDescriptionTooLong = "a".repeat(201)
    private val validPrice = 10000
    private val invalidPriceZero = 0
    private val validOriginalPrice = 12000
    private val invalidOriginalPriceNotGreaterThanPrice = 9000
    private val validQuantity = 10
    private val invalidQuantityZero = 0
    private val validCategoryId = "6829c962d68156000d778b45"
    private val validImageUrl = "http://example.com/image.png"
    private val validImageUrls = listOf(validImageUrl, "http://example.com/image2.png")

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = ProductViewModel(productRepository, fileUploadRepository)
        mockUriList = listOf(mockUri, mockUri) // Example with 2 URIs
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `WorkspaceProductList success updates productListState and productList`() = runTest {
        val products = listOf(ProductDomainEntity(id = "1", name = "Product 1"))
        whenever(productRepository.getProductsByShop()).thenReturn(Resource.Success(products))

        viewModel.fetchProductList()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.productListState.value is Resource.Success)
        assertEquals(products, (viewModel.productListState.value as Resource.Success).data)
        assertEquals(products, viewModel.productList.value)
    }

    @Test
    fun `WorkspaceProductList error updates productListState with error`() = runTest {
        val exception = BadRequestException(null, "Error fetching products")
        whenever(productRepository.getProductsByShop()).thenReturn(Resource.Error(exception))

        viewModel.fetchProductList()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.productListState.value is Resource.Error)
        assertEquals(exception, (viewModel.productListState.value as Resource.Error).error)
    }

    @Test
    fun `WorkspaceProductList success with empty list updates productListState and productList`() = runTest {
        val emptyProducts = emptyList<ProductDomainEntity>()
        whenever(productRepository.getProductsByShop()).thenReturn(Resource.Success(emptyProducts))

        viewModel.fetchProductList()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.productListState.value is Resource.Success)
        assertEquals(emptyProducts, (viewModel.productListState.value as Resource.Success).data)
        assertEquals(emptyProducts, viewModel.productList.value)
    }


    @Test
    fun `getCategories success updates categoryListState`() = runTest {
        val categories = listOf(CategoryDomainEntity(id = "cat1", name = "Category 1"))
        whenever(productRepository.getCategories()).thenReturn(Resource.Success(categories))

        viewModel.getCategories()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.categoryListState.test {
            val emission = awaitItem() // Consume initial Idle or Loading
            if (emission is Resource.Loading) awaitItem() // Consume loading if it's the first
            val successEmission = if (viewModel.categoryListState.value is Resource.Success) viewModel.categoryListState.value else awaitItem()

            assertTrue(successEmission is Resource.Success)
            assertEquals(categories, (successEmission as Resource.Success).data)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `getCategories error updates categoryListState with error`() = runTest {
        val exception = BadRequestException(null, "Error fetching categories")
        whenever(productRepository.getCategories()).thenReturn(Resource.Error(exception))

        viewModel.getCategories()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.categoryListState.test {
            val emission = awaitItem() // Consume initial Idle or Loading
            if (emission is Resource.Loading) awaitItem()
            val errorEmission = if (viewModel.categoryListState.value is Resource.Error) viewModel.categoryListState.value else awaitItem()

            assertTrue(errorEmission is Resource.Error)
            assertEquals(exception, (errorEmission as Resource.Error).error)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `getProductDetails success updates productDetailsState`() = runTest {
        val product = ProductDomainEntity(id = "1", name = "Product Detail")
        whenever(productRepository.getProductDetail("1")).thenReturn(Resource.Success(product))

        viewModel.getProductDetails("1")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.productDetailsState.value is Resource.Success)
        assertEquals(product, (viewModel.productDetailsState.value as Resource.Success).data)
    }

    @Test
    fun `getProductDetails error updates productDetailsState with error`() = runTest {
        val exception = BadRequestException(null, "Error fetching product detail")
        whenever(productRepository.getProductDetail("1")).thenReturn(Resource.Error(exception))

        viewModel.getProductDetails("1")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.productDetailsState.value is Resource.Error)
        assertEquals(exception, (viewModel.productDetailsState.value as Resource.Error).error)
    }

    @Test
    fun `createProduct success updates saveProductState and refreshes list`() = runTest {
        val newProduct = ProductDomainEntity(name = validProductName)
        whenever(productRepository.createProduct(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(Resource.Success(newProduct))
        whenever(productRepository.getProductsByShop()).thenReturn(Resource.Success(listOf(newProduct))) // For refresh

        viewModel.createProduct(validProductName, validProductDescription, validPrice, validOriginalPrice, validQuantity, validImageUrls, validCategoryId)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.saveProductState.value is Resource.Success)
        assertEquals(newProduct, (viewModel.saveProductState.value as Resource.Success).data)
        verify(productRepository).getProductsByShop() // Verify refresh was called
    }

    @Test
    fun `createProduct error updates saveProductState with error`() = runTest {
        val exception = BadRequestException(null, "Error creating product")
        whenever(productRepository.createProduct(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(Resource.Error(exception))

        viewModel.createProduct(validProductName, validProductDescription, validPrice, validOriginalPrice, validQuantity, validImageUrls, validCategoryId)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.saveProductState.value is Resource.Error)
        assertEquals(exception, (viewModel.saveProductState.value as Resource.Error).error)
    }

    @Test
    fun `updateProduct success updates saveProductState and refreshes list`() = runTest {
        val updatedProduct = ProductDomainEntity(id = "1", name = "Updated Product")
        whenever(productRepository.updateProduct(any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(Resource.Success(updatedProduct))
        whenever(productRepository.getProductsByShop()).thenReturn(Resource.Success(listOf(updatedProduct))) // For refresh

        viewModel.updateProduct("1", "Updated Product", validProductDescription, validPrice, validOriginalPrice, validQuantity, validImageUrls, validCategoryId)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.saveProductState.value is Resource.Success)
        assertEquals(updatedProduct, (viewModel.saveProductState.value as Resource.Success).data)
        verify(productRepository).getProductsByShop() // Verify refresh was called
    }

    @Test
    fun `updateProduct error updates saveProductState with error`() = runTest {
        val exception = BadRequestException(null, "Error updating product")
        whenever(productRepository.updateProduct(any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(Resource.Error(exception))

        viewModel.updateProduct("1", "Updated Name", validProductDescription, validPrice, validOriginalPrice, validQuantity, validImageUrls, validCategoryId)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.saveProductState.value is Resource.Error)
        assertEquals(exception, (viewModel.saveProductState.value as Resource.Error).error)
    }

    @Test
    fun `uploadProductImages success updates uploadImagesState`() = runTest {
        val uploadedUrls = listOf("http://example.com/img1.png", "http://example.com/img2.png")
        whenever(fileUploadRepository.uploadMultipleFiles(mockUriList)).thenReturn(Resource.Success(uploadedUrls))

        viewModel.uploadProductImages(mockUriList)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uploadImagesState.test {
            val emission = awaitItem() // Consume initial Idle or Loading
            if (emission is Resource.Loading) awaitItem()
            val successEmission = if (viewModel.uploadImagesState.value is Resource.Success) viewModel.uploadImagesState.value else awaitItem()

            assertTrue(successEmission is Resource.Success)
            assertEquals(uploadedUrls, (successEmission as Resource.Success).data)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `uploadProductImages error updates uploadImagesState with error`() = runTest {
        val exception = BadRequestException(null, "Error uploading images")
        whenever(fileUploadRepository.uploadMultipleFiles(mockUriList)).thenReturn(Resource.Error(exception))

        viewModel.uploadProductImages(mockUriList)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uploadImagesState.test {
            val emission = awaitItem() // Consume initial Idle or Loading
            if (emission is Resource.Loading) awaitItem()
            val errorEmission = if (viewModel.uploadImagesState.value is Resource.Error) viewModel.uploadImagesState.value else awaitItem()

            assertTrue(errorEmission is Resource.Error)
            assertEquals(exception, (errorEmission as Resource.Error).error)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `deleteProduct success updates deleteProductState and refreshes list`() = runTest {
        whenever(productRepository.deleteProduct("1")).thenReturn(Resource.Success(Unit))
        whenever(productRepository.getProductsByShop()).thenReturn(Resource.Success(emptyList())) // For refresh

        viewModel.deleteProduct("1")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.deleteProductState.value is Resource.Success)
        verify(productRepository).getProductsByShop() // Verify refresh was called
    }

    @Test
    fun `deleteProduct error updates deleteProductState with error`() = runTest {
        val exception = BadRequestException(null, "Error deleting product")
        whenever(productRepository.deleteProduct("1")).thenReturn(Resource.Error(exception))

        viewModel.deleteProduct("1")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.deleteProductState.value is Resource.Error)
    }

    @Test
    fun `createProduct with name too long should result in error from repository`() = runTest {
        val exception = BadRequestException(null, "Product name too long")
        whenever(productRepository.createProduct(
            argThat { length > 50 }, // name
            any(), any(), any(), any(), any(), any(), any()
        )).thenReturn(Resource.Error(exception))

        viewModel.createProduct(productNameTooLong, validProductDescription, validPrice, validOriginalPrice, validQuantity, validImageUrls, validCategoryId)
        testDispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.saveProductState.value
        assertTrue(result is Resource.Error)
        assertEquals("Product name too long", (result as Resource.Error).error.message)
    }

    @Test
    fun `createProduct with description too long should result in error from repository`() = runTest {
        val exception = BadRequestException(null, "Product description too long")
        whenever(productRepository.createProduct(
            any(),
            argThat { length > 200 }, // description
            any(), any(), any(), any(), any(), any()
        )).thenReturn(Resource.Error(exception))

        viewModel.createProduct(validProductName, productDescriptionTooLong, validPrice, validOriginalPrice, validQuantity, validImageUrls, validCategoryId)
        testDispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.saveProductState.value
        assertTrue(result is Resource.Error)
        assertEquals("Product description too long", (result as Resource.Error).error.message)
    }

    @Test
    fun `createProduct with invalid price (zero) should result in error from repository`() = runTest {
        val exception = BadRequestException(null, "Price must be greater than 0")
        whenever(productRepository.createProduct(
            any(), any(),
            argThat { this <= 0 }, // price
            any(), any(), any(), any(), any()
        )).thenReturn(Resource.Error(exception))

        viewModel.createProduct(validProductName, validProductDescription, invalidPriceZero, validOriginalPrice, validQuantity, validImageUrls, validCategoryId)
        testDispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.saveProductState.value
        assertTrue(result is Resource.Error)
        assertEquals("Price must be greater than 0", (result as Resource.Error).error.message)
    }

    @Test
    fun `createProduct with invalid original price (less than price) should result in error from repository`() = runTest {
        val exception = BadRequestException(null, "Original price must be greater than price")
        whenever(productRepository.createProduct(
            any(), any(),
            argThat { this > 0 }, // price
            argThat { this <= validPrice }, // originalPrice
            any(), any(), any(), any()
        )).thenReturn(Resource.Error(exception))

        viewModel.createProduct(validProductName, validProductDescription, validPrice, invalidOriginalPriceNotGreaterThanPrice, validQuantity, validImageUrls, validCategoryId)
        testDispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.saveProductState.value
        assertTrue(result is Resource.Error)
        assertEquals("Original price must be greater than price", (result as Resource.Error).error.message)
    }


    @Test
    fun `createProduct with invalid quantity (zero) should result in error from repository`() = runTest {
        val exception = BadRequestException(null, "Quantity must be greater than 0")
        whenever(productRepository.createProduct(
            any(), any(), any(), any(),
            argThat { this <= 0 }, // quantity
            any(), any(), any()
        )).thenReturn(Resource.Error(exception))

        viewModel.createProduct(validProductName, validProductDescription, validPrice, validOriginalPrice, invalidQuantityZero, validImageUrls, validCategoryId)
        testDispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.saveProductState.value
        assertTrue(result is Resource.Error)
        assertEquals("Quantity must be greater than 0", (result as Resource.Error).error.message)
    }

    @Test
    fun `createProduct without categoryId should result in error from repository if category is required`() = runTest {
        val exception = BadRequestException(null, "Category is required")
        // Giả sử repository sẽ trả về lỗi nếu category trống
        whenever(productRepository.createProduct(
            any(), any(), any(), any(), any(), any(), any(),
            argThat { isEmpty() } // category
        )).thenReturn(Resource.Error(exception))

        viewModel.createProduct(validProductName, validProductDescription, validPrice, validOriginalPrice, validQuantity, validImageUrls, "")
        testDispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.saveProductState.value
        assertTrue(result is Resource.Error)
        assertEquals("Category is required", (result as Resource.Error).error.message)
    }

    @Test
    fun `createProduct without images should result in error from repository if images are required`() = runTest {
        val exception = BadRequestException(null, "At least one image is required")
        // Giả sử repository sẽ trả về lỗi nếu imageFiles trống
        whenever(productRepository.createProduct(
            any(), any(), any(), any(), any(),
            argThat { isEmpty() }, // images
            any(), any()
        )).thenReturn(Resource.Error(exception))


        viewModel.createProduct(validProductName, validProductDescription, validPrice, validOriginalPrice, validQuantity, emptyList(), validCategoryId)
        testDispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.saveProductState.value
        assertTrue(result is Resource.Error)
        assertEquals("At least one image is required", (result as Resource.Error).error.message)
    }


    @Test
    fun `initialStates areCorrect`() = runTest {
        assertTrue(viewModel.productListState.value is Resource.Idle)
        assertTrue(viewModel.productList.value.isEmpty())
        assertTrue(viewModel.productDetailsState.value is Resource.Idle)
        assertTrue(viewModel.saveProductState.value is Resource.Idle)
        assertTrue(viewModel.uploadImagesState.value is Resource.Idle)
        assertTrue(viewModel.deleteProductState.value is Resource.Idle)
        assertTrue(viewModel.categoryListState.value is Resource.Idle)
    }
}