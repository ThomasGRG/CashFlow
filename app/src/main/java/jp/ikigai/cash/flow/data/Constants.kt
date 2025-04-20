package jp.ikigai.cash.flow.data

import android.icu.util.Currency
import androidx.compose.ui.text.buildAnnotatedString
import compose.icons.TablerIcons
import compose.icons.tablericons.Archive
import compose.icons.tablericons.ArrowUpCircle
import compose.icons.tablericons.BuildingBank
import compose.icons.tablericons.CreditCard
import compose.icons.tablericons.Users
import jp.ikigai.cash.flow.data.dto.CurrencyInfo

object Constants {

    const val tweenDuration = 350

    const val WAIT_DIALOG_MINIMUM_SCREEN_TIME: Long = 1000L

    val currencyList = listOf(
        Currency.getInstance("INR"),
        Currency.getInstance("USD"),
        Currency.getInstance("EUR"),
        Currency.getInstance("JPY"),
        Currency.getInstance("GBP"),
        Currency.getInstance("CHF"),
        Currency.getInstance("CAD"),
        Currency.getInstance("AUD"),
        Currency.getInstance("CNY"),
        Currency.getInstance("BRL"),
    ).map {
        CurrencyInfo(
            annotatedString = buildAnnotatedString {
                append("${it.displayName} (${it.currencyCode})")
            },
            currency = it
        )
    }

    val DEFAULT_TYPE_ICON = TablerIcons.ArrowUpCircle
    val DEFAULT_CATEGORY_ICON = TablerIcons.Archive
    val DEFAULT_COUNTERPARTY_ICON = TablerIcons.Users
    val DEFAULT_METHOD_ICON = TablerIcons.CreditCard
    val DEFAULT_SOURCE_ICON = TablerIcons.BuildingBank
}