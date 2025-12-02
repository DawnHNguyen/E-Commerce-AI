package com.ptit.core

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.ptit.common.R
import com.ptit.common.presentation.EventManager
import com.ptit.common.presentation.component.LocalBottomNavigationVisibility
import com.ptit.common.presentation.rememberDerivedState
import com.ptit.common.presentation.rememberState
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.common.utils.safeCollectFlow
import com.ptit.core.account.AccountScreen
import com.ptit.core.account.EditProfile
import com.ptit.core.account.paymentConfig.PaymentConfigScreen
import com.ptit.core.account.paymentMethod.PaymentMethodScreen
import com.ptit.core.cart.CartDataTransfer
import com.ptit.core.cart.CartScreen
import com.ptit.core.category.CategoryScreen
import com.ptit.core.category.ProductsByCategoryScreen
import com.ptit.core.chat.ChatScreen
import com.ptit.core.chat.ChatSessionListScreen
import com.ptit.core.home.HomeScreen
import com.ptit.core.home.SearchScreen
import com.ptit.core.order.CreateOrderScreen
import com.ptit.core.order.OrderDetailScreen
import com.ptit.core.order_history.OrderHistoryScreen
import com.ptit.core.overview.OverviewScreen
import com.ptit.core.product.ProductForm
import com.ptit.core.product.ProductListScreen
import com.ptit.core.product_detail.ProductDetailScreen
import com.ptit.core.seller_request.CreateSellerRequestScreen
import com.ptit.core.shop.ShopDetailScreen
import com.ptit.core.shop.UpdateShopScreen
import com.ptit.navigation.Navigator
import com.ptit.navigation.destination.BottomNavigationItem
import com.ptit.navigation.destination.BottomNavigationScreen
import com.ptit.navigation.destination.ChatRoute
import com.ptit.navigation.destination.ChatSessionListRoute
import com.ptit.navigation.destination.ConfigPaymentMethodRoute
import com.ptit.navigation.destination.CreateOrderRoute
import com.ptit.navigation.destination.CreateSellerRequestRoute
import com.ptit.navigation.destination.EditProfileRoute
import com.ptit.navigation.destination.ListPaymentMethodRoute
import com.ptit.navigation.destination.OrderDetailRoute
import com.ptit.navigation.destination.OrderHistoryRoute
import com.ptit.navigation.destination.OverviewRoute
import com.ptit.navigation.destination.ProductDetailRoute
import com.ptit.navigation.destination.ProductFormRoute
import com.ptit.navigation.destination.ProductListRoute
import com.ptit.navigation.destination.ProductsByCategoryRoute
import com.ptit.navigation.destination.ProfileRoute
import com.ptit.navigation.destination.SearchRoute
import com.ptit.navigation.destination.ShopDetailRoute
import com.ptit.navigation.destination.UpdateShopRoute
import com.recurly.androidsdk.data.model.RecurlySessionData
import dagger.hilt.android.AndroidEntryPoint

@OptIn(ExperimentalComposeUiApi::class)
@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        window.decorView
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        RecurlySessionData.setPublicKey("fra-YEvkB0OKEp3y0bDYldcPoT")

        safeCollectFlow(EventManager.events) {
            //TODO: Handle the events bus
        }

        setContent {
            val navController = rememberNavController()
            val layoutDirection = LocalLayoutDirection.current

            CompositionLocalProvider(
                LocalBottomNavigationVisibility provides rememberState { true },
            ) {
                Scaffold(
                    bottomBar = {
                        if (LocalBottomNavigationVisibility.current.value) {
                            BottomNavigationBar(
                                navController = navController,
                            )
                        }
                    },
                    modifier = Modifier
                        .navigationBarsPadding()
                        .imePadding()
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = BottomNavigationScreen.HomeScreen,
                        modifier = Modifier
                            .semantics { testTagsAsResourceId = true }
                            .padding(
                                start = it.calculateStartPadding(layoutDirection),
                                end = it.calculateEndPadding(layoutDirection),
                                bottom = it.calculateBottomPadding()
                            ),
                        enterTransition = {
                            fadeIn(animationSpec = tween(0))
                        },
                        exitTransition = {
                            fadeOut(animationSpec = tween(0))
                        },
                        popExitTransition = {
                            fadeOut(animationSpec = tween(0))
                        },
                        popEnterTransition = {
                            fadeIn(animationSpec = tween(0))
                        }
                    ) {
                        // ... (Các composable khác: HomeScreen, SearchScreen, CartScreen...)
                        composable<BottomNavigationScreen.HomeScreen> {
                            HomeScreen(
                                navigateToSearch = {
                                    navController.navigate(SearchRoute)
                                },
                                navigateToProductDetail = { productId ->
                                    navController.navigate(ProductDetailRoute(productId = productId))
                                }
                            )
                        }

                        composable<SearchRoute> {
                            SearchScreen(
                                navigateBack = navController::navigateUp,
                                navigateToProductDetail = { productId ->
                                    navController.navigate(ProductDetailRoute(productId = productId))
                                },
                            )
                        }

                        composable<BottomNavigationScreen.CategoryScreen> {
                            CategoryScreen(navController = navController)
                        }

                        composable<ProductsByCategoryRoute> { backStackEntry ->
                            val args = backStackEntry.toRoute<ProductsByCategoryRoute>()
                            ProductsByCategoryScreen(
                                categoryId = args.categoryId,
                                categoryDisplayName = args.categoryDisplayName,
                                onBack = navController::navigateUp,
                                onProductClick = { productId: String ->
                                    navController.navigate(ProductDetailRoute(productId = productId))
                                }
                            )
                        }

//                        composable<BottomNavigationScreen.CategoryScreen> { // Thêm composable cho CategoryScreen
//                            CategoryScreen(navController = navController)
//                        }

                        // Chat Session List (Bottom Nav destination)
                        navigation<BottomNavigationScreen.ChatScreen>(
                            startDestination = ChatSessionListRoute,
                        ) {
                            composable<ChatSessionListRoute> {
                                ChatSessionListScreen(
                                    onNavigateToChat = { sessionId ->
                                        navController.navigate(ChatRoute(sessionId = sessionId))
                                    }
                                )
                            }

                            composable<ChatRoute> { backStackEntry ->
                                val args = backStackEntry.toRoute<ChatRoute>()
                                ChatScreen(
                                    sessionId = args.sessionId ?: "",
                                    onNavigateBack = navController::navigateUp,
                                    onNavigateToAddPayment = {
                                        navController.navigate(ConfigPaymentMethodRoute)
                                    },
                                    onNavigateToOrderDetail = { orderId ->
                                        navController.navigate(OrderDetailRoute(orderId = orderId))
                                    }
                                )
                            }
                        }

//                        composable<ProductsByCategoryRoute> { backStackEntry -> // Thêm route này
//                            val args = backStackEntry.toRoute<ProductsByCategoryRoute>()
//                            ProductsByCategoryScreen(
//                                navController = navController,
//                                navigateToProductDetail = { productId ->
//                                    navController.navigate(ProductDetailRoute(productId = productId))
//                                }
//                            )
//                        }

                        composable<BottomNavigationScreen.CartScreen> {
                            CartScreen(
                                onBack = navController::navigateUp,
                                onCheckout = { selectedItemIds, groupedItems ->
                                    CartDataTransfer.groupedItems = groupedItems
                                    navController.navigate(CreateOrderRoute(selectedItemIds = selectedItemIds))
                                },
                                onProductClick = { productId ->
                                    navController.navigate(ProductDetailRoute(productId = productId))
                                }
                            )
                        }

                        // 🔴 SỬA composable<CreateOrderRoute>
                        composable<CreateOrderRoute> { backStackEntry ->
                            val args = backStackEntry.toRoute<CreateOrderRoute>()
                            val groupedItems = CartDataTransfer.groupedItems ?: emptyList()

                            CreateOrderScreen(
                                selectedItemIds = args.selectedItemIds,
                                groupedCartItems = groupedItems,
                                onBack = navController::navigateUp,
                                onOrderCreated = { orderId ->
                                    CartDataTransfer.clear()
                                    // Sửa lại điều hướng sang OrderDetail theo logic cũ của bạn
                                    navController.navigate(OrderDetailRoute(orderId = orderId)) {
                                        popUpTo(BottomNavigationScreen.CartScreen) { inclusive = true }
                                    }
                                }
                            )
                        }

                        // (OrderHistoryRoute giữ nguyên)
                        composable<OrderHistoryRoute> {
                            OrderHistoryScreen(
                                onBack = navController::navigateUp,
                                onNavigateToDetail = { orderId ->
                                    navController.navigate(OrderDetailRoute(orderId = orderId))
                                }
                            )
                        }

                        // ✅ NEW: Overview Screen
                        composable<OverviewRoute> {
                            OverviewScreen(
                                onBack = navController::navigateUp,
                                onViewAllOrders = {
                                    navController.navigate(OrderHistoryRoute)
                                },
                                onOrderClick = { orderId ->
                                    navController.navigate(OrderDetailRoute(orderId = orderId))
                                }
                            )
                        }

                        // 🔴 SỬA composable<OrderDetailRoute>
                        composable<OrderDetailRoute> { backStackEntry ->
                            val args = backStackEntry.toRoute<OrderDetailRoute>()
                            OrderDetailScreen(
                                orderId = args.orderId,
                                onBack = navController::navigateUp,
                                // 🔴 SỬA: Bỏ comment dòng này để hết lỗi biên dịch
                                navigateToPaymentMethod = {
                                    navController.navigate(ListPaymentMethodRoute)
                                },
                                backToCart = {
                                    navController.popBackStack(BottomNavigationScreen.CartScreen, false)
                                }
                            )
                        }

                        // 🔴 SỬA navigation<BottomNavigationScreen.ProfileScreen>
                        navigation<BottomNavigationScreen.ProfileScreen>(
                            startDestination = ProfileRoute,
                        ) {
                            composable<ProfileRoute> {
                                val accountGraphBackStackEntry = remember(it) {
                                    navController.getBackStackEntry(BottomNavigationScreen.ProfileScreen)
                                }
                                AccountScreen(
                                    backStackEntry = accountGraphBackStackEntry,
                                    onLogoutSuccess = {
                                        Navigator.navigateToAuthActivity(this@MainActivity)
                                    },
                                    // 🔴 SỬA: Điều hướng đến OrderHistoryRoute
                                    onNavigateToOrders = {
                                        navController.navigate(OrderHistoryRoute)
                                    },
                                    // ✅ NEW: Navigate to Overview
                                    onNavigateToOverview = {
                                        navController.navigate(OverviewRoute)
                                    },
                                    onNavigateToEditProfile = {
                                        navController.navigate(EditProfileRoute)
                                    },
                                    onNavigateToChangePassword = {
                                        // TODO: Implement navigation to Change Password
                                    },
                                    onNavigateToPaymentMethods = {
                                        navController.navigate(ListPaymentMethodRoute)
                                    },
                                    onNavigateToShop = {
                                        navController.navigate(ShopDetailRoute)
                                    },
                                    onNavigateToCreateShop = {
                                        navController.navigate(CreateSellerRequestRoute)
                                    },
                                )
                            }
                            // ... (Các composable con của Profile giữ nguyên)
                            composable<EditProfileRoute> {
                                val accountGraphBackStackEntry = remember(it) {
                                    navController.getBackStackEntry(BottomNavigationScreen.ProfileScreen)
                                }
                                EditProfile(
                                    backStackEntry = accountGraphBackStackEntry,
                                    onBack = navController::navigateUp,
                                )
                            }
                        }

                        // ... (Các composable khác giữ nguyên)
                        composable<ListPaymentMethodRoute> {
                            PaymentMethodScreen(
                                onNavigateBack = navController::navigateUp,
                                onNavigateToAddPaymentMethod = {
                                    navController.navigate(ConfigPaymentMethodRoute)
                                },
                            )
                        }

                        composable<CreateSellerRequestRoute> {
                            CreateSellerRequestScreen(
                                onNavigateBack = navController::navigateUp,
                                onRequestSubmitted = {
                                    // Navigate back to account screen
                                    navController.navigateUp()
                                }
                            )
                        }

                        composable<UpdateShopRoute> {
                            UpdateShopScreen(
                                onNavigateBack = navController::navigateUp
                            )
                        }

                        composable<ConfigPaymentMethodRoute> {
                            PaymentConfigScreen(
                                onNavigateBack = navController::navigateUp,
                                onPaymentMethodSaved = navController::navigateUp,
                            )
                        }

                        composable<ShopDetailRoute> {
                            ShopDetailScreen(
                                onNavigateBack = navController::navigateUp,
                                onNavigateToEditShop = {
                                    navController.navigate(UpdateShopRoute)
                                },
                                onNavigateToProductList = {
                                    navController.navigate(ProductListRoute)
                                }
                            )
                        }

                        composable<ProductListRoute> {
                            ProductListScreen(
                                onNavigateBack = navController::navigateUp,
                                onNavigateToProductForm = { productId ->
                                    navController.navigate(ProductFormRoute(productId))
                                },
                                onNavigateToProductDetail = { productId ->
                                    navController.navigate(ProductDetailRoute(productId))
                                },
                            )
                        }

                        composable<ProductFormRoute> { backStackEntry ->
                            val args = backStackEntry.toRoute<ProductFormRoute>()
                            val productId = args.productId
                            ProductForm(
                                onNavigateBack = navController::navigateUp,
                                productId = productId,
                            )
                        }

                        composable<ProductDetailRoute> { backStackEntry ->
                            val args = backStackEntry.toRoute<ProductDetailRoute>()
                            val productId = args.productId
                            ProductDetailScreen(
                                productId = productId,
                                onBackClick = navController::navigateUp,
                                onCartClick = {
                                    navController.navigate(BottomNavigationScreen.CartScreen) {
                                        popUpTo(navController.graph.startDestinationId)
                                        launchSingleTop = true
                                    }
                                },
                                onAddToCartClick = {
                                    // Logic handled in ViewModel
                                },
                                onProductItemClick = { similarProductId ->
                                    navController.navigate(ProductDetailRoute(productId = similarProductId)) {
                                        launchSingleTop = true
                                        popUpTo(BottomNavigationScreen.HomeScreen) // Optional: pop back to home to avoid deep stack
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun BottomNavigationBar(
        navController: NavController,
    ) {
        val bottomNavigationItems = remember {
            listOf(
                BottomNavigationItem(
                    icon = Icons.Outlined.Home,
                    title = "Trang chủ",
                    screen = BottomNavigationScreen.HomeScreen
                ),
                BottomNavigationItem( // Thêm mục Danh mục
                    icon = Icons.Outlined.Apps, // Sử dụng icon Apps hoặc một icon Category phù hợp
                    title = "Danh mục",
                    screen = BottomNavigationScreen.CategoryScreen
                ),
                BottomNavigationItem(
                    icon = Icons.Outlined.SmartToy,
                    title = "Trợ lý",
                    screen = BottomNavigationScreen.ChatScreen
                ),
                BottomNavigationItem(
                    icon = Icons.Outlined.ShoppingCart,
                    title = "Giỏ hàng",
                    screen = BottomNavigationScreen.CartScreen
                ),
                BottomNavigationItem(
                    icon = Icons.Outlined.Person,
                    title = "Tôi",
                    screen = BottomNavigationScreen.ProfileScreen
                ),
            )
        }

        CompositionLocalProvider(
            LocalRippleConfiguration provides null
        ) {
            NavigationBar(
                containerColor = Color.White,
                contentColor = Color.Transparent,
            ) {
                val navBackStackEntry = navController.currentBackStackEntryAsState()
                val currentDestination = rememberDerivedState {
                    navBackStackEntry.value?.destination
                }

                bottomNavigationItems.forEach { item ->
                    val isSelected =
                        rememberDerivedState {
                            currentDestination.value?.hierarchy?.any { it.hasRoute(item.screen::class) } == true
                        }
                    NavigationBarItem(
                        selected = isSelected.value,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {
                            navController.navigate(item.screen) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = colorResource(id = R.color.colorSystem_heading_button), // Thay đổi màu selected
                            unselectedIconColor = colorResource(id = R.color.colorSystem_greyscale_500), // Màu xám cho unselected
                            selectedTextColor = colorResource(id = R.color.colorSystem_heading_button), // Màu text selected
                            unselectedTextColor = colorResource(id = R.color.colorSystem_greyscale_600), // Màu text unselected
                            indicatorColor = colorResource(R.color.colorSystem_text_field) // Màu nền khi selected
                        ),
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                style = CustomTypography.TextSemiBold,
                                fontSize = 12.sp
                            )
                        },
                    )
                }
            }
        }
    }
}