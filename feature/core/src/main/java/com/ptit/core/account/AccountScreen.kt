package com.ptit.core.account

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.ptit.common.R
import com.ptit.common.presentation.MaxSizeColumn
import com.ptit.common.presentation.MaxWidthBox
import com.ptit.common.presentation.MaxWidthColumn
import com.ptit.common.presentation.MaxWidthRow
import com.ptit.common.presentation.MyCrossFade
import com.ptit.common.presentation.component.FullScreenProgressBar
import com.ptit.common.presentation.component.LocalBottomNavigationVisibility
import com.ptit.common.presentation.rememberState
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.common.utils.safeCollectFlow
import com.ptit.domain.entity.common.UserDomainEntity
import com.ptit.domain.utils.onError
import com.ptit.domain.utils.onLoading
import com.ptit.domain.utils.onSuccess
import com.ptit.common.utils.toPriceFormat

@Composable
fun AccountScreen(
    backStackEntry: NavBackStackEntry,
    onNavigateToOrders: () -> Unit,
    onNavigateToOverview: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToPaymentMethods: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToShop: () -> Unit,
    onNavigateToCreateShop: () -> Unit,
    onLogoutSuccess: () -> Unit,
) {
    LocalBottomNavigationVisibility.current.value = true

    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    val viewModel = hiltViewModel<AccountViewModel>(viewModelStoreOwner = backStackEntry)
    val uiModel = viewModel.uiModel.collectAsStateWithLifecycle()
    val isShowProgressBar = rememberState { false }

    // Handle user profile data
    LaunchedEffect(Unit) {
        lifecycleOwner.safeCollectFlow(viewModel.userProfileState) {
            it
                .onLoading {
                    isShowProgressBar.value = true
                }
                .onError {
                    isShowProgressBar.value = false
                    Toast.makeText(
                        context,
                        "Không thể tải thông tin cá nhân: ${it.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .onSuccess {
                    isShowProgressBar.value = false
                }
        }

        lifecycleOwner.safeCollectFlow(viewModel.logoutState) {
            it
                .onLoading {
                    isShowProgressBar.value = true
                }
                .onError {
                    isShowProgressBar.value = false
                    Toast.makeText(
                        context,
                        "Đăng xuất thất bại: ${it.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .onSuccess {
                    isShowProgressBar.value = false
                    onLogoutSuccess()
                }
        }
    }

    // Main content
    MaxSizeColumn(
        modifier = Modifier
            .background(colorResource(R.color.colorSystem_background_level_0))
            .statusBarsPadding()
    ) {
        // Top Bar
        Text(
            text = "Tài khoản",
            style = CustomTypography.TextBold,
            fontSize = 20.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            textAlign = TextAlign.Center,
            color = colorResource(id = R.color.colorSystem_heading_button)
        )

        // Content
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .weight(1f)
        ) {
            // Profile Card
            ProfileCard(
                user = uiModel.value.user,
                onEditClick = {
                    viewModel.onEditProfile()
                    onNavigateToEditProfile()
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ✅ Order Statistics Section with real data
            OrderStatisticsSection(
                totalOrders = uiModel.value.totalOrders,
                totalSpent = uiModel.value.totalSpent,
                isLoading = uiModel.value.isLoadingStats,
                onViewOverviewClick = onNavigateToOverview
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Menu Items
            SettingsMenuCard(
                isSettingsExpanded = uiModel.value.isSettingsExpanded,
                //hasShop = uiModel.value.user.shop.name.isNotEmpty(),\
                hasShop = false,
                onSettingsClick = viewModel::toggleSettingsExpanded,
                onOverviewClick = onNavigateToOverview,
                onOrdersClick = onNavigateToOrders,
                onShopClick = {
                    // Nếu đã có shop thì đến ShopDetail, chưa có thì đến CreateSellerRequest
                    if (false) { // hasShop = false
                        onNavigateToShop()
                    } else {
                        onNavigateToCreateShop()
                    }
                },
                onPaymentMethodsClick = onNavigateToPaymentMethods,
                onChangePasswordClick = onNavigateToChangePassword,
                onLogoutClick = viewModel::logout
            )
        }
    }

    // Show loading indicator when loading data or logging out
    if (isShowProgressBar.value) {
        FullScreenProgressBar()
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun ProfileCard(
    user: UserDomainEntity,
    onEditClick: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        MaxWidthBox {
            // Edit Profile Button - now at top right of card
            IconButton(
                onClick = onEditClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Chỉnh sửa hồ sơ",
                    tint = colorResource(R.color.colorSystem_heading_button),
                    modifier = Modifier.size(18.dp)
                )
            }

            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Profile Image - LEFT
                GlideImage(
                    model = user.avatar.takeIf { it.isNotEmpty() }
                        ?: "https://i.pinimg.com/564x/19/b8/d6/19b8d6e9b13eef23ec9c746968bb88b1.jpg",
                    contentDescription = "Ảnh đại diện",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.FillBounds,
                    transition = MyCrossFade
                ) {
                    it.centerCrop()
                }

                // User Info - RIGHT
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // User Name
                    Text(
                        text = user.name.ifEmpty { "Tên người dùng" },
                        style = CustomTypography.TextBold,
                        fontSize = 18.sp,
                        color = colorResource(id = R.color.colorSystem_heading_button)
                    )

                    // User Phone
                    if (user.phoneNumber.isNotEmpty()) {
                        Text(
                            text = user.phoneNumber,
                            style = CustomTypography.TextRegular,
                            fontSize = 14.sp,
                            color = colorResource(id = R.color.colorSystem_normal_text)
                        )
                    }

                    // User Email
                    Text(
                        text = user.email.ifEmpty { "email@example.com" },
                        style = CustomTypography.TextRegular,
                        fontSize = 13.sp,
                        color = colorResource(id = R.color.colorSystem_greyscale_500)
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderStatisticsSection(
    totalOrders: Int,
    totalSpent: Int,
    isLoading: Boolean,
    onViewOverviewClick: () -> Unit
) {
    if (isLoading) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = colorResource(R.color.colorSystem_heading_button)
                )
            }
        }
    } else {
        // Horizontal layout with 2 cards side by side
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Total Orders Card - Primary Color
            ElevatedCard(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = colorResource(R.color.colorSystem_heading_button) // App primary color
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Icon
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Title
                    Text(
                        text = "Tổng đơn hàng",
                        style = CustomTypography.TextMedium.copy(fontSize = 13.sp),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    // Number - Reduced size
                    Text(
                        text = totalOrders.toString(),
                        style = CustomTypography.TextBold.copy(fontSize = 24.sp),
                        color = Color.White
                    )
                }
            }

            // Total Spent Card - Red
            ElevatedCard(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = Color(0xFFFF5252) // Red color
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Icon
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AttachMoney,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Title
                    Text(
                        text = "Tổng chi tiêu",
                        style = CustomTypography.TextMedium.copy(fontSize = 13.sp),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    // Amount - Reduced size
                    Text(
                        text = totalSpent.toPriceFormat(),
                        style = CustomTypography.TextBold.copy(fontSize = 20.sp),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsMenuCard(
    isSettingsExpanded: Boolean,
    hasShop: Boolean,
    onSettingsClick: () -> Unit,
    onOverviewClick: () -> Unit,
    onOrdersClick: () -> Unit,
    onShopClick: () -> Unit,
    onPaymentMethodsClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        MaxWidthColumn(
            modifier = Modifier.padding(8.dp)
        ) {
            // Thống kê chi tiêu - NEW MENU ITEM
            MenuItem(
                icon = Icons.Default.Assessment,
                title = "Thống kê chi tiêu",
                subtitle = "Xem chi tiết thống kê và phân tích chi tiêu",
                onClick = onOverviewClick
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = colorResource(id = R.color.colorSystem_text_button)
            )

            // My Orders
            MenuItem(
                icon = Icons.Outlined.ShoppingBag,
                title = "Đơn hàng của tôi",
                subtitle = "Xem trạng thái đơn hàng và lịch sử mua",
                onClick = onOrdersClick
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = colorResource(id = R.color.colorSystem_text_button)
            )

            // My Shop or Create Shop
            MenuItem(
                icon = Icons.Outlined.Store,
                title = if (hasShop) "Cửa hàng của tôi" else "Tạo cửa hàng",
                subtitle = if (hasShop) "Quản lý cửa hàng của bạn" else "Tạo cửa hàng để bán sản phẩm",
                onClick = onShopClick
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = colorResource(id = R.color.colorSystem_text_button)
            )

            // Settings (Expandable)
            val rotationAngle by animateFloatAsState(
                targetValue = if (isSettingsExpanded) 180f else 0f,
                label = "Rotation Animation"
            )

            MenuItem(
                icon = Icons.Outlined.Settings,
                title = "Cài đặt",
//                subtitle = "Phương thức thanh toán, mật khẩu, đăng xuất",
                subtitle = "Phương thức thanh toán, đăng xuất",
                onClick = onSettingsClick,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.rotate(rotationAngle),
                        tint = colorResource(id = R.color.colorSystem_heading_button)
                    )
                }
            )

            // Expandable Settings Content
            AnimatedVisibility(
                visible = isSettingsExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                MaxWidthColumn {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = colorResource(id = R.color.colorSystem_text_button)
                    )

                    // Payment Methods
                    MenuItem(
                        icon = Icons.Outlined.CreditCard,
                        title = "Phương thức thanh toán",
                        subtitle = "Quản lý các phương thức thanh toán",
                        onClick = onPaymentMethodsClick,
                        isSubItem = true
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(end = 16.dp, start = 56.dp),
                        color = colorResource(id = R.color.colorSystem_text_button)
                    )

                    // Change Password
                    MenuItem(
                        icon = Icons.Outlined.Password,
                        title = "Đổi mật khẩu",
                        subtitle = "Cập nhật mật khẩu tài khoản",
                        onClick = onChangePasswordClick,
                        isSubItem = true
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(end = 16.dp, start = 56.dp),
                        color = colorResource(id = R.color.colorSystem_text_button)
                    )

                    // Logout
                    MenuItem(
                        icon = Icons.AutoMirrored.Outlined.Logout,
                        title = "Đăng xuất",
                        subtitle = "Đăng xuất khỏi tài khoản",
                        onClick = onLogoutClick,
                        isSubItem = true,
                        textColor = colorResource(id = R.color.colorSystem_tint_red)
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    isSubItem: Boolean = false,
    textColor: Color = colorResource(id = R.color.colorSystem_heading_button),
    trailingIcon: @Composable (() -> Unit)? = {
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = colorResource(id = R.color.colorSystem_heading_button)
        )
    },
) {
    MaxWidthRow(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(
                start = if (isSubItem) 56.dp else 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!isSubItem) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = CustomTypography.TextMedium,
                fontSize = 16.sp,
                color = textColor
            )

            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = CustomTypography.TextRegular,
                    fontSize = 12.sp,
                    color = colorResource(id = R.color.colorSystem_normal_text)
                )
            }
        }

        trailingIcon?.invoke()
    }
}
