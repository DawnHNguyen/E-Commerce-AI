package com.ptit.navigation.destination

import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

sealed class BottomNavigationScreen {
    @Serializable
    data object HomeScreen : BottomNavigationScreen()

    @Serializable
    data object CategoryScreen : BottomNavigationScreen()

    @Serializable
    data object ChatScreen : BottomNavigationScreen()

    @Serializable
    data object CartScreen : BottomNavigationScreen()

    @Serializable
    data object ProfileScreen : BottomNavigationScreen()
}

data class BottomNavigationItem(
    val icon: ImageVector,
    val title: String,
    val screen: BottomNavigationScreen,
)
