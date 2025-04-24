package com.ptit.core.account.paymentConfig

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ptit.common.presentation.component.FilledButton
import com.ptit.common.presentation.component.FilledTextField
import com.ptit.common.presentation.rememberDerivedState
import com.ptit.common.presentation.theme.CustomTypography
import com.recurly.androidsdk.R
import com.recurly.androidsdk.data.model.CreditCardsParameters

@Composable
fun PaymentConfigScreen() {
    val viewModel: CreditCardViewModel = viewModel()

    val cardNumber = viewModel.cardNumberState.collectAsStateWithLifecycle()
    val expirationDate = viewModel.expirationDateState.collectAsStateWithLifecycle()
    val cvv = viewModel.cvvState.collectAsStateWithLifecycle()
    val cardType = viewModel.cardTypeState.collectAsStateWithLifecycle()
    val cardIcon = rememberDerivedState {
        getCardIconResource(cardType.value)
    }
    val cardPhysicalPattern = rememberDerivedState {
        try {
            CreditCardsParameters.valueOf(cardType.value.uppercase()).getPhysicalPatternArray()
        } catch (_: Exception) {
            emptyList<Int>()
        }
    }

    val isCardNumberValid = viewModel.isCardNumberValidState.collectAsStateWithLifecycle()
    val isExpirationDateValid = viewModel.isExpirationDateValidState.collectAsStateWithLifecycle()
    val isCvvValid = viewModel.isCvvValidState.collectAsStateWithLifecycle()
    val isFormValid = viewModel.isFormValid.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .statusBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterVertically)
    ) {
        Text(
            text = "Enter Payment Information",
            style = CustomTypography.TextSemiBold.merge(fontSize = 32.sp)
        )

        FilledTextField(
            modifier = Modifier.fillMaxWidth(),
            value = cardNumber.value,
            hint = "Card Number",
            onValueChange = viewModel::updateCardNumber,
            leadingContent = {
                Image(
                    imageVector = ImageVector.vectorResource(cardIcon.value),
                    contentDescription = null,
                )
            },
            isError = !isCardNumberValid.value,
            visualTransformation = CardNumberVisualTransformation(
                physicalPattern = cardPhysicalPattern.value
            ),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
        )

        FilledTextField(
            modifier = Modifier.fillMaxWidth(),
            value = expirationDate.value,
            hint = "MM/YY",
            onValueChange = viewModel::updateExpirationDate,
            isError = !isExpirationDateValid.value,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            visualTransformation = object : VisualTransformation {
                override fun filter(text: AnnotatedString): TransformedText {
                    val trimmed = if (text.text.length >= 4) text.text.substring(0, 4) else text.text
                    val formatted = buildString {
                        for (i in trimmed.indices) {
                            if (i == 2) append("/")
                            append(trimmed[i])
                        }
                    }

                    val offsetMapping = object : OffsetMapping {
                        override fun originalToTransformed(offset: Int): Int {
                            return when {
                                offset <= 2 -> offset
                                offset <= 4 -> offset + 1
                                else -> 5
                            }
                        }

                        override fun transformedToOriginal(offset: Int): Int {
                            return when {
                                offset <= 2 -> offset
                                offset <= 5 -> offset - 1
                                else -> 4
                            }
                        }
                    }

                    return TransformedText(AnnotatedString(formatted), offsetMapping)
                }
            }
        )

        FilledTextField(
            modifier = Modifier.fillMaxWidth(),
            value = cvv.value,
            hint = "CVV",
            onValueChange = viewModel::updateCvv,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            isError = !isCvvValid.value
        )

        FilledButton(
            onClick = viewModel::initCreditCardTokenization,
            enabled = isFormValid.value,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            text = "Submit"
        )
    }
}

// Helper function to get the card icon resource based on card type
private fun getCardIconResource(cardType: String): Int {
    return when (cardType.uppercase()) {
        "VISA" -> R.drawable.ic_visa_card
        "MASTERCARD" -> R.drawable.ic_mastercard_card
        "AMERICANEXPRESS" -> R.drawable.ic_amex_card
        "DISCOVER" -> R.drawable.ic_discover_card
        "JCB" -> R.drawable.ic_jcb_card
        "DINERSCLUB" -> R.drawable.ic_diners_club_card
        else -> R.drawable.ic_generic_disabled_card
    }
}