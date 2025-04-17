package com.ptit.ecommerce

import android.app.Application
import com.tencent.mmkv.MMKV
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ECommerceApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        MMKV.initialize(this)
    }
}