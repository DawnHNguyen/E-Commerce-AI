package com.ptit.core.account.paymentConfig

import org.junit.Assert.*
import org.junit.Test

class PaymentConfigUiStateTest {

    @Test
    fun `default state has expected values`() {
        val state = PaymentConfigUiState()
        
        // Check default values
        assertEquals("", state.cardNumber)
        assertEquals("", state.expirationDate)
        assertEquals("", state.cvv)
        assertEquals("", state.cardType)
        assertTrue(state.isCardNumberValid) // Default to true (no validation error shown initially)
        assertTrue(state.isExpirationDateValid)
        assertTrue(state.isCvvValid)
        assertFalse(state.useAsDefaultCard)
        
        // Save button should be disabled with empty fields
        assertFalse(state.isEnableSaveButton)
    }

    @Test
    fun `isEnableSaveButton returns true only when all fields are valid and non-empty`() {
        // Valid state with all fields populated and valid
        val validState = PaymentConfigUiState(
            cardNumber = "4111111111111111",
            expirationDate = "1225",
            cvv = "123",
            cardType = "VISA",
            isCardNumberValid = true,
            isExpirationDateValid = true,
            isCvvValid = true
        )
        
        assertTrue(validState.isEnableSaveButton)
        
        // Test each invalid condition separately
        
        // Invalid card number
        val invalidCardState = validState.copy(isCardNumberValid = false)
        assertFalse(invalidCardState.isEnableSaveButton)
        
        // Invalid expiration date
        val invalidExpirationState = validState.copy(isExpirationDateValid = false)
        assertFalse(invalidExpirationState.isEnableSaveButton)
        
        // Invalid CVV
        val invalidCvvState = validState.copy(isCvvValid = false)
        assertFalse(invalidCvvState.isEnableSaveButton)
        
        // Empty card number
        val emptyCardState = validState.copy(cardNumber = "")
        assertFalse(emptyCardState.isEnableSaveButton)
        
        // Empty expiration date
        val emptyExpirationState = validState.copy(expirationDate = "")
        assertFalse(emptyExpirationState.isEnableSaveButton)
        
        // Empty CVV
        val emptyCvvState = validState.copy(cvv = "")
        assertFalse(emptyCvvState.isEnableSaveButton)
    }

    @Test
    fun `copy method maintains all fields and allows partial updates`() {
        val initialState = PaymentConfigUiState(
            cardNumber = "4111111111111111",
            expirationDate = "1225",
            cvv = "123",
            cardType = "VISA",
            isCardNumberValid = true,
            isExpirationDateValid = true,
            isCvvValid = true,
            useAsDefaultCard = true
        )
        
        // Test partial update
        val updatedState = initialState.copy(
            cardNumber = "5555555555554444", 
            cardType = "MASTERCARD"
        )
        
        // Verify updated fields
        assertEquals("5555555555554444", updatedState.cardNumber)
        assertEquals("MASTERCARD", updatedState.cardType)
        
        // Verify unchanged fields
        assertEquals("1225", updatedState.expirationDate)
        assertEquals("123", updatedState.cvv)
        assertTrue(updatedState.isCardNumberValid)
        assertTrue(updatedState.isExpirationDateValid)
        assertTrue(updatedState.isCvvValid)
        assertTrue(updatedState.useAsDefaultCard)
    }

    @Test
    fun `isEnableSaveButton works with edge cases`() {
        // All fields valid but at minimum length
        val minimalValidState = PaymentConfigUiState(
            cardNumber = "4111111111111", // Minimum valid card (13 digits for some cards)
            expirationDate = "0130", // Future date
            cvv = "123",
            cardType = "VISA",
            isCardNumberValid = true,
            isExpirationDateValid = true,
            isCvvValid = true
        )
        
        assertTrue(minimalValidState.isEnableSaveButton)
        
        // State with spaces in card number (should still be valid)
        val spaceInCardNumber = PaymentConfigUiState(
            cardNumber = "4111 1111 1111 1111", // With spaces
            expirationDate = "0130",
            cvv = "123",
            cardType = "VISA",
            isCardNumberValid = true,
            isExpirationDateValid = true,
            isCvvValid = true
        )
        
        assertTrue(spaceInCardNumber.isEnableSaveButton)
    }
}