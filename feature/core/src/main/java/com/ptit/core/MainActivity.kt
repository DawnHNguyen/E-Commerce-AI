package com.ptit.core

import android.os.Bundle
import androidx.activity.ComponentActivity
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
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
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
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.ptit.common.R
import com.ptit.common.presentation.EventManager
import com.ptit.common.presentation.component.LocalBottomNavigationVisibility
import com.ptit.common.presentation.rememberDerivedState
import com.ptit.common.presentation.rememberState
import com.ptit.common.presentation.theme.CustomTypography
import com.ptit.common.utils.safeCollectFlow
import com.ptit.core.account.AccountScreen
import com.ptit.core.cart.CartScreen
import com.ptit.core.home.HomeScreen
import com.ptit.core.product_detail.ProductDetailScreen
import com.ptit.navigation.destination.BottomNavigationItem
import com.ptit.navigation.destination.BottomNavigationScreen
import com.ptit.navigation.destination.ProductDetailRoute
import dagger.hilt.android.AndroidEntryPoint

@OptIn(ExperimentalComposeUiApi::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        window.decorView
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        safeCollectFlow(EventManager.events) {
            //TODO: Handle the events bus
        }

        setContent {
            val navController = rememberNavController()
            val layoutDirection = LocalLayoutDirection.current

            // A surface container using the 'background' color from the theme
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
                        composable<BottomNavigationScreen.HomeScreen> {
                            HomeScreen(
                                navigateToCart = {

                                },
                                navigateToSearch = {

                                },
                                navigateToProductDetail = { productId ->
                                    navController.navigate(ProductDetailRoute(productId = productId))
                                }
                            )
                        }

                        composable<BottomNavigationScreen.CartScreen> {
                            CartScreen()
                        }

                        composable<BottomNavigationScreen.ProfileScreen> {
                            AccountScreen()
                        }

                        // In MainActivity.kt, update the ProductDetailScreen composable
                        composable<ProductDetailRoute> { backStackEntry ->
                            val args = backStackEntry.toRoute<ProductDetailRoute>()
                            val productId = args.productId
                            ProductDetailScreen(
                                productId = productId,
                                onBackClick = navController::navigateUp,
                                onCartClick = {

                                },
                                onAddToCartClick = {

                                },
                                onBuyNowClick = {

                                },
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
                            selectedIconColor = colorResource(id = R.color.colorSystem_greyscale_0_white),
                            unselectedIconColor = colorResource(id = R.color.colorSystem_heading_button),
                            selectedTextColor = colorResource(id = R.color.colorSystem_heading_button),
                            unselectedTextColor = colorResource(id = R.color.colorSystem_heading_button),
                            indicatorColor = colorResource(R.color.colorSystem_normal_button)
                        ),
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null
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