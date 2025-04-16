package com.ptit.navigation.destination

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import kotlinx.serialization.Serializable

sealed class BottomNavigationScreen {
    @Serializable
    data object HomeScreen : BottomNavigationScreen()

    @Serializable
    data object ListScreen : BottomNavigationScreen()

    @Serializable
    data object ProfileScreen : BottomNavigationScreen()
}

data class BottomNavigationItem(
    @DrawableRes val iconRes: Int,
    @StringRes val titleRes: Int,
    val screen: BottomNavigationScreen,
)
