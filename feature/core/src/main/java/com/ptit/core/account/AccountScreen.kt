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
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material3.CardDefaults
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

@Composable
fun AccountScreen(
    backStackEntry: NavBackStackEntry,
    onNavigateToOrders: () -> Unit,
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
                onEditClick = onNavigateToEditProfile
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Menu Items
            SettingsMenuCard(
                isSettingsExpanded = uiModel.value.isSettingsExpanded,
                hasShop = uiModel.value.user.shop.name.isNotEmpty(),
                onSettingsClick = viewModel::toggleSettingsExpanded,
                onOrdersClick = onNavigateToOrders,
                onShopClick = {
                    if (uiModel.value.user.shop.name.isNotEmpty()) {
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

            MaxWidthColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                // Profile Image
                GlideImage(
                    model = user.avatar.takeIf { it.isNotEmpty() }
                        ?: "https://i.pinimg.com/564x/19/b8/d6/19b8d6e9b13eef23ec9c746968bb88b1.jpg",
                    contentDescription = "Ảnh đại diện",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.FillBounds,
                    transition = MyCrossFade
                ) {
                    it.centerCrop()
                }

                Spacer(modifier = Modifier.height(16.dp))

                // User Name
                Text(
                    text = user.name.ifEmpty { "Tên người dùng" },
                    style = CustomTypography.TextBold,
                    fontSize = 20.sp,
                    color = colorResource(id = R.color.colorSystem_heading_button)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // User Email
                Text(
                    text = user.email.ifEmpty { "email@example.com" },
                    style = CustomTypography.TextRegular,
                    fontSize = 14.sp,
                    color = colorResource(id = R.color.colorSystem_normal_text)
                )

                // User Phone
                if (user.phone.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = user.phone,
                        style = CustomTypography.TextRegular,
                        fontSize = 14.sp,
                        color = colorResource(id = R.color.colorSystem_normal_text)
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
            // My Orders
//            MenuItem(
//                icon = Icons.Outlined.ShoppingBag,
//                title = "Đơn hàng của tôi",
//                subtitle = "Xem trạng thái đơn hàng và lịch sử mua",
//                onClick = onOrdersClick
//            )
//
//            HorizontalDivider(
//                modifier = Modifier.padding(horizontal = 16.dp),
//                color = colorResource(id = R.color.colorSystem_text_button)
//            )

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

//                    // Change Password
//                    MenuItem(
//                        icon = Icons.Outlined.Password,
//                        title = "Đổi mật khẩu",
//                        subtitle = "Cập nhật mật khẩu tài khoản",
//                        onClick = onChangePasswordClick,
//                        isSubItem = true
//                    )
//
//                    HorizontalDivider(
//                        modifier = Modifier.padding(end = 16.dp, start = 56.dp),
//                        color = colorResource(id = R.color.colorSystem_text_button)
//                    )

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
