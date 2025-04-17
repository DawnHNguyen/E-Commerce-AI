package com.ptit.core

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ptit.common.presentation.EventManager
import com.ptit.common.presentation.MaxSizeBox
import com.ptit.common.presentation.component.LocalBottomNavigationVisibility
import com.ptit.common.presentation.rememberState
import com.ptit.common.utils.safeCollectFlow
import com.ptit.navigation.destination.BottomNavigationScreen
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
//                            BottomNavigationBar(
//                                navController = navController,
//                            )
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
                            MaxSizeBox(
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Home Screen",
                                )
                            }
                        }
                    }
                }
            }
        }
    }

//    @OptIn(ExperimentalMaterial3Api::class)
//    @Composable
//    private fun BottomNavigationBar(
//        navController: NavController,
//    ) {
//        val bottomNavigationItems = remember {
//            listOf(
//                BottomNavigationItem(
//                    index = 0,
//                    screen = BottomNavigationScreen.FirstScreen
//                ),
//                BottomNavigationItem(
//                    index = 1,
//                    screen = BottomNavigationScreen.SecondScreen
//                ),
//                BottomNavigationItem(
//                    index = 2,
//                    screen = BottomNavigationScreen.ThirdScreen
//                ),
//                BottomNavigationItem(
//                    index = 3,
//                    screen = BottomNavigationScreen.FourthScreen
//                ),
//            )
//        }
//
//        CompositionLocalProvider(
//            LocalRippleConfiguration provides null
//        ) {
//            NavigationBar(
//                containerColor = Color.White,
//                contentColor = Color.Transparent,
//            ) {
//                val navBackStackEntry = navController.currentBackStackEntryAsState()
//                val currentDestination = navBackStackEntry.value?.destination
//
//                bottomNavigationItems.forEach { item ->
//                    val isSelected =
//                        currentDestination?.hierarchy?.any { it.hasRoute(item.screen::class) } == true
//                    NavigationBarItem(
//                        selected = isSelected,
//                        interactionSource = remember { MutableInteractionSource() },
//                        onClick = {
//                            navController.navigate(item.screen) {
//                                popUpTo(navController.graph.startDestinationId)
//                                launchSingleTop = true
//                            }
//                        },
////                        colors = NavigationBarItemDefaults.colors(
////                            selectedIconColor = colorResource(id = CommonR.color.colorSystem_primary_50),
////                            unselectedIconColor = colorResource(id = R.color.bottomNav_defaultIcon),
////                            selectedTextColor = colorResource(id = CommonR.color.colorSystem_primary_50),
////                            unselectedTextColor = colorResource(id = R.color.bottomNav_defaultIcon),
////                            indicatorColor = Color.White
////                        ),
//                        icon = {
//                            Icon(
//                                imageVector = when (item.index) {
//                                    0 -> Icons.Filled.Home
//                                    1 -> Icons.Filled.Favorite
//                                    2 -> Icons.Filled.Person
//                                    else -> Icons.Filled.Settings
//                                },
//                                contentDescription = null
//                            )
//                        },
//                        modifier = Modifier.semantics {
//                            contentDescription = item.screen.toString()
//                        },
//                    )
//                }
//            }
//        }
//    }
}