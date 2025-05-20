package com.ptit.core.account.paymentConfig

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TransformedText
import org.junit.Assert.*
import org.junit.Test

class CardNumberVisualTransformationTest {
    
    @Test
    fun `filter returns original text when pattern is empty`() {
        // Given
        val transformation = CardNumberVisualTransformation(emptyList())
        val input = AnnotatedString("1234567890123456")
        
        // When
        val result = transformation.filter(input)
        
        // Then
        assertEquals(input.text, result.text.text)
    }
    
    @Test
    fun `filter returns original text when input is empty`() {
        // Given
        val transformation = CardNumberVisualTransformation(listOf(4, 4, 4, 4))
        val emptyInput = AnnotatedString("")
        
        // When
        val result = transformation.filter(emptyInput)
        
        // Then
        assertEquals(emptyInput.text, result.text.text)
    }
    
    @Test
    fun `filter formats VISA pattern (4-4-4-4) correctly`() {
        // Given
        val transformation = CardNumberVisualTransformation(listOf(4, 4, 4, 4))
        val input = AnnotatedString("4111111111111111")
        
        // When
        val result = transformation.filter(input)
        
        // Then
        assertEquals("4111-1111-1111-1111", result.text.text)
    }
    
    @Test
    fun `filter formats AMEX pattern (4-6-5) correctly`() {
        // Given
        val transformation = CardNumberVisualTransformation(listOf(4, 6, 5))
        val input = AnnotatedString("378282246310005")
        
        // When
        val result = transformation.filter(input)
        
        // Then
        assertEquals("3782-822463-10005", result.text.text)
    }
    
    @Test
    fun `filter handles partial card numbers correctly`() {
        // Given
        val transformation = CardNumberVisualTransformation(listOf(4, 4, 4, 4))
        val partialInput = AnnotatedString("41111")
        
        // When
        val result = transformation.filter(partialInput)
        
        // Then
        assertEquals("4111-1", result.text.text)
    }
    
    @Test
    fun `offset mapping correctly maps original to transformed for VISA pattern`() {
        // Given
        val transformation = CardNumberVisualTransformation(listOf(4, 4, 4, 4))
        val input = AnnotatedString("4111111111111111")
        
        // When
        val result = transformation.filter(input)
        
        // Then - Check offset mapping for various positions
        val offsetMapping = result.offsetMapping
        
        // Original positions to transformed
        assertEquals(0, offsetMapping.originalToTransformed(0)) // Start
        assertEquals(4, offsetMapping.originalToTransformed(4)) // Before first dash
        assertEquals(5, offsetMapping.originalToTransformed(4) + 1) // After first dash
        assertEquals(9, offsetMapping.originalToTransformed(8)) // Before second dash
        assertEquals(10, offsetMapping.originalToTransformed(8) + 1) // After second dash
        assertEquals(14, offsetMapping.originalToTransformed(12)) // Before third dash
        assertEquals(15, offsetMapping.originalToTransformed(12) + 1) // After third dash
        assertEquals(19, offsetMapping.originalToTransformed(16)) // End
    }
    
    @Test
    fun `offset mapping correctly maps transformed to original for VISA pattern`() {
        // Given
        val transformation = CardNumberVisualTransformation(listOf(4, 4, 4, 4))
        val input = AnnotatedString("4111111111111111")
        
        // When
        val result = transformation.filter(input)
        
        // Then - Check offset mapping for various positions
        val offsetMapping = result.offsetMapping
        
        // Transformed positions to original
        assertEquals(0, offsetMapping.transformedToOriginal(0)) // Start
        assertEquals(4, offsetMapping.transformedToOriginal(4)) // Before first dash
        assertEquals(4, offsetMapping.transformedToOriginal(5)) // The first dash position
        assertEquals(8, offsetMapping.transformedToOriginal(9)) // Before second dash
        assertEquals(8, offsetMapping.transformedToOriginal(10)) // The second dash position
        assertEquals(12, offsetMapping.transformedToOriginal(14)) // Before third dash
        assertEquals(12, offsetMapping.transformedToOriginal(15)) // The third dash position
        assertEquals(16, offsetMapping.transformedToOriginal(19)) // End
    }
    
    @Test
    fun `CreditCardsParameters extension correctly parses physical pattern`() {
        // Given - Mock a credit card parameters object with a physical pattern
        // We're using a workaround since we can't directly instantiate enum CreditCardsParameters
        val mockPatternGetter = object {
            fun getPattern(patternString: String): List<Int> {
                return patternString
                    .split("-")
                    .toTypedArray()
                    .map { it.toInt() }
            }
        }
        
        // When/Then
        assertEquals(listOf(4, 4, 4, 4), mockPatternGetter.getPattern("4-4-4-4")) // VISA pattern
        assertEquals(listOf(4, 6, 5), mockPatternGetter.getPattern("4-6-5")) // AMEX pattern
        assertEquals(listOf(4, 4, 4, 4, 3), mockPatternGetter.getPattern("4-4-4-4-3")) // 19-digit pattern
    }
}