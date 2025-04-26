package com.ptit.core.account.paymentMethod

import com.recurly.androidsdk.R
import com.recurly.androidsdk.data.model.CreditCardsParameters

object PaymentMethodUtils {
    
    fun formatCardNumber(firstSixNum: String, lastFourNum: String): String {
        return "•••• •••• •••• $lastFourNum"
    }

    fun getFormattedCardNumberWithPrefix(firstSixNum: String, lastFourNum: String): String {
        val visiblePrefix = if (firstSixNum.length >= 4) firstSixNum.substring(0, 4) else firstSixNum
        return "$visiblePrefix•• •••• •••• $lastFourNum"
    }

    fun getCardTypeIcon(cardType: CreditCardsParameters): Int {
        return when (cardType) {
            CreditCardsParameters.VISA -> R.drawable.ic_visa_card
            CreditCardsParameters.MASTER -> R.drawable.ic_mastercard_card
            CreditCardsParameters.AMERICAN_EXPRESS -> R.drawable.ic_amex_card
            CreditCardsParameters.DISCOVER -> R.drawable.ic_discover_card
            CreditCardsParameters.JCB -> R.drawable.ic_jcb_card
            CreditCardsParameters.DINERS_CLUB -> R.drawable.ic_diners_club_card
            else -> R.drawable.ic_generic_disabled_card
        }
    }

    fun getCardTypeName(cardType: CreditCardsParameters): String {
        return when (cardType) {
            CreditCardsParameters.VISA -> "Visa"
            CreditCardsParameters.MASTER -> "Mastercard"
            CreditCardsParameters.AMERICAN_EXPRESS -> "American Express"
            CreditCardsParameters.DISCOVER -> "Discover"
            CreditCardsParameters.JCB -> "JCB"
            CreditCardsParameters.DINERS_CLUB -> "Diners Club"
            CreditCardsParameters.UNION_PAY -> "Union Pay"
            CreditCardsParameters.ELO -> "Elo"
            CreditCardsParameters.HIPERCARD -> "Hipercard"
            CreditCardsParameters.TARJETA_NARANJA -> "Tarjeta Naranja"
        }
    }
}
