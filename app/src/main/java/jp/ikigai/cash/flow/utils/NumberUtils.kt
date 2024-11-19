package jp.ikigai.cash.flow.utils

import android.icu.number.LocalizedNumberFormatter
import android.icu.number.Notation
import android.icu.number.NumberFormatter
import android.icu.number.Precision
import android.icu.util.Currency
import java.util.Locale

fun getNumberFormatter(locale: Locale? = null): LocalizedNumberFormatter {
    return NumberFormatter
        .withLocale(locale ?: Locale.getDefault())
        .notation(Notation.simple())
        .precision(Precision.maxFraction(2))
}

fun getCurrencyFormatter(locale: Locale? = null, currencyCode: String): LocalizedNumberFormatter {
    return NumberFormatter
        .withLocale(locale ?: Locale.getDefault())
        .unit(Currency.getInstance(currencyCode))
}