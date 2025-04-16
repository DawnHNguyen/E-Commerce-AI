package com.ptit.navigation

import android.content.Context
import android.content.Intent

object Navigator {
    fun navigateToMainActivity(context: Context) {
        val intent = Intent(context, Class.forName("com.ptit.core.MainActivity"))
        context.startActivity(intent)
    }
}