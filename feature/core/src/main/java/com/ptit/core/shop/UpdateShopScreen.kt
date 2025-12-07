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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.ptit.domain.utils.Resource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun UpdateShopScreen(
    viewModel: ShopViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val uiModel by viewModel.uiModel.collectAsStateWithLifecycle()
    val updateShopState by viewModel.updateShopState.collectAsStateWithLifecycle()
    val uploadImageState by viewModel.uploadImageState.collectAsStateWithLifecycle()

    // Initialize states from current shop data
    var shopName by remember { mutableStateOf(uiModel.shop.name) }
    var shopDescription by remember { mutableStateOf(uiModel.shop.description ?: "") }
    var shopAddress by remember { mutableStateOf(uiModel.shop.address ?: "") }
    var shopPhone by remember { mutableStateOf(uiModel.shop.phone ?: "") }
    var shopAvatarUrl by remember { mutableStateOf(uiModel.shop.avatar ?: "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Update local states when shop data changes
    LaunchedEffect(uiModel.shop) {
        shopName = uiModel.shop.name
        shopDescription = uiModel.shop.description ?: ""
        shopAddress = uiModel.shop.address ?: ""
        shopPhone = uiModel.shop.phone ?: ""
        shopAvatarUrl = uiModel.shop.avatar ?: ""
    }

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
                Toast.makeText(context, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                viewModel.resetUploadImageState()
            }
            is Resource.Error -> {
                Toast.makeText(
                    context,
                    (uploadImageState as Resource.Error).error.message ?: "Upload failed",
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.resetUploadImageState()
            }
            else -> {}
        }
    }

    // Handle update shop result
    LaunchedEffect(updateShopState) {
        when (updateShopState) {
            is Resource.Success -> {
                Toast.makeText(context, "Shop updated successfully!", Toast.LENGTH_SHORT).show()
                onNavigateBack()
            }
            is Resource.Error -> {
                Toast.makeText(
                    context,
                    (updateShopState as Resource.Error).error.message ?: "Failed to update shop",
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
                        "Update Shop Details",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.surface
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
                            .border(4.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri != null || shopAvatarUrl.isNotEmpty()) {
                            GlideImage(
                                model = selectedImageUri ?: shopAvatarUrl,
                                contentDescription = "Shop Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Store,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }

                        // Camera icon overlay
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = "Change photo",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Upload indicator
                        if (uploadImageState is Resource.Loading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "Tap to change shop logo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
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
                OutlinedTextField(
                    value = shopName,
                    onValueChange = { shopName = it },
                    label = { Text("Shop Name *") },
                    leadingIcon = {
                        Icon(Icons.Default.Store, contentDescription = null)
                    },
                    placeholder = { Text("Enter your shop name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                // Shop Description
                OutlinedTextField(
                    value = shopDescription,
                    onValueChange = { shopDescription = it },
                    label = { Text("Shop Description") },
                    leadingIcon = {
                        Icon(Icons.Default.Description, contentDescription = null)
                    },
                    placeholder = { Text("Tell customers about your shop") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp)
                )

                // Phone
                OutlinedTextField(
                    value = shopPhone,
                    onValueChange = { shopPhone = it },
                    label = { Text("Phone Number") },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null)
                    },
                    placeholder = { Text("Enter contact phone") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Address
                OutlinedTextField(
                    value = shopAddress,
                    onValueChange = { shopAddress = it },
                    label = { Text("Shop Address") },
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                    },
                    placeholder = { Text("Enter shop address") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Update Button
                Button(
                    onClick = {
                        if (shopName.isBlank()) {
                            Toast.makeText(context, "Shop name is required", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.updateShopDetails(
                            name = if (shopName != uiModel.shop.name) shopName.trim() else null,
                            description = if (shopDescription != (uiModel.shop.description ?: "")) shopDescription.trim().ifBlank { null } else null,
                            address = if (shopAddress != (uiModel.shop.address ?: "")) shopAddress.trim().ifBlank { null } else null,
                            phone = if (shopPhone != (uiModel.shop.phone ?: "")) shopPhone.trim().ifBlank { null } else null,
                            avatar = if (shopAvatarUrl != (uiModel.shop.avatar ?: "")) shopAvatarUrl.ifBlank { null } else null
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = updateShopState !is Resource.Loading && shopName.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (updateShopState is Resource.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Save Changes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Shop Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Shop Information",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            "Shop ID: ${uiModel.shop.id}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Status: ${if (uiModel.shop.isActive) "Active" else "Inactive"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (uiModel.shop.isActive) Color(0xFF4CAF50) else Color(0xFFF44336),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
