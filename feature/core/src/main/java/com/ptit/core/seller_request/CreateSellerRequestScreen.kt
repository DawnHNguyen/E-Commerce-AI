package com.ptit.core.seller_request

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ptit.common.R
import com.ptit.common.presentation.component.FilledTextField
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.domain.entity.seller_request.SellerRequestDomainEntity
import com.ptit.domain.entity.seller_request.SellerRequestStatus
import com.ptit.domain.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSellerRequestScreen(
    viewModel: SellerRequestViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onRequestSubmitted: () -> Unit
) {
    val context = LocalContext.current

    // States
    var shopName by remember { mutableStateOf(TextFieldValue("")) }
    var shopDescription by remember { mutableStateOf(TextFieldValue("")) }
    var businessLicense by remember { mutableStateOf(TextFieldValue("")) }
    var taxCode by remember { mutableStateOf(TextFieldValue("")) }

    val createRequestState by viewModel.createRequestState.collectAsStateWithLifecycle()
    val myRequestState by viewModel.myRequestState.collectAsStateWithLifecycle()

    // Fetch my request on screen load
    LaunchedEffect(Unit) {
        viewModel.fetchMySellerRequest()
    }

    // Handle create request result
    LaunchedEffect(createRequestState) {
        when (createRequestState) {
            is Resource.Success -> {
                Toast.makeText(
                    context,
                    "Gửi yêu cầu trở thành seller thành công. Vui lòng chờ admin duyệt.",
                    Toast.LENGTH_LONG
                ).show()
                viewModel.resetCreateRequestState()
                // Refresh the request status
                viewModel.fetchMySellerRequest()
            }
            is Resource.Error -> {
                val errorMessage = (createRequestState as Resource.Error).error.message
                Toast.makeText(
                    context,
                    errorMessage ?: "Gửi yêu cầu thất bại",
                    Toast.LENGTH_LONG
                ).show()
                viewModel.resetCreateRequestState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Trở thành Seller",
                        style = CustomTypography.TextBold.copy(fontSize = 20.sp),
                        color = colorResource(R.color.colorSystem_greyscale_0_white)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = colorResource(R.color.colorSystem_greyscale_0_white)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.colorSystem_heading_button),
                    titleContentColor = colorResource(R.color.colorSystem_greyscale_0_white)
                )
            )
        }
    ) { paddingValues ->
        // Show loading while fetching request status
        when (myRequestState) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = colorResource(R.color.colorSystem_heading_button)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Đang tải thông tin...",
                            style = CustomTypography.TextRegular.copy(fontSize = 14.sp),
                            color = colorResource(R.color.colorSystem_greyscale_600)
                        )
                    }
                }
            }
            is Resource.Error -> {
                // Error loading request - show error message and retry button
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.ErrorOutline,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color(0xFFE53935)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Không thể tải thông tin yêu cầu",
                        style = CustomTypography.TextBold.copy(fontSize = 18.sp),
                        color = Color(0xFFE53935)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        (myRequestState as Resource.Error).error.message ?: "Đã xảy ra lỗi",
                        style = CustomTypography.TextRegular.copy(fontSize = 14.sp),
                        color = colorResource(R.color.colorSystem_greyscale_600),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.fetchMySellerRequest() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(R.color.colorSystem_heading_button)
                        )
                    ) {
                        Text("Thử lại")
                    }
                }
            }
            is Resource.Success -> {
                val existingRequest = (myRequestState as Resource.Success).data

                SellerRequestContent(
                    existingRequest = existingRequest,
                    createRequestState = createRequestState,
                    shopName = shopName,
                    onShopNameChange = { shopName = it },
                    shopDescription = shopDescription,
                    onShopDescriptionChange = { shopDescription = it },
                    businessLicense = businessLicense,
                    onBusinessLicenseChange = { businessLicense = it },
                    taxCode = taxCode,
                    onTaxCodeChange = { taxCode = it },
                    onSubmitRequest = { name, desc, license, tax ->
                        viewModel.createSellerRequest(name, desc, license, tax)
                    },
                    paddingValues = paddingValues
                )
            }
            else -> {
                // Idle state - shouldn't happen as we fetch on init
            }
        }
    }
}

@Composable
private fun SellerRequestContent(
    existingRequest: SellerRequestDomainEntity?,
    createRequestState: Resource<SellerRequestDomainEntity>,
    shopName: TextFieldValue,
    onShopNameChange: (TextFieldValue) -> Unit,
    shopDescription: TextFieldValue,
    onShopDescriptionChange: (TextFieldValue) -> Unit,
    businessLicense: TextFieldValue,
    onBusinessLicenseChange: (TextFieldValue) -> Unit,
    taxCode: TextFieldValue,
    onTaxCodeChange: (TextFieldValue) -> Unit,
    onSubmitRequest: (String, String?, String?, String?) -> Unit,
    paddingValues: PaddingValues
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(colorResource(R.color.colorSystem_background_level_0))
            .verticalScroll(rememberScrollState())
    ) {
        // Header with gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colorResource(R.color.colorSystem_heading_button),
                            colorResource(R.color.colorSystem_background_level_0)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Store,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = colorResource(R.color.colorSystem_greyscale_0_white)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Đăng ký trở thành Seller",
                    style = CustomTypography.TextBold.copy(fontSize = 18.sp),
                    color = colorResource(R.color.colorSystem_greyscale_0_white)
                )
            }
        }

        // Show existing request status if any
        existingRequest?.let { request ->
            ExistingRequestCard(request = request)
        }

        // Show form only if no request or rejected (can resubmit)
        if (existingRequest == null || existingRequest.status == SellerRequestStatus.REJECTED) {
            RequestFormContent(
                existingRequest = existingRequest,
                createRequestState = createRequestState,
                shopName = shopName,
                onShopNameChange = onShopNameChange,
                shopDescription = shopDescription,
                onShopDescriptionChange = onShopDescriptionChange,
                businessLicense = businessLicense,
                onBusinessLicenseChange = onBusinessLicenseChange,
                taxCode = taxCode,
                onTaxCodeChange = onTaxCodeChange,
                onSubmitRequest = onSubmitRequest
            )
        } else if (existingRequest.status == SellerRequestStatus.PENDING) {
            // Show waiting message
            WaitingForApprovalContent()
        } else if (existingRequest.status == SellerRequestStatus.APPROVED) {
            // Show success message
            ApprovedContent()
        }
    }
}

@Composable
private fun ExistingRequestCard(request: SellerRequestDomainEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (request.status) {
                SellerRequestStatus.PENDING -> Color(0xFFFFF3CD)
                SellerRequestStatus.APPROVED -> Color(0xFFD4EDDA)
                SellerRequestStatus.REJECTED -> Color(0xFFF8D7DA)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    when (request.status) {
                        SellerRequestStatus.PENDING -> Icons.Default.HourglassEmpty
                        SellerRequestStatus.APPROVED -> Icons.Default.CheckCircle
                        SellerRequestStatus.REJECTED -> Icons.Default.Cancel
                    },
                    contentDescription = null,
                    tint = when (request.status) {
                        SellerRequestStatus.PENDING -> Color(0xFF856404)
                        SellerRequestStatus.APPROVED -> Color(0xFF155724)
                        SellerRequestStatus.REJECTED -> Color(0xFF721C24)
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (request.status) {
                        SellerRequestStatus.PENDING -> "Đang chờ duyệt"
                        SellerRequestStatus.APPROVED -> "Đã được duyệt"
                        SellerRequestStatus.REJECTED -> "Bị từ chối"
                    },
                    style = CustomTypography.TextBold.copy(fontSize = 16.sp),
                    color = when (request.status) {
                        SellerRequestStatus.PENDING -> Color(0xFF856404)
                        SellerRequestStatus.APPROVED -> Color(0xFF155724)
                        SellerRequestStatus.REJECTED -> Color(0xFF721C24)
                    }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Display request details
            RequestDetailItem(label = "Tên cửa hàng", value = request.shopName)
            request.shopDescription?.let {
                Spacer(modifier = Modifier.height(8.dp))
                RequestDetailItem(label = "Mô tả", value = it)
            }
            request.businessLicense?.let {
                Spacer(modifier = Modifier.height(8.dp))
                RequestDetailItem(label = "Giấy phép KD", value = it)
            }
            request.taxCode?.let {
                Spacer(modifier = Modifier.height(8.dp))
                RequestDetailItem(label = "Mã số thuế", value = it)
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = when (request.status) {
                    SellerRequestStatus.PENDING -> "Yêu cầu của bạn đang được xem xét. Vui lòng chờ admin duyệt."
                    SellerRequestStatus.APPROVED -> "Chúc mừng! Bạn đã trở thành Seller."
                    SellerRequestStatus.REJECTED -> "Lý do từ chối: ${request.rejectionReason ?: "Không có lý do cụ thể"}"
                },
                style = CustomTypography.TextRegular.copy(fontSize = 14.sp),
                color = when (request.status) {
                    SellerRequestStatus.PENDING -> Color(0xFF856404)
                    SellerRequestStatus.APPROVED -> Color(0xFF155724)
                    SellerRequestStatus.REJECTED -> Color(0xFF721C24)
                }
            )
        }
    }
}

@Composable
private fun RequestDetailItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = CustomTypography.TextSemiBold.copy(fontSize = 13.sp),
            color = Color(0xFF555555)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = CustomTypography.TextRegular.copy(fontSize = 14.sp),
            color = Color(0xFF333333)
        )
    }
}

@Composable
private fun RequestFormContent(
    existingRequest: SellerRequestDomainEntity?,
    createRequestState: Resource<SellerRequestDomainEntity>,
    shopName: TextFieldValue,
    onShopNameChange: (TextFieldValue) -> Unit,
    shopDescription: TextFieldValue,
    onShopDescriptionChange: (TextFieldValue) -> Unit,
    businessLicense: TextFieldValue,
    onBusinessLicenseChange: (TextFieldValue) -> Unit,
    taxCode: TextFieldValue,
    onTaxCodeChange: (TextFieldValue) -> Unit,
    onSubmitRequest: (String, String?, String?, String?) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Shop Name (Required)
        Column {
            Text(
                text = "Tên cửa hàng *",
                style = CustomTypography.TextSemiBold.copy(fontSize = 15.sp),
                color = colorResource(R.color.colorSystem_heading_button)
            )
            Spacer(modifier = Modifier.height(8.dp))
            FilledTextField(
                value = shopName,
                onValueChange = onShopNameChange,
                hint = "Nhập tên cửa hàng (3-500 ký tự)",
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Shop Description (Optional)
        Column {
            Text(
                text = "Mô tả cửa hàng",
                style = CustomTypography.TextSemiBold.copy(fontSize = 15.sp),
                color = colorResource(R.color.colorSystem_heading_button)
            )
            Spacer(modifier = Modifier.height(8.dp))
            FilledTextField(
                value = shopDescription,
                onValueChange = onShopDescriptionChange,
                hint = "Giới thiệu về cửa hàng của bạn (tối đa 2000 ký tự)",
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
        }

        // Business License (Optional)
        Column {
            Text(
                text = "Giấy phép kinh doanh",
                style = CustomTypography.TextSemiBold.copy(fontSize = 15.sp),
                color = colorResource(R.color.colorSystem_heading_button)
            )
            Spacer(modifier = Modifier.height(8.dp))
            FilledTextField(
                value = businessLicense,
                onValueChange = onBusinessLicenseChange,
                hint = "Nhập số giấy phép kinh doanh (nếu có)",
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Tax Code (Optional)
        Column {
            Text(
                text = "Mã số thuế",
                style = CustomTypography.TextSemiBold.copy(fontSize = 15.sp),
                color = colorResource(R.color.colorSystem_heading_button)
            )
            Spacer(modifier = Modifier.height(8.dp))
            FilledTextField(
                value = taxCode,
                onValueChange = onTaxCodeChange,
                hint = "Nhập mã số thuế (nếu có)",
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Submit Button
        Button(
            onClick = {
                val name = shopName.text.trim()
                if (name.isBlank()) {
                    Toast.makeText(context, "Vui lòng nhập tên cửa hàng", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (name.length < 3) {
                    Toast.makeText(context, "Tên cửa hàng phải có ít nhất 3 ký tự", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (name.length > 500) {
                    Toast.makeText(context, "Tên cửa hàng không được vượt quá 500 ký tự", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val desc = shopDescription.text.trim().ifBlank { null }
                if (desc != null && desc.length > 2000) {
                    Toast.makeText(context, "Mô tả không được vượt quá 2000 ký tự", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                onSubmitRequest(
                    name,
                    desc,
                    businessLicense.text.trim().ifBlank { null },
                    taxCode.text.trim().ifBlank { null }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = createRequestState !is Resource.Loading && shopName.text.isNotBlank(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.colorSystem_heading_button)
            )
        ) {
            if (createRequestState is Resource.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = colorResource(R.color.colorSystem_greyscale_0_white)
                )
            } else {
                Icon(
                    Icons.Default.Send,
                    contentDescription = null,
                    tint = colorResource(R.color.colorSystem_greyscale_0_white)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (existingRequest?.status == SellerRequestStatus.REJECTED) "Gửi lại yêu cầu" else "Gửi yêu cầu",
                    style = CustomTypography.TextBold.copy(fontSize = 16.sp),
                    color = colorResource(R.color.colorSystem_greyscale_0_white)
                )
            }
        }

        // Info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE3F2FD)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF1976D2),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "Lưu ý",
                        style = CustomTypography.TextBold.copy(fontSize = 14.sp),
                        color = Color(0xFF1976D2)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "• Tên cửa hàng là bắt buộc (3-500 ký tự)\n" +
                                "• Mô tả cửa hàng giúp khách hàng hiểu rõ hơn về shop của bạn\n" +
                                "• Giấy phép kinh doanh và mã số thuế là tùy chọn nhưng giúp tăng độ tin cậy\n" +
                                "• Admin sẽ xem xét yêu cầu trong vòng 24-48 giờ",
                        style = CustomTypography.TextRegular.copy(fontSize = 13.sp),
                        color = Color(0xFF1976D2)
                    )
                }
            }
        }
    }
}

@Composable
private fun WaitingForApprovalContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "Bạn không thể gửi yêu cầu mới khi yêu cầu trước đó đang được xem xét.",
            style = CustomTypography.TextRegular.copy(fontSize = 14.sp),
            color = colorResource(R.color.colorSystem_greyscale_600),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ApprovedContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color(0xFF4CAF50)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Bạn đã là Seller!",
                style = CustomTypography.TextBold.copy(fontSize = 20.sp),
                color = Color(0xFF4CAF50)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Bây giờ bạn có thể bắt đầu bán hàng trên nền tảng.",
                style = CustomTypography.TextRegular.copy(fontSize = 14.sp),
                color = colorResource(R.color.colorSystem_greyscale_600),
                textAlign = TextAlign.Center
            )
        }
    }
}

