package com.ptit.core.account

import android.provider.Settings
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recurly.androidsdk.data.model.RecurlySessionData
import com.recurly.androidsdk.data.model.tokenization.TokenizationRequest
import com.recurly.androidsdk.data.model.tokenization.TokenizationResponse
import com.recurly.androidsdk.data.network.TokenService
import com.recurly.androidsdk.domain.RecurlyInputValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class CreditCardViewModel : ViewModel() {
    private val recurlyApi by lazy { TokenService() }

    // State for the credit card form
    private val _cardNumberState = MutableStateFlow("")
    val cardNumberState: StateFlow<String> = _cardNumberState

    private val _expirationDateState = MutableStateFlow("")
    val expirationDateState: StateFlow<String> = _expirationDateState

    private val _cvvState = MutableStateFlow("")
    val cvvState: StateFlow<String> = _cvvState

    private val _cardTypeState = MutableStateFlow("")
    val cardTypeState: StateFlow<String> = _cardTypeState

    private val _isCardNumberValidState = MutableStateFlow(true)
    val isCardNumberValidState: StateFlow<Boolean> = _isCardNumberValidState

    private val _isExpirationDateValidState = MutableStateFlow(true)
    val isExpirationDateValidState: StateFlow<Boolean> = _isExpirationDateValidState

    private val _isCvvValidState = MutableStateFlow(true)
    val isCvvValidState: StateFlow<Boolean> = _isCvvValidState

    // Update and validate card number
    fun updateCardNumber(number: String) {
        val (isValid, cardType, formattedNumber) = RecurlyInputValidator.validateCreditCardNumber(number)
        _cardNumberState.value = number
        _cardTypeState.value = cardType
        _isCardNumberValidState.value = isValid && RecurlyInputValidator.verifyCardNumber(formattedNumber, cardType)

        // Update CVV validation if card type changed
        if (_cvvState.value.isNotEmpty()) {
            _isCvvValidState.value = RecurlyInputValidator.verifyCVV(_cvvState.value, cardType)
        }
    }

    // Update and validate expiration date
    fun updateExpirationDate(date: String) {
        val previousValue = _expirationDateState.value
        val (isValid, formattedDate) = RecurlyInputValidator.validateExpirationDate(date, previousValue)
        _expirationDateState.value = date
        _isExpirationDateValidState.value = isValid && RecurlyInputValidator.verifyDate(formattedDate)
    }

    // Update and validate CVV
    fun updateCvv(cvv: String) {
        val sanitizedCvv = RecurlyInputValidator.regexSpecialCharacters(cvv, "0-9")
        _cvvState.value = sanitizedCvv
        _isCvvValidState.value = RecurlyInputValidator.verifyCVV(sanitizedCvv, _cardTypeState.value)
    }

    // Check if the entire form is valid
    val isFormValid: StateFlow<Boolean> = combine(
        _isCardNumberValidState,
        _isExpirationDateValidState,
        _isCvvValidState
    ) { isCardValid, isExpirationValid, isCvvValid ->
        isCardValid && isExpirationValid && isCvvValid &&
                cardNumberState.value.isNotEmpty() && expirationDateState.value.isNotEmpty() && cvvState.value.isNotEmpty()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun initCreditCardTokenization() {
        val request = TokenizationRequest(
            firstName = "John",
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
            /**
             * the credit card field are obtained directly from the input data
             */
            cardNumber = cardNumberState.value.toLong(),
            expirationMonth = expirationDateState.value.substring(0, 2).toInt(),
            expirationYear = expirationDateState.value.substring(2).toInt(),
            cvvCode = cvvState.value.toInt(),

            /**
             * the session data is obtained directly from the persisted values
             */
            sdkVersion = RecurlySessionData.versionName,
            publicKey = "fra-YEvkB0OKEp3y0bDYldcPoT",
            deviceId = RecurlySessionData.deviceId,
            sessionId = UUID.randomUUID().toString()
        )

        viewModelScope.launch {
            val result: TokenizationResponse = recurlyApi.getToken(request)
            Log.d("taotestcard", "Token: ${result.token}, Type: ${result.type}")
        }
    }
}