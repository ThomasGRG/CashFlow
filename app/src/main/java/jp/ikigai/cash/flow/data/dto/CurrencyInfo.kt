package jp.ikigai.cash.flow.data.dto

import android.icu.util.Currency
import androidx.compose.ui.text.AnnotatedString

data class CurrencyInfo(
    val annotatedString: AnnotatedString,
    val currency: Currency
)