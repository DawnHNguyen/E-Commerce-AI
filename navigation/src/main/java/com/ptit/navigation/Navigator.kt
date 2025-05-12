package com.ptit.navigation

import android.content.Context
import android.content.Intent

object Navigator {
    fun navigateToMainActivity(context: Context) {
        val intent = Intent(context, Class.forName("com.ptit.core.MainActivity"))
        context.startActivity(intent)
    }

    fun navigateToAuthActivity(context: Context) {
        val intent = Intent(context, Class.forName("com.ptit.auth.AuthActivity"))
        context.startActivity(intent)
    }
}