package jp.ikigai.cash.flow.utils

import android.icu.number.LocalizedNumberFormatter
import android.icu.number.Notation
import android.icu.number.NumberFormatter
import android.icu.number.Precision
import android.icu.util.Currency
import jp.ikigai.cash.flow.data.Constants
import java.util.Locale

fun getNumberFormatter(locale: Locale? = null): LocalizedNumberFormatter {
    return NumberFormatter
        .withLocale(locale ?: Locale.getDefault())
        .notation(Notation.simple())
        .precision(Precision.maxFraction(2))
}

fun getCurrencyFormatterMap(locale: Locale? = null): Map<String, LocalizedNumberFormatter> {
    return Constants.currencyList.associateBy(
        keySelector = {
            it.currency.currencyCode
        },
        valueTransform = {
            NumberFormatter
                .withLocale(locale ?: Locale.getDefault())
                .unit(Currency.getInstance(it.currency.currencyCode))
        }
    )
}