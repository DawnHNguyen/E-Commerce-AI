package com.ptit.core.account

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.recurly.androidsdk.data.model.CreditCardsParameters

fun CreditCardsParameters.getPhysicalPatternArray(): List<Int> =
    physicalPattern
        .split("-")
        .toTypedArray()
        .map { it.toInt() }

class CardNumberVisualTransformation(private val physicalPattern: List<Int>) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        if (physicalPattern.isEmpty() || text.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val inputText = text.text
        val resultBuilder = StringBuilder()

        var patternIndex = 0
        var charCount = 0
        val dashIndices = mutableListOf<Int>()

        for (i in inputText.indices) {
            resultBuilder.append(inputText[i])
            charCount++

            // Check if we need to add a dash
            if (charCount == physicalPattern[patternIndex] && i < inputText.length - 1) {
                resultBuilder.append('-')
                dashIndices.add(i + 1)
                charCount = 0
                patternIndex = (patternIndex + 1) % physicalPattern.size
            }
        }

        // Create offset mapping
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                // Count dashes before the offset
                val dashesBeforeOffset = dashIndices.count { it < offset }
                return offset + dashesBeforeOffset
            }

            override fun transformedToOriginal(offset: Int): Int {
                // Count dashes in the transformed text up to offset
                var dashCount = 0
                for (dashPos in dashIndices) {
                    val transformedPos = dashPos + dashCount
                    if (transformedPos < offset) {
                        dashCount++
                    } else if (transformedPos == offset) {
                        return dashPos
                    } else {
                        break
                    }
                }
                return offset - dashCount
            }
        }

        return TransformedText(AnnotatedString(resultBuilder.toString()), offsetMapping)
    }
}