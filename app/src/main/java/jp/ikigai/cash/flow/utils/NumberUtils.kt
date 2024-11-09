package jp.ikigai.cash.flow.utils

import android.icu.number.LocalizedNumberFormatter
import android.icu.number.Notation
import android.icu.number.NumberFormatter
import android.icu.number.Precision
import java.util.Locale

fun getNumberFormatter(locale: Locale? = null): LocalizedNumberFormatter {
    return NumberFormatter
        .withLocale(locale ?: Locale.getDefault())
        .notation(Notation.simple())
        .precision(Precision.maxFraction(2))
}