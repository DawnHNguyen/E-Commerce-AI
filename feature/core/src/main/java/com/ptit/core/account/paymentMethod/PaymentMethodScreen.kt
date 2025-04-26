package com.ptit.core.account.paymentMethod

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.ptit.common.R
import com.ptit.common.presentation.MaxSizeBox
import com.ptit.common.presentation.MaxSizeColumn
import com.ptit.common.presentation.MaxWidthColumn
import com.ptit.common.presentation.MaxWidthRow
import com.ptit.common.presentation.component.FullScreenProgressBar
import com.ptit.common.presentation.component.LocalBottomNavigationVisibility
import com.ptit.common.presentation.rememberState
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.common.utils.safeCollectFlow
import com.ptit.domain.entity.payment.PaymentMethodDomainEntity
import com.ptit.domain.utils.onError
import com.ptit.domain.utils.onLoading
import com.ptit.domain.utils.onSuccess

@Composable
fun PaymentMethodScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddPaymentMethod: () -> Unit,
) {
    LocalBottomNavigationVisibility.current.value = false

    val viewModel = hiltViewModel<PaymentMethodViewModel>()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val isShowProgressBar = rememberState { false }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Handle data loading
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

    // Handle delete operation results
    LaunchedEffect(Unit) {
        lifecycleOwner.safeCollectFlow(viewModel.deleteState) { resource ->
            resource
                .onLoading {
                    isShowProgressBar.value = true
                }
                .onError { error ->
                    isShowProgressBar.value = false
                    Toast.makeText(
                        context,
                        "Không thể xóa phương thức thanh toán: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .onSuccess {
                    isShowProgressBar.value = false
                    Toast.makeText(
                        context,
                        "Đã xóa phương thức thanh toán",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    // Handle set default operation results
    LaunchedEffect(Unit) {
        lifecycleOwner.safeCollectFlow(viewModel.defaultState) { resource ->
            resource
                .onLoading {
                    isShowProgressBar.value = true
                }
                .onError { error ->
                    isShowProgressBar.value = false
                    Toast.makeText(
                        context,
                        "Không thể đặt làm mặc định: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .onSuccess {
                    isShowProgressBar.value = false
                    Toast.makeText(
                        context,
                        "Đã đặt làm phương thức thanh toán mặc định",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    // Main layout
    Scaffold(
        modifier = Modifier
            .background(colorResource(R.color.colorSystem_background_level_0))
            .statusBarsPadding(),
        containerColor = colorResource(id = R.color.colorSystem_background_level_0),
        topBar = {
            // Top bar
            MaxWidthRow(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp),
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
                    text = "Phương thức thanh toán",
                    style = CustomTypography.TextBold,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = colorResource(id = R.color.colorSystem_heading_button)
                )

                // Empty space to balance the back button
                Spacer(modifier = Modifier.size(48.dp))
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddPaymentMethod,
                containerColor = colorResource(id = R.color.colorSystem_heading_button),
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Thêm phương thức thanh toán"
                )
            }
        },
        content = { paddingValues ->
            // Content area
            MaxSizeBox(
                modifier = Modifier.padding(paddingValues)
            ) {
                // Payment methods list or empty state
                if (uiState.value.paymentMethods.isEmpty() && !isShowProgressBar.value) {
                    EmptyPaymentMethodsState(
                        onAddPaymentMethod = onNavigateToAddPaymentMethod
                    )
                } else {
                    PaymentMethodsList(
                        paymentMethods = uiState.value.paymentMethods,
                        onDeletePaymentMethod = { firstSix, lastFour ->
                            viewModel.deletePaymentMethod(firstSix, lastFour)
                        },
                        onSetAsDefault = { firstSix, lastFour ->
                            viewModel.setDefaultPaymentMethod(firstSix, lastFour)
                        }
                    )
                }
            }
        }
    )

    // Loading indicator
    if (isShowProgressBar.value) {
        FullScreenProgressBar()
    }
}

@Composable
private fun EmptyPaymentMethodsState(
    onAddPaymentMethod: () -> Unit,
) {
    MaxSizeColumn(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.CreditCard,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = colorResource(id = R.color.colorSystem_heading_button)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Chưa có phương thức thanh toán",
            style = CustomTypography.TextBold,
            fontSize = 18.sp,
            color = colorResource(id = R.color.colorSystem_heading_button)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Thêm phương thức thanh toán để mua sắm thuận tiện hơn",
            style = CustomTypography.TextRegular,
            fontSize = 14.sp,
            color = colorResource(id = R.color.colorSystem_normal_text),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onAddPaymentMethod),
            colors = CardDefaults.cardColors(
                containerColor = colorResource(id = R.color.colorSystem_background_level_2)
            )
        ) {
            MaxWidthRow(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(colorResource(id = R.color.colorSystem_normal_text).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Thêm",
                        tint = colorResource(id = R.color.colorSystem_heading_button),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                MaxWidthColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Liên kết với thẻ tín dụng",
                        style = CustomTypography.TextMedium,
                        fontSize = 16.sp,
                        color = colorResource(id = R.color.colorSystem_heading_button)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Thêm phương thức thanh toán mới",
                        style = CustomTypography.TextRegular,
                        fontSize = 14.sp,
                        color = colorResource(id = R.color.colorSystem_normal_text)
                    )
                }

                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = "Tiếp tục",
                    tint = colorResource(id = R.color.colorSystem_heading_button)
                )
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun PaymentMethodsList(
    paymentMethods: List<PaymentMethodDomainEntity>,
    onDeletePaymentMethod: (String, String) -> Unit,
    onSetAsDefault: (String, String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(paymentMethods) { paymentMethod ->
            PaymentMethodItem(
                paymentMethod = paymentMethod,
                onDelete = { onDeletePaymentMethod(paymentMethod.firstSixNum, paymentMethod.lastFourNum) },
                onSetAsDefault = { onSetAsDefault(paymentMethod.firstSixNum, paymentMethod.lastFourNum) }
            )

            if (paymentMethod != paymentMethods.last()) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = colorResource(id = R.color.colorSystem_text_button).copy(alpha = 0.2f)
                )
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun PaymentMethodItem(
    paymentMethod: PaymentMethodDomainEntity,
    onDelete: () -> Unit,
    onSetAsDefault: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.colorSystem_background_level_2)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        MaxWidthColumn(
            modifier = Modifier.padding(16.dp)
        ) {
            // Card info row
            MaxWidthRow(
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
                    // Card type and default indicator
                    MaxWidthRow(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = PaymentMethodUtils.getCardTypeName(paymentMethod.cardType),
                            style = CustomTypography.TextMedium,
                            fontSize = 16.sp,
                            color = colorResource(id = R.color.colorSystem_heading_button)
                        )

                        if (paymentMethod.isDefault) {
                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(colorResource(id = R.color.colorSystem_normal_text))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Mặc định",
                                    style = CustomTypography.TextMedium,
                                    fontSize = 10.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Card number preview
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

                // Delete button
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Xóa",
                        tint = colorResource(id = R.color.colorSystem_tint_red)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Set as default row - only show if not already default
            if (!paymentMethod.isDefault) {
                MaxWidthRow(
                    modifier = Modifier
                        .clickable(onClick = onSetAsDefault)
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = false,
                        onCheckedChange = { onSetAsDefault() }
                    )

                    Text(
                        text = "Đặt làm phương thức thanh toán mặc định",
                        style = CustomTypography.TextRegular,
                        color = colorResource(id = R.color.colorSystem_heading_button),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            } else {
                MaxWidthRow(
                    modifier = Modifier
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "Đang sử dụng",
                        tint = colorResource(id = R.color.colorSystem_normal_text),
                        modifier = Modifier.size(24.dp)
                    )

                    Text(
                        text = "Đang sử dụng làm phương thức thanh toán mặc định",
                        style = CustomTypography.TextRegular,
                        color = colorResource(id = R.color.colorSystem_normal_text),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}
