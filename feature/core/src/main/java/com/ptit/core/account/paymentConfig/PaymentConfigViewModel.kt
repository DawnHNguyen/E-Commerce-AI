package com.ptit.core.account.paymentConfig

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ptit.domain.entity.payment.PaymentMethodDomainEntity
import com.ptit.domain.repository.PaymentMethodRepository
import com.ptit.domain.utils.Resource
import com.ptit.domain.utils.UnknownException
import com.recurly.androidsdk.data.model.CreditCardsParameters
import com.recurly.androidsdk.data.network.TokenService
import com.recurly.androidsdk.domain.RecurlyInputValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PaymentConfigViewModel @Inject constructor(
    private val paymentMethodRepository: PaymentMethodRepository,
) : ViewModel() {
    private val recurlyApi by lazy { TokenService() }

    private val _uiState = MutableStateFlow(PaymentConfigUiState())
    val uiState: StateFlow<PaymentConfigUiState> = _uiState.asStateFlow()

    private val _saveCardState = MutableStateFlow<Resource<Unit>>(Resource.idle())
    val saveCardState: StateFlow<Resource<Unit>> = _saveCardState

    // Update and validate card number
    fun updateCardNumber(number: String) {
        val (isValid, cardType, formattedNumber) = RecurlyInputValidator.validateCreditCardNumber(number)
        _uiState.update { state ->
            state.copy(
                cardNumber = number,
                cardType = cardType,
                isCardNumberValid = isValid && RecurlyInputValidator.verifyCardNumber(formattedNumber, cardType),
                isCvvValid = if (state.cvv.isNotEmpty())
                    RecurlyInputValidator.verifyCVV(state.cvv, cardType)
                else
                    state.isCvvValid
            )
        }
    }

    // Update and validate expiration date
    fun updateExpirationDate(date: String) {
        val previousValue = uiState.value.expirationDate
        val (isValid, formattedDate) = RecurlyInputValidator.validateExpirationDate(date, previousValue)
        _uiState.update { state ->
            state.copy(
                expirationDate = date,
                isExpirationDateValid = isValid && RecurlyInputValidator.verifyDate(formattedDate)
            )
        }
    }

    // Update and validate CVV
    fun updateCvv(cvv: String) {
        val sanitizedCvv = RecurlyInputValidator.regexSpecialCharacters(cvv, "0-9")
        _uiState.update { state ->
            state.copy(
                cvv = sanitizedCvv,
                isCvvValid = RecurlyInputValidator.verifyCVV(sanitizedCvv, state.cardType)
            )
        }
    }

    // Update default card setting
    fun updateUseAsDefaultCard(useAsDefault: Boolean) {
        _uiState.update { it.copy(useAsDefaultCard = useAsDefault) }
    }

    fun tokenizeAndSaveCard() {
        if (!uiState.value.isEnableSaveButton) return

        _saveCardState.value = Resource.loading()

        val state = uiState.value

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val request = createTokenizationRequest(state)
                val tokenResponse = recurlyApi.getToken(request)

                // Extract first six and last four digits from card number
                val cardNumber = state.cardNumber.replace(" ", "")
                val firstSixNum = if (cardNumber.length >= 6) cardNumber.substring(0, 6) else cardNumber
                val lastFourNum = if (cardNumber.length >= 4)
                    cardNumber.substring(cardNumber.length - 4)
                else
                    cardNumber

                // Create payment method entity and save
                val paymentMethod = PaymentMethodDomainEntity(
                    firstSixNum = firstSixNum,
                    lastFourNum = lastFourNum,
                    cardType = getCreditCardType(state.cardType),
                    token = tokenResponse.token ?: "",
                    isDefault = state.useAsDefaultCard
                )

                val result = paymentMethodRepository.savePaymentMethod(paymentMethod)
                _saveCardState.value = result

            } catch (e: Exception) {
                _saveCardState.value = Resource.error(
                    error = UnknownException(
                        message = e.message,
                        requestUrl = "",
                        error = null
                    )
                )
            }
        }
    }

    private fun createTokenizationRequest(state: PaymentConfigUiState): com.recurly.androidsdk.data.model.tokenization.TokenizationRequest {
        return com.recurly.androidsdk.data.model.tokenization.TokenizationRequest(
            firstName = "John", // These would ideally come from a profile screen
            lastName = "Doe",
            company = "",
            addressOne = "123 Main St",
            addressTwo = "",
            city = "LA",
            state = "CA",
            postalCode = "12345",
            country = "US",
            phone = "",
            vatNumber = "",
            taxIdentifier = "",
            taxIdentifierType = "",

            // Card details from UI
            cardNumber = state.cardNumber.replace(" ", "").toLong(),
            expirationMonth = state.expirationDate.substring(0, 2).toInt(),
            expirationYear = state.expirationDate.substring(2).toInt(),
            cvvCode = state.cvv.toInt(),

            // Session data
            sdkVersion = com.recurly.androidsdk.data.model.RecurlySessionData.versionName,
            publicKey = "fra-YEvkB0OKEp3y0bDYldcPoT", // Should come from configuration
            deviceId = com.recurly.androidsdk.data.model.RecurlySessionData.deviceId,
            sessionId = UUID.randomUUID().toString()
        )
    }

    private fun getCreditCardType(cardType: String): CreditCardsParameters {
        return try {
            when (cardType.uppercase()) {
                "VISA" -> CreditCardsParameters.VISA
                "MASTER", "MASTERCARD" -> CreditCardsParameters.MASTER
                "AMERICANEXPRESS", "AMERICAN_EXPRESS" -> CreditCardsParameters.AMERICAN_EXPRESS
                "DISCOVER" -> CreditCardsParameters.DISCOVER
                "JCB" -> CreditCardsParameters.JCB
                "DINERSCLUB", "DINERS_CLUB" -> CreditCardsParameters.DINERS_CLUB
                "UNION_PAY" -> CreditCardsParameters.UNION_PAY
                "ELO" -> CreditCardsParameters.ELO
                "HIPERCARD" -> CreditCardsParameters.HIPERCARD
                "TARJETA_NARANJA" -> CreditCardsParameters.TARJETA_NARANJA
                else -> CreditCardsParameters.VISA // Default to VISA on error
            }
        } catch (e: Exception) {
            CreditCardsParameters.VISA // Default to VISA on error
        }
    }
}
