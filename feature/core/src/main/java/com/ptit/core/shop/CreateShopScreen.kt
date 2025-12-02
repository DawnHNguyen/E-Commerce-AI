package com.ptit.core.shop

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.ptit.common.R
import com.ptit.common.presentation.component.FilledTextField
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.domain.utils.Resource

@Deprecated(
    message = "Use CreateSellerRequestScreen instead. Users must submit a seller request and wait for admin approval.",
    replaceWith = ReplaceWith(
        "CreateSellerRequestScreen(onNavigateBack, onRequestSubmitted)",
        "com.ptit.core.seller_request.CreateSellerRequestScreen"
    ),
    level = DeprecationLevel.WARNING
)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun CreateShopScreen(
    viewModel: ShopViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onShopCreated: () -> Unit
) {
    val context = LocalContext.current

    // States
    var shopName by remember { mutableStateOf(TextFieldValue("")) }
    var shopDescription by remember { mutableStateOf(TextFieldValue("")) }
    var shopAddress by remember { mutableStateOf(TextFieldValue("")) }
    var shopPhone by remember { mutableStateOf(TextFieldValue("")) }
    var shopAvatarUrl by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val createShopState by viewModel.createShopState.collectAsStateWithLifecycle()
    val uploadImageState by viewModel.uploadImageState.collectAsStateWithLifecycle()

    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            viewModel.uploadShopImage(it)
        }
    }

    // Handle upload image result
    LaunchedEffect(uploadImageState) {
        when (uploadImageState) {
            is Resource.Success -> {
                shopAvatarUrl = (uploadImageState as Resource.Success).data
                Toast.makeText(context, "Tải ảnh thành công!", Toast.LENGTH_SHORT).show()
                viewModel.resetUploadImageState()
            }
            is Resource.Error -> {
                Toast.makeText(
                    context,
                    (uploadImageState as Resource.Error).error.message ?: "Tải ảnh thất bại",
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.resetUploadImageState()
            }
            else -> {}
        }
    }

    // Handle create shop result
    LaunchedEffect(createShopState) {
        when (createShopState) {
            is Resource.Success -> {
                Toast.makeText(context, "Tạo cửa hàng thành công!", Toast.LENGTH_SHORT).show()
                viewModel.resetCreateShopState()
                onShopCreated()
            }
            is Resource.Error -> {
                Toast.makeText(
                    context,
                    (createShopState as Resource.Error).error.message ?: "Tạo cửa hàng thất bại",
                    Toast.LENGTH_LONG
                ).show()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Tạo cửa hàng",
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
                    .height(220.dp)
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
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .border(4.dp, colorResource(R.color.colorSystem_heading_button), CircleShape)
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri != null || shopAvatarUrl.isNotEmpty()) {
                            GlideImage(
                                model = selectedImageUri ?: shopAvatarUrl,
                                contentDescription = "Logo cửa hàng",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(colorResource(R.color.colorSystem_background_level_2)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.Store,
                                        contentDescription = null,
                                        modifier = Modifier.size(56.dp),
                                        tint = colorResource(R.color.colorSystem_heading_button)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Thêm logo",
                                        style = CustomTypography.TextMedium.copy(fontSize = 14.sp),
                                        color = colorResource(R.color.colorSystem_text_button)
                                    )
                                }
                            }
                        }

                        // Upload indicator
                        if (uploadImageState is Resource.Loading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = colorResource(R.color.colorSystem_greyscale_0_white)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "Nhấn để tải logo cửa hàng",
                        style = CustomTypography.TextRegular.copy(fontSize = 14.sp),
                        color = colorResource(R.color.colorSystem_text_button)
                    )
                }
            }

            // Form fields
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
                        onValueChange = { shopName = it },
                        hint = "Nhập tên cửa hàng của bạn",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Shop Description
                Column {
                    Text(
                        text = "Mô tả cửa hàng",
                        style = CustomTypography.TextSemiBold.copy(fontSize = 15.sp),
                        color = colorResource(R.color.colorSystem_heading_button)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FilledTextField(
                        value = shopDescription,
                        onValueChange = { shopDescription = it },
                        hint = "Giới thiệu về cửa hàng của bạn",
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )
                }

                // Phone
                Column {
                    Text(
                        text = "Số điện thoại",
                        style = CustomTypography.TextSemiBold.copy(fontSize = 15.sp),
                        color = colorResource(R.color.colorSystem_heading_button)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FilledTextField(
                        value = shopPhone,
                        onValueChange = { shopPhone = it },
                        hint = "Nhập số điện thoại liên hệ",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Address
                Column {
                    Text(
                        text = "Địa chỉ cửa hàng",
                        style = CustomTypography.TextSemiBold.copy(fontSize = 15.sp),
                        color = colorResource(R.color.colorSystem_heading_button)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FilledTextField(
                        value = shopAddress,
                        onValueChange = { shopAddress = it },
                        hint = "Nhập địa chỉ cửa hàng",
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Create Button
                Button(
                    onClick = {
                        if (shopName.text.isBlank()) {
                            Toast.makeText(context, "Vui lòng nhập tên cửa hàng", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.createShop(
                            name = shopName.text.trim(),
                            description = shopDescription.text.trim().ifBlank { null },
                            address = shopAddress.text.trim().ifBlank { null },
                            phone = shopPhone.text.trim().ifBlank { null },
                            avatar = shopAvatarUrl.ifBlank { null }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = createShopState !is Resource.Loading && shopName.text.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.colorSystem_heading_button)
                    )
                ) {
                    if (createShopState is Resource.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = colorResource(R.color.colorSystem_greyscale_0_white)
                        )
                    } else {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = colorResource(R.color.colorSystem_greyscale_0_white)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Tạo cửa hàng",
                            style = CustomTypography.TextBold.copy(fontSize = 16.sp),
                            color = colorResource(R.color.colorSystem_greyscale_0_white)
                        )
                    }
                }

                // Info card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = colorResource(R.color.colorSystem_background_level_2)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = colorResource(R.color.colorSystem_heading_button),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Sau khi tạo cửa hàng, bạn có thể bắt đầu thêm sản phẩm và quản lý kinh doanh của mình.",
                            style = CustomTypography.TextRegular.copy(fontSize = 14.sp),
                            color = colorResource(R.color.colorSystem_normal_text)
                        )
                    }
                }
            }
        }
    }
}

