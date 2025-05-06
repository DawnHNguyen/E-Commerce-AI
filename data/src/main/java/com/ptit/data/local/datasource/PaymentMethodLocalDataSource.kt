package com.ptit.data.local.datasource

import app.cash.sqldelight.coroutines.asFlow
import com.ptit.database.PtitEcomDatabase
import com.ptit.domain.utils.Resource
import com.ptit.domain.utils.UnknownException
import comptitdatabase.TblPaymentMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PaymentMethodLocalDataSource @Inject constructor(
    private val database: PtitEcomDatabase,
) {
    suspend fun getDefaultPaymentMethod(): TblPaymentMethod? = withContext(Dispatchers.IO) {
        database.ecomDatabaseQueries.getDefaultPaymentMethod().executeAsOneOrNull()
    }
    suspend fun insertPaymentMethod(
        firstSixNum: String,
        lastFourNum: String,
        cardType: com.recurly.androidsdk.data.model.CreditCardsParameters,
        isDefault: Boolean,
    ): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            // If this is the default card, reset all other cards first
            if (isDefault) {
                database.ecomDatabaseQueries.setDefaultPaymentMethod()
            }

            database.ecomDatabaseQueries.insertPaymentMethod(
                firstSixNum = firstSixNum,
                lastFourNum = lastFourNum,
                cardType = cardType,
                isDefault = if (isDefault) 1L else 0L
            )
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.error(
                error = UnknownException(
                    message = e.message,
                    requestUrl = "",
                    error = null
                )
            )
        }
    }

    fun getAllPaymentMethods(): Flow<List<TblPaymentMethod>> {
        return database.ecomDatabaseQueries
            .getAllPaymentMethods()
            .asFlow()
            .map { it.executeAsList() }
            .flowOn(Dispatchers.IO)
    }

    suspend fun deletePaymentMethod(firstSixNum: String, lastFourNum: String): Resource<Unit> =
        withContext(Dispatchers.IO) {
            try {
                database.ecomDatabaseQueries.deletePaymentMethod(
                    firstSixNum = firstSixNum,
                    lastFourNum = lastFourNum
                )
                Resource.Success(Unit)
            } catch (e: Exception) {
                Resource.error(
                    error = UnknownException(
                        message = e.message,
                        requestUrl = "",
                        error = null
                    )
                )
            }
        }

    suspend fun setDefaultPaymentMethod(firstSixNum: String, lastFourNum: String): Resource<Unit> =
        withContext(Dispatchers.IO) {
            try {
                database.ecomDatabaseQueries.transaction {
                    // First, remove default flag from all cards
                    database.ecomDatabaseQueries.setDefaultPaymentMethod()

                    // Set the selected card as default
                    database.ecomDatabaseQueries.updatePaymentMethodAsDefault(
                        firstSixNum = firstSixNum,
                        lastFourNum = lastFourNum
                    )
                }
                Resource.Success(Unit)
            } catch (e: Exception) {
                Resource.error(
                    error = UnknownException(
                        message = e.message,
                        requestUrl = "",
                        error = null
                    )
                )
            }
        }
}
