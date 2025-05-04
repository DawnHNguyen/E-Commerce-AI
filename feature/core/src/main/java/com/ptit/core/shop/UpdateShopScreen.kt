package com.ptit.core.shop

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.ptit.common.presentation.MaxSizeColumn
import com.ptit.common.presentation.MyCrossFade
import com.ptit.common.presentation.component.FullScreenProgressBar
import com.ptit.common.presentation.rememberState
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.common.utils.safeCollectFlow
import com.ptit.domain.entity.shop.ShopDomainEntity
import com.ptit.domain.utils.onError
import com.ptit.domain.utils.onLoading
import com.ptit.domain.utils.onSuccess

@Composable
fun UpdateShopScreen(
    onNavigateBack: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val viewModel = hiltViewModel<ShopViewModel>()
    val uiModel = viewModel.uiModel.collectAsStateWithLifecycle()
    val isLoading = rememberState { false }

    var shopName by remember { mutableStateOf("") }
    var shopDescription by remember { mutableStateOf("") }
    var shopAddress by remember { mutableStateOf("") }
    var shopAvatar by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Initialize form fields with current shop data
    LaunchedEffect(uiModel.value.shop) {
        shopName = uiModel.value.shop.name
        shopDescription = uiModel.value.shop.description
        shopAddress = uiModel.value.shop.address
        shopAvatar = uiModel.value.shop.avatar
    }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            // Upload image and get URL
            viewModel.uploadShopImage(it)
        }
    }

    // Observe image upload status
    LaunchedEffect(Unit) {
        lifecycleOwner.safeCollectFlow(viewModel.uploadImageState) {
            it
                .onLoading {
                    isLoading.value = true
                }
                .onError { error ->
                    isLoading.value = false
                    Toast.makeText(
                        context,
                        "Không thể tải hình ảnh: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .onSuccess { imageUrl ->
                    isLoading.value = false
                    shopAvatar = imageUrl
                    Toast.makeText(
                        context,
                        "Tải hình ảnh thành công",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    // Observe update shop status
    LaunchedEffect(Unit) {
        lifecycleOwner.safeCollectFlow(viewModel.updateShopState) {
            it
                .onLoading {
                    isLoading.value = true
                }
                .onError { error ->
                    isLoading.value = false
                    Toast.makeText(
                        context,
                        "Cập nhật thông tin thất bại: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .onSuccess { _ ->
                    isLoading.value = false
                    Toast.makeText(
                        context,
                        "Cập nhật thông tin thành công",
                        Toast.LENGTH_SHORT
                    ).show()
                    onNavigateBack()
                }
        }
    }

    MaxSizeColumn(
        modifier = Modifier
            .background(colorResource(R.color.colorSystem_background_level_0))
            .statusBarsPadding()
    ) {
        // Top bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Back button
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = colorResource(id = R.color.colorSystem_heading_button)
                )
            }

            // Title
            Text(
                text = "Cập nhật cửa hàng",
                style = CustomTypography.TextBold,
                fontSize = 20.sp,
                modifier = Modifier.align(Alignment.Center),
                textAlign = TextAlign.Center,
                color = colorResource(id = R.color.colorSystem_heading_button)
            )
        }

        // Content
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Shop avatar editor
            AvatarEditor(
                currentAvatar = shopAvatar,
                shopName = shopName,
                onSelectImage = { imagePickerLauncher.launch("image/*") }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Shop details form
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Shop name field
                    Text(
                        text = "Tên cửa hàng",
                        style = CustomTypography.TextSemiBold,
                        fontSize = 16.sp,
                        color = colorResource(id = R.color.colorSystem_heading_button)
                    )

                    OutlinedTextField(
                        value = shopName,
                        onValueChange = { shopName = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        placeholder = {
                            Text(
                                "Nhập tên cửa hàng",
                                style = CustomTypography.TextRegular,
                                color = colorResource(id = R.color.colorSystem_text_button)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorResource(id = R.color.colorSystem_heading_button),
                            unfocusedBorderColor = colorResource(id = R.color.colorSystem_stroke)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Shop description field
                    Text(
                        text = "Mô tả cửa hàng",
                        style = CustomTypography.TextSemiBold,
                        fontSize = 16.sp,
                        color = colorResource(id = R.color.colorSystem_heading_button)
                    )

                    OutlinedTextField(
                        value = shopDescription,
                        onValueChange = { shopDescription = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .height(120.dp),
                        placeholder = {
                            Text(
                                "Mô tả về cửa hàng của bạn",
                                style = CustomTypography.TextRegular,
                                color = colorResource(id = R.color.colorSystem_text_button)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorResource(id = R.color.colorSystem_heading_button),
                            unfocusedBorderColor = colorResource(id = R.color.colorSystem_stroke)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Shop address field
                    Text(
                        text = "Địa chỉ cửa hàng",
                        style = CustomTypography.TextSemiBold,
                        fontSize = 16.sp,
                        color = colorResource(id = R.color.colorSystem_heading_button)
                    )

                    OutlinedTextField(
                        value = shopAddress,
                        onValueChange = { shopAddress = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        placeholder = {
                            Text(
                                "Nhập địa chỉ cửa hàng",
                                style = CustomTypography.TextRegular,
                                color = colorResource(id = R.color.colorSystem_text_button)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorResource(id = R.color.colorSystem_heading_button),
                            unfocusedBorderColor = colorResource(id = R.color.colorSystem_stroke)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save button
            Button(
                onClick = {
                    viewModel.updateShopDetails(
                        ShopDomainEntity(
                            name = shopName,
                            description = shopDescription,
                            address = shopAddress,
                            avatar = shopAvatar,
                            phone = uiModel.value.shop.phone // Keep existing phone number
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.colorSystem_heading_button)
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = shopName.isNotEmpty() && !isLoading.value
            ) {
                Text(
                    text = "Lưu thay đổi",
                    style = CustomTypography.TextSemiBold,
                    fontSize = 16.sp,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }

    // Show loading indicator when processing
    if (isLoading.value) {
        FullScreenProgressBar()
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun AvatarEditor(
    currentAvatar: String,
    shopName: String,
    onSelectImage: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(150.dp)
            .clip(CircleShape)
            .background(colorResource(R.color.colorSystem_background_level_2))
            .border(2.dp, colorResource(R.color.colorSystem_stroke), CircleShape)
            .clickable { onSelectImage() },
        contentAlignment = Alignment.Center
    ) {
        if (currentAvatar.isNotEmpty()) {
            GlideImage(
                model = currentAvatar,
                contentDescription = "Logo cửa hàng",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop,
                transition = MyCrossFade
            ) {
                it.centerCrop()
            }
        } else {
            Text(
                text = if (shopName.isNotEmpty()) shopName.first().toString().uppercase() else "?",
                style = CustomTypography.TextBold,
                fontSize = 50.sp,
                color = colorResource(id = R.color.colorSystem_heading_button)
            )
        }

        // Camera icon overlay
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.CameraAlt,
                contentDescription = "Chọn ảnh",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Nhấn để thay đổi ảnh",
        style = CustomTypography.TextRegular,
        fontSize = 14.sp,
        color = colorResource(id = R.color.colorSystem_normal_text)
    )
}