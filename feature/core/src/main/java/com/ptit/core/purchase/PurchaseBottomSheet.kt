package com.ptit.core.purchase

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.ptit.common.R
import com.ptit.common.presentation.MaxWidthColumn
import com.ptit.common.presentation.MaxWidthRow
import com.ptit.common.presentation.component.BaseBottomSheet
import com.ptit.common.presentation.component.FilledButton
import com.ptit.common.presentation.component.FullScreenProgressBar
import com.ptit.common.presentation.rememberState
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.common.utils.safeCollectFlow
import com.ptit.core.account.paymentMethod.PaymentMethodUtils
import com.ptit.domain.entity.payment.PaymentMethodDomainEntity
import com.ptit.domain.utils.onError
import com.ptit.domain.utils.onLoading
import com.ptit.domain.utils.onSuccess
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onAddPaymentMethod: () -> Unit,
    onConfirmedPurchase: (String) -> Unit,
) {
    val viewModel: PurchaseBottomSheetViewModel = hiltViewModel()

    val context = LocalContext.current as FragmentActivity
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val paymentMethods by viewModel.paymentMethodsState.collectAsStateWithLifecycle()
    val isShowProgressBar = rememberState { false }

    val modalBottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    // Handle payment methods loading state
    LaunchedEffect(Unit) {
        lifecycleOwner.safeCollectFlow(viewModel.paymentMethodsState) { resource ->
            resource
                .onLoading {
                    isShowProgressBar.value = true
                }
                .onError { error ->
                    isShowProgressBar.value = false
                    Toast.makeText(
                        context,
                        "Không thể tải dữ liệu: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .onSuccess {
                    isShowProgressBar.value = false
                }
        }
    }

    BaseBottomSheet(
        modalSheetState = modalBottomSheetState,
        isShowBottomSheet = isVisible,
        onDismiss = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorResource(id = R.color.colorSystem_background_level_0))
                .padding(bottom = 24.dp)
        ) {
            // Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = colorResource(id = R.color.colorSystem_background_level_2)
                ),
                shape = RoundedCornerShape(0.dp)
            ) {
                MaxWidthColumn(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Phương thức thanh toán",
                        style = CustomTypography.TextBold,
                        fontSize = 18.sp,
                        color = colorResource(id = R.color.colorSystem_heading_button),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Content
            if (uiState.value.selectedPaymentMethod == null && !isShowProgressBar.value) {
                NoPaymentMethodState(onAddPaymentMethod = onAddPaymentMethod)
            } else {
                PaymentMethodSelectionState(
                    selectedPaymentMethod = uiState.value.selectedPaymentMethod,
                    paymentMethods = paymentMethods.dataOrNull ?: emptyList(),
                    isSelectingPaymentMethod = uiState.value.isSelectingPaymentMethod,
                    onToggleSelection = viewModel::togglePaymentMethodSelection,
                    onSelectPaymentMethod = viewModel::selectPaymentMethod,
                    onAddPaymentMethod = onAddPaymentMethod,
                    onConfirmPayment = {
//                        if (context is FragmentActivity) {
                        viewModel.startAuthentication()

                        if (BiometricUtil.canAuthenticate(context)) {
                            BiometricUtil.showBiometricPrompt(
                                activity = context,
                                title = "Xác thực thanh toán",
                                subtitle = "Sử dụng sinh trắc học để xác thực",
                                description = "Xác thực để hoàn tất thanh toán",
                                onSuccess = {
                                    scope.launch {
                                        viewModel.getTokenizedPaymentInfo()?.let { token ->
                                            // Set this payment method as default
                                            viewModel.setDefaultPaymentMethod()
                                            onConfirmedPurchase(token)
                                        } ?: run {
                                            Toast.makeText(
                                                context,
                                                "Lỗi: Không thể lấy thông tin thanh toán",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        viewModel.finishAuthentication()
                                    }
                                },
                                onError = { error ->
                                    viewModel.finishAuthentication()
                                    Toast.makeText(
                                        context,
                                        "Xác thực thất bại: $error",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        } else {
                            // Biometric authentication not available, proceed anyway
                            scope.launch {
                                viewModel.getTokenizedPaymentInfo()?.let { token ->
                                    // Set this payment method as default
                                    viewModel.setDefaultPaymentMethod()
                                    onConfirmedPurchase(token)
                                } ?: run {
                                    Toast.makeText(
                                        context,
                                        "Lỗi: Không thể lấy thông tin thanh toán",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                viewModel.finishAuthentication()
                            }
                        }
//                        }
                    }
                )
            }
        }
    }

    // Loading indicator
    if (isShowProgressBar.value || uiState.value.isAuthenticating) {
        FullScreenProgressBar()
    }
}

@Composable
private fun NoPaymentMethodState(
    onAddPaymentMethod: () -> Unit,
) {
    MaxWidthColumn(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.CreditCard,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = colorResource(id = R.color.colorSystem_heading_button)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Bạn chưa có phương thức thanh toán !!!",
            style = CustomTypography.TextMedium,
            fontSize = 16.sp,
            color = colorResource(id = R.color.colorSystem_heading_button),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        FilledButton(
            onClick = onAddPaymentMethod,
            text = "Cài đặt",
            modifier = Modifier.width(120.dp)
        )
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun PaymentMethodSelectionState(
    selectedPaymentMethod: PaymentMethodDomainEntity?,
    paymentMethods: List<PaymentMethodDomainEntity>,
    isSelectingPaymentMethod: Boolean,
    onToggleSelection: () -> Unit,
    onSelectPaymentMethod: (PaymentMethodDomainEntity) -> Unit,
    onAddPaymentMethod: () -> Unit,
    onConfirmPayment: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        selectedPaymentMethod?.let { paymentMethod ->
            // Current selected payment method
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                MaxWidthRow(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Card type icon
                    GlideImage(
                        model = PaymentMethodUtils.getCardTypeIcon(paymentMethod.cardType),
                        contentDescription = "Card Type",
                        modifier = Modifier.size(40.dp),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    MaxWidthColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = PaymentMethodUtils.getCardTypeName(paymentMethod.cardType),
                            style = CustomTypography.TextMedium,
                            fontSize = 16.sp,
                            color = colorResource(id = R.color.colorSystem_heading_button)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = PaymentMethodUtils.formatCardNumber(
                                paymentMethod.firstSixNum,
                                paymentMethod.lastFourNum
                            ),
                            style = CustomTypography.TextRegular,
                            fontSize = 14.sp,
                            color = colorResource(id = R.color.colorSystem_normal_text)
                        )
                    }

                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "Selected",
                        tint = colorResource(id = R.color.colorSystem_heading_button)
                    )
                }
            }
        }

        // Choose another payment method section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleSelection() },
            colors = CardDefaults.cardColors(
                containerColor = colorResource(id = R.color.colorSystem_background_level_2)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            MaxWidthColumn {
                // Header for selection
                MaxWidthRow(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CreditCard,
                        contentDescription = null,
                        tint = colorResource(id = R.color.colorSystem_heading_button),
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Chọn phương thức thanh toán khác",
                        style = CustomTypography.TextMedium,
                        modifier = Modifier.weight(1f),
                        color = colorResource(id = R.color.colorSystem_heading_button)
                    )

                    val rotationAngle = if (isSelectingPaymentMethod) 180f else 0f
                    Icon(
                        imageVector = if (isSelectingPaymentMethod)
                            Icons.Outlined.KeyboardArrowUp
                        else
                            Icons.Outlined.KeyboardArrowDown,
                        contentDescription = if (isSelectingPaymentMethod) "Hide" else "Show",
                        tint = colorResource(id = R.color.colorSystem_heading_button)
                    )
                }

                // Payment method selection list
                if (isSelectingPaymentMethod && paymentMethods.isNotEmpty()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = colorResource(id = R.color.colorSystem_text_button).copy(alpha = 0.2f)
                    )

                    // Limit height and make scrollable
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp), // Fixed height to prevent very long lists
                        state = rememberLazyListState()
                    ) {
                        items(paymentMethods.filter { it.firstSixNum != selectedPaymentMethod?.firstSixNum || it.lastFourNum != selectedPaymentMethod.lastFourNum }) { paymentMethod ->
                            MaxWidthRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelectPaymentMethod(paymentMethod)
                                    }
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = false, // Always false because we filter out the selected one
                                    onClick = { onSelectPaymentMethod(paymentMethod) }
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                GlideImage(
                                    model = PaymentMethodUtils.getCardTypeIcon(paymentMethod.cardType),
                                    contentDescription = "Card Type",
                                    modifier = Modifier.size(32.dp),
                                    contentScale = ContentScale.Fit
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = PaymentMethodUtils.formatCardNumber(
                                        paymentMethod.firstSixNum,
                                        paymentMethod.lastFourNum
                                    ),
                                    style = CustomTypography.TextRegular,
                                    color = colorResource(id = R.color.colorSystem_normal_text)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Add payment method
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .clickable(onClick = onAddPaymentMethod),
            colors = CardDefaults.cardColors(
                containerColor = colorResource(id = R.color.colorSystem_background_level_2)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            MaxWidthRow(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colorResource(id = R.color.colorSystem_heading_button).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Add",
                        tint = colorResource(id = R.color.colorSystem_heading_button),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Thêm phương thức thanh toán mới",
                    style = CustomTypography.TextMedium,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f),
                    color = colorResource(id = R.color.colorSystem_heading_button)
                )

                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = "Navigate",
                    tint = colorResource(id = R.color.colorSystem_heading_button)
                )
            }
        }

        // Confirm button
        if (selectedPaymentMethod != null) {
            Spacer(modifier = Modifier.height(32.dp))

            FilledButton(
                onClick = onConfirmPayment,
                text = "Xác nhận thanh toán",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
