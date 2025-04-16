package com.ptit.ecommerce

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.ptit.core.MainActivity
import com.ptit.auth.AuthActivity
import com.tencent.mmkv.MMKV
import dagger.hilt.android.AndroidEntryPoint
import com.ptit.common.const.SecureStorageKey

@AndroidEntryPoint
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        window.decorView
        super.onCreate(savedInstanceState)
        installSplashScreen().setKeepOnScreenCondition { true }

        val mmkv = MMKV.defaultMMKV()

        val intent = if (mmkv.containsKey(SecureStorageKey.ACCESS_TOKEN)) {
            Intent(this, MainActivity::class.java)
        }
        else {
            Intent(this, AuthActivity::class.java)
        }

        startActivity(intent)
        finish()
        return
    }
}