package com.ptit.data.repository

import com.ptit.common.const.SecureStorageKey
import com.ptit.data.local.datasource.PaymentMethodLocalDataSource
import com.ptit.domain.entity.payment.PaymentMethodDomainEntity
import com.ptit.domain.repository.PaymentMethodRepository
import com.ptit.domain.utils.Resource
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PaymentMethodRepositoryImpl @Inject constructor(
    private val localDataSource: PaymentMethodLocalDataSource
) : PaymentMethodRepository {

    override suspend fun savePaymentMethod(paymentMethod: PaymentMethodDomainEntity): Resource<Unit> {
        // Save token to MMKV
        val mmkv = MMKV.defaultMMKV()
        val key = buildTokenKey(paymentMethod.firstSixNum, paymentMethod.lastFourNum)
        mmkv.putString(key, paymentMethod.token)

        // Save card info to database
        return localDataSource.insertPaymentMethod(
            firstSixNum = paymentMethod.firstSixNum,
            lastFourNum = paymentMethod.lastFourNum,
            cardType = paymentMethod.cardType,
            isDefault = paymentMethod.isDefault
        )
    }

    override fun getPaymentMethods(): Flow<List<PaymentMethodDomainEntity>> {
        return localDataSource.getAllPaymentMethods().map { paymentMethods ->
            paymentMethods.map { paymentMethod ->
                PaymentMethodDomainEntity(
                    firstSixNum = paymentMethod.firstSixNum,
                    lastFourNum = paymentMethod.lastFourNum,
                    cardType = paymentMethod.cardType,
                    token = "", // Don't load tokens here for security
                    isDefault = paymentMethod.isDefault == 1L
                )
            }
        }
    }
    
    override suspend fun getDefaultPaymentMethod(): PaymentMethodDomainEntity? {
        return localDataSource.getDefaultPaymentMethod()?.let { paymentMethod ->
            PaymentMethodDomainEntity(
                firstSixNum = paymentMethod.firstSixNum,
                lastFourNum = paymentMethod.lastFourNum,
                cardType = paymentMethod.cardType,
                token = "", // Don't load token here for security
                isDefault = true
            )
        }
    }

    override suspend fun deletePaymentMethod(firstSixNum: String, lastFourNum: String): Resource<Unit> {
        // Remove token from MMKV
        val mmkv = MMKV.defaultMMKV()
        val key = buildTokenKey(firstSixNum, lastFourNum)
        mmkv.remove(key)

        // Remove from database
        return localDataSource.deletePaymentMethod(firstSixNum, lastFourNum)
    }

    override suspend fun getPaymentMethodToken(firstSixNum: String, lastFourNum: String): String? {
        val mmkv = MMKV.defaultMMKV()
        val key = buildTokenKey(firstSixNum, lastFourNum)
        return mmkv.getString(key, null)
    }

    override suspend fun setDefaultPaymentMethod(firstSixNum: String, lastFourNum: String): Resource<Unit> {
        return localDataSource.setDefaultPaymentMethod(firstSixNum, lastFourNum)
    }

    private fun buildTokenKey(firstSixNum: String, lastFourNum: String): String {
        return "${SecureStorageKey.PAYMENT_METHOD_TOKEN_PREFIX}${firstSixNum}_${lastFourNum}"
    }
}
