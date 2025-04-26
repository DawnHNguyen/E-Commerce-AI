package com.ptit.data.di

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.ptit.data.local.enumCreditCardTypeAdapter
import com.ptit.database.PtitEcomDatabase
import comptitdatabase.TblPaymentMethod
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalModule {
    @Singleton
    @Provides
    fun provideVibesWidgetDatabase(@ApplicationContext context: Context): PtitEcomDatabase {
        val driver = AndroidSqliteDriver(PtitEcomDatabase.Schema, context, "ptitecom.db")
        return PtitEcomDatabase(
            driver = driver,
            tblPaymentMethodAdapter = TblPaymentMethod.Adapter(
                cardTypeAdapter = enumCreditCardTypeAdapter
            ),
        )
    }
}