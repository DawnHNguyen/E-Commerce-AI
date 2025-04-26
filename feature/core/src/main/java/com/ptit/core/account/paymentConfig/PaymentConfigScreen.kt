package com.ptit.core.account.paymentConfig

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ptit.common.R
import com.ptit.common.presentation.component.FilledButton
import com.ptit.common.presentation.component.FilledTextField
import com.ptit.common.presentation.component.FullScreenProgressBar
import com.ptit.common.presentation.component.LocalBottomNavigationVisibility
import com.ptit.common.presentation.rememberDerivedState
import com.ptit.common.presentation.rememberState
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.common.utils.safeCollectFlow
import com.ptit.domain.utils.onError
import com.ptit.domain.utils.onLoading
import com.ptit.domain.utils.onSuccess
import com.recurly.androidsdk.data.model.CreditCardsParameters

@Composable
fun PaymentConfigScreen(
    onNavigateBack: () -> Unit,
    onPaymentMethodSaved: () -> Unit,
) {
    LocalBottomNavigationVisibility.current.value = false

    val viewModel: PaymentConfigViewModel = hiltViewModel()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val isShowProgressBar = rememberState { false }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cardIcon = rememberDerivedState {
        getCardIconResource(uiState.value.cardType)
    }

    val cardPhysicalPattern = rememberDerivedState {
        try {
            CreditCardsParameters.valueOf(uiState.value.cardType.uppercase()).getPhysicalPatternArray()
        } catch (_: Exception) {
            emptyList<Int>()
        }
    }

    // Handle saving result
    LaunchedEffect(Unit) {
        lifecycleOwner.safeCollectFlow(viewModel.saveCardState) { resource ->
            resource
                .onLoading {
                    isShowProgressBar.value = true
                }
                .onError { error ->
                    isShowProgressBar.value = false
                    Toast.makeText(
                        context,
                        "Lỗi: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .onSuccess {
                    isShowProgressBar.value = false
                    Toast.makeText(
                        context,
                        "Đã lưu phương thức thanh toán",
                        Toast.LENGTH_SHORT
                    ).show()
                    onPaymentMethodSaved()
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.colorSystem_background_level_0))
            .padding(16.dp)
            .statusBarsPadding()
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = colorResource(id = R.color.colorSystem_heading_button)
                )
            }

            Text(
                text = "Thêm phương thức thanh toán",
                style = CustomTypography.TextBold,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = colorResource(id = R.color.colorSystem_heading_button)
            )

            // Empty space to balance the back button
            Spacer(modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.weight(0.1f))

        // Card Form
        Text(
            text = "Thông tin thẻ",
            style = CustomTypography.TextSemiBold,
            fontSize = 24.sp,
            color = colorResource(id = R.color.colorSystem_heading_button)
        )

        Spacer(modifier = Modifier.height(24.dp))

        FilledTextField(
            modifier = Modifier.fillMaxWidth(),
            value = uiState.value.cardNumber,
            hint = "Số thẻ",
            onValueChange = viewModel::updateCardNumber,
            leadingContent = {
                Image(
                    imageVector = ImageVector.vectorResource(cardIcon.value),
                    contentDescription = null,
                )
            },
            isError = !uiState.value.isCardNumberValid && uiState.value.cardNumber.isNotEmpty(),
            errorMessage = "Số thẻ không hợp lệ",
            visualTransformation = CardNumberVisualTransformation(
                physicalPattern = cardPhysicalPattern.value
            ),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Row for expiration date and CVV
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FilledTextField(
                modifier = Modifier.weight(1f),
                value = uiState.value.expirationDate,
                hint = "MM/YY",
                onValueChange = viewModel::updateExpirationDate,
                isError = !uiState.value.isExpirationDateValid && uiState.value.expirationDate.isNotEmpty(),
                errorMessage = "Ngày hết hạn không hợp lệ",
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
                modifier = Modifier.weight(1f),
                value = uiState.value.cvv,
                hint = "CVV",
                onValueChange = viewModel::updateCvv,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                isError = !uiState.value.isCvvValid && uiState.value.cvv.isNotEmpty(),
                errorMessage = "CVV không hợp lệ"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Default card checkbox
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = uiState.value.useAsDefaultCard,
                onCheckedChange = viewModel::updateUseAsDefaultCard,
                colors = CheckboxDefaults.colors(
                    checkedColor = colorResource(id = R.color.colorSystem_heading_button),
                )
            )

            Text(
                text = "Đặt làm phương thức thanh toán mặc định",
                style = CustomTypography.TextRegular,
                color = colorResource(id = R.color.colorSystem_heading_button),
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        FilledButton(
            onClick = viewModel::tokenizeAndSaveCard,
            enabled = uiState.value.isEnableSaveButton,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            text = "Lưu phương thức thanh toán"
        )
    }

    // Show loading indicator when processing
    if (isShowProgressBar.value) {
        FullScreenProgressBar()
    }
}

private fun getCardIconResource(cardType: String): Int {
    return when (cardType.uppercase()) {
        "VISA" -> com.recurly.androidsdk.R.drawable.ic_visa_card
        "MASTER", "MASTERCARD" -> com.recurly.androidsdk.R.drawable.ic_mastercard_card
        "AMERICAN_EXPRESS", "AMERICANEXPRESS" -> com.recurly.androidsdk.R.drawable.ic_amex_card
        "DISCOVER" -> com.recurly.androidsdk.R.drawable.ic_discover_card
        "JCB" -> com.recurly.androidsdk.R.drawable.ic_jcb_card
        "DINERS_CLUB", "DINERSCLUB" -> com.recurly.androidsdk.R.drawable.ic_diners_club_card
        else -> com.recurly.androidsdk.R.drawable.ic_generic_disabled_card
    }
}