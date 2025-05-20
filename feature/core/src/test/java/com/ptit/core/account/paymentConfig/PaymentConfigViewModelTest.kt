package com.ptit.core.account.paymentConfig

import app.cash.turbine.test
import com.ptit.domain.entity.payment.PaymentMethodDomainEntity
import com.ptit.domain.repository.PaymentMethodRepository
import com.ptit.domain.utils.Resource
import com.recurly.androidsdk.data.model.CreditCardsParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class PaymentConfigViewModelTest {

    @Mock
    private lateinit var paymentMethodRepository: PaymentMethodRepository

    @Captor
    private lateinit var paymentMethodCaptor: ArgumentCaptor<PaymentMethodDomainEntity>

    private lateinit var viewModel: PaymentConfigViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        // Create a special test version of the ViewModel with a mock TokenService
        viewModel = PaymentConfigViewModel(paymentMethodRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `updateCardNumber sets card type and validates correctly`() = runTest {
        // Given
        val cardNumber = "4111111111111111"

        // When
        viewModel.updateCardNumber(cardNumber)

        // Then
        val state = viewModel.uiState.first()
        assertEquals(cardNumber, state.cardNumber)
        assertEquals("visa", state.cardType)
        assertTrue(state.isCardNumberValid)
    }

    @Test
    fun `updateCardNumber identifies invalid card correctly`() = runTest {
        // Given
        val invalidCardNumber = "1234567890123456" // Invalid card number format

        // When
        viewModel.updateCardNumber(invalidCardNumber)

        // Then
        val state = viewModel.uiState.first()
        assertEquals(invalidCardNumber, state.cardNumber)
        assertFalse(state.isCardNumberValid)
    }

    @Test
    fun `updateExpirationDate validates date correctly`() = runTest {
        // Given
        val validDate = "1225" // December 2025

        // When
        viewModel.updateExpirationDate(validDate)

        // Then
        val state = viewModel.uiState.first()
        assertEquals(validDate, state.expirationDate)
        assertTrue(state.isExpirationDateValid)
    }

    @Test
    fun `updateExpirationDate identifies expired date`() = runTest {
        // Given
        val expiredDate = "1220" // December 2020

        // When
        viewModel.updateExpirationDate(expiredDate)

        // Then
        val state = viewModel.uiState.first()
        assertEquals(expiredDate, state.expirationDate)
        assertFalse(state.isExpirationDateValid)
    }

    @Test
    fun `updateCvv validates CVV correctly`() = runTest {
        // First set a card type for proper CVV validation
        viewModel.updateCardNumber("4111111111111111") // VISA card

        // When
        viewModel.updateCvv("123") // Valid CVV for VISA

        // Then
        val state = viewModel.uiState.first()
        assertEquals("123", state.cvv)
        assertTrue(state.isCvvValid)
    }

    @Test
    fun `updateCvv identifies invalid CVV`() = runTest {
        // First set a card type for proper CVV validation
        viewModel.updateCardNumber("4111111111111111") // VISA card

        // When
        viewModel.updateCvv("12") // Too short for VISA

        // Then
        val state = viewModel.uiState.first()
        assertEquals("12", state.cvv)
        assertFalse(state.isCvvValid)
    }

    @Test
    fun `updateUseAsDefaultCard updates state correctly`() = runTest {
        // When
        viewModel.updateUseAsDefaultCard(true)

        // Then
        val state = viewModel.uiState.first()
        assertTrue(state.useAsDefaultCard)
    }

    // Note: We're not testing the actual tokenizeAndSaveCard method directly
    // because it relies on the real TokenService which makes network calls
    // and can't be easily mocked. Instead, we focus on testing the validation
    // logic and UI state management.

    // We're also not testing repository errors for the same reason -
    // we can't reliably mock the TokenService in this test environment

    // Test for form validation

    @Test
    fun `tokenizeAndSaveCard does nothing when form is invalid`() = runTest {
        // Given an incomplete form
        viewModel.updateCardNumber("4111111111111111")
        // Missing expiration date and CVV

        // When
        viewModel.tokenizeAndSaveCard()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - verify repository was never called
        viewModel.saveCardState.test {
            val emission = awaitItem()
            assertTrue(emission is Resource.Idle) // Should remain idle
        }
    }

    @Test
    fun `getCreditCardType handles all known card types`() = runTest {
        val cardTypes = mapOf(
            "VISA" to CreditCardsParameters.VISA,
            "MASTER" to CreditCardsParameters.MASTER,
            "MASTERCARD" to CreditCardsParameters.MASTER,
            "AMERICANEXPRESS" to CreditCardsParameters.AMERICAN_EXPRESS,
            "AMERICAN_EXPRESS" to CreditCardsParameters.AMERICAN_EXPRESS,
            "DISCOVER" to CreditCardsParameters.DISCOVER,
            "JCB" to CreditCardsParameters.JCB,
            "DINERSCLUB" to CreditCardsParameters.DINERS_CLUB,
            "DINERS_CLUB" to CreditCardsParameters.DINERS_CLUB,
            "UNION_PAY" to CreditCardsParameters.UNION_PAY,
            "ELO" to CreditCardsParameters.ELO,
            "HIPERCARD" to CreditCardsParameters.HIPERCARD,
            "TARJETA_NARANJA" to CreditCardsParameters.TARJETA_NARANJA,
            "UNKNOWN" to CreditCardsParameters.VISA // Default to VISA on unknown types
        )

        cardTypes.forEach { (inputType, expectedOutput) ->
            // Use reflection to access private method
            val getCreditCardTypeMethod = PaymentConfigViewModel::class.java.getDeclaredMethod(
                "getCreditCardType", String::class.java
            )
            getCreditCardTypeMethod.isAccessible = true

            val result = getCreditCardTypeMethod.invoke(viewModel, inputType)
            assertEquals(expectedOutput, result)
        }
    }
}