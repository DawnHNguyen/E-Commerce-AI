package com.ptit.core.shop

import android.net.Uri
import app.cash.turbine.test
import com.ptit.domain.entity.shop.ShopDomainEntity
import com.ptit.domain.repository.FileUploadRepository
import com.ptit.domain.repository.ShopRepository
import com.ptit.domain.utils.BadRequestException
import com.ptit.domain.utils.Resource
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
class ShopViewModelTest {

    @Mock
    private lateinit var shopRepository: ShopRepository

    @Mock
    private lateinit var fileUploadRepository: FileUploadRepository

    @Mock
    private lateinit var mockUri: Uri

    private lateinit var viewModel: ShopViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val validShopName = "My Shop"
    private val shopNameTooLong = "a".repeat(51)
    private val shopNameWithSpecialChars = "My Shop@!"
    private val validShopDescription = "This is a valid description."
    private val shopDescriptionTooLong = "a".repeat(201)
    private val validShopAddress = "123 Main St"
    private val shopAddressTooLong = "a".repeat(51)

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = ShopViewModel(shopRepository, fileUploadRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `WorkspaceMyShopDetails success updates uiModel and shopDetailsState`() = runTest {
        val shopEntity = ShopDomainEntity(name = "Test Shop", description = "Description")
        whenever(shopRepository.getMyShop()).thenReturn(Resource.Success(shopEntity))

        viewModel.fetchMyShopDetails()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(shopEntity, viewModel.uiModel.first().shop)
        assertTrue(viewModel.shopDetailsState.first() is Resource.Success)
        assertEquals(shopEntity, (viewModel.shopDetailsState.first() as Resource.Success).data)
    }

    @Test
    fun `WorkspaceMyShopDetails error updates shopDetailsState with error`() = runTest {
        val exception = BadRequestException(null, "Error fetching shop")
        whenever(shopRepository.getMyShop()).thenReturn(Resource.Error(exception))

        viewModel.fetchMyShopDetails()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.shopDetailsState.first() is Resource.Error)
        assertEquals(exception, (viewModel.shopDetailsState.first() as Resource.Error).error)
    }

    @Test
    fun `updateShopDetails success updates uiModel and updateShopState`() = runTest {
        val updatedShop = ShopDomainEntity(name = validShopName, description = validShopDescription, address = validShopAddress, phone = "123456789", avatar = "avatar.png")
        whenever(shopRepository.updateShop(any(), any(), any(), any(), any())).thenReturn(Resource.Success(updatedShop))

        viewModel.updateShopDetails(updatedShop)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(updatedShop, viewModel.uiModel.first().shop)
        assertTrue(viewModel.updateShopState.first() is Resource.Success)
        assertEquals(updatedShop, (viewModel.updateShopState.first() as Resource.Success).data)
    }

    @Test
    fun `updateShopDetails error updates updateShopState with error`() = runTest {
        val shopToUpdate = ShopDomainEntity(name = validShopName, description = "Desc", address = "Addr", phone = "123", avatar = "ava.png")
        val exception = BadRequestException(null, "Error updating shop")
        whenever(shopRepository.updateShop(any(), any(), any(), any(), any())).thenReturn(Resource.Error(exception))

        viewModel.updateShopDetails(shopToUpdate)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.updateShopState.first() is Resource.Error)
        assertEquals(exception, (viewModel.updateShopState.first() as Resource.Error).error)
    }

    @Test
    fun `uploadShopImage success updates uploadImageState with image url`() = runTest {
        val imageUrl = "http://example.com/image.png"
        whenever(fileUploadRepository.uploadSingleFile(mockUri)).thenReturn(Resource.Success(imageUrl))

        viewModel.uploadShopImage(mockUri)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uploadImageState.test {
            val emission = awaitItem()
            assertTrue(emission is Resource.Success)
            assertEquals(imageUrl, (emission as Resource.Success).data)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `uploadShopImage error updates uploadImageState with error`() = runTest {
        val exception = BadRequestException(null, "Error uploading image")
        whenever(fileUploadRepository.uploadSingleFile(mockUri)).thenReturn(Resource.Error(exception))

        viewModel.uploadShopImage(mockUri)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uploadImageState.test {
            val emission = awaitItem()
            assertTrue(emission is Resource.Error)
            assertEquals(exception, (emission as Resource.Error).error)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `initialStates are correct`() = runTest {
        assertEquals(ShopUiModel(), viewModel.uiModel.value)
        assertTrue(viewModel.shopDetailsState.value is Resource.Idle)
        assertTrue(viewModel.updateShopState.value is Resource.Idle)
        assertTrue(viewModel.uploadImageState.value is Resource.Idle)
    }

    @Test
    fun `updateShopDetails with shop name too long should ideally be prevented by UI or return validation error`() = runTest {
        val shopWithLongName = ShopDomainEntity(name = shopNameTooLong, description = validShopDescription, address = validShopAddress)
        val exception = BadRequestException(null, "Shop name too long")
        whenever(shopRepository.updateShop(shopNameTooLong, validShopDescription, validShopAddress, "", ""))
            .thenReturn(Resource.Error(exception))

        viewModel.updateShopDetails(shopWithLongName)
        testDispatcher.scheduler.advanceUntilIdle()
        val result = viewModel.updateShopState.value
        assertTrue(result is Resource.Error)
        assertEquals("Shop name too long", (result as Resource.Error).error.message)
    }

    @Test
    fun `updateShopDetails with shop name containing special characters should ideally be prevented by UI or return validation error`() = runTest {
        val shopWithSpecialCharsName = ShopDomainEntity(name = shopNameWithSpecialChars, description = validShopDescription, address = validShopAddress)
        val exception = BadRequestException(null, "Shop name contains special characters")
        whenever(shopRepository.updateShop(shopNameWithSpecialChars, validShopDescription, validShopAddress, "", ""))
            .thenReturn(Resource.Error(exception))

        viewModel.updateShopDetails(shopWithSpecialCharsName)
        testDispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.updateShopState.value
        assertTrue(result is Resource.Error)
        assertEquals("Shop name contains special characters", (result as Resource.Error).error.message)
    }

    @Test
    fun `updateShopDetails with shop description too long should ideally be prevented by UI or return validation error`() = runTest {
        val shopWithLongDesc = ShopDomainEntity(name = validShopName, description = shopDescriptionTooLong, address = validShopAddress)
        val exception = BadRequestException(null, "Shop description too long")
        whenever(shopRepository.updateShop(validShopName, shopDescriptionTooLong, validShopAddress, "", ""))
            .thenReturn(Resource.Error(exception))

        viewModel.updateShopDetails(shopWithLongDesc)
        testDispatcher.scheduler.advanceUntilIdle()
        val result = viewModel.updateShopState.value
        assertTrue(result is Resource.Error)
        assertEquals("Shop description too long", (result as Resource.Error).error.message)
    }

    @Test
    fun `updateShopDetails with shop address too long should ideally be prevented by UI or return validation error`() = runTest {
        val shopWithLongAddress = ShopDomainEntity(name = validShopName, description = validShopDescription, address = shopAddressTooLong)
        val exception = BadRequestException(null, "Shop address too long")
        whenever(shopRepository.updateShop(validShopName, validShopDescription, shopAddressTooLong, "", ""))
            .thenReturn(Resource.Error(exception))

        viewModel.updateShopDetails(shopWithLongAddress)
        testDispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.updateShopState.value
        assertTrue(result is Resource.Error)
        assertEquals("Shop address too long", (result as Resource.Error).error.message)
    }
}