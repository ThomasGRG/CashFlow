package jp.ikigai.cash.flow.data

import android.icu.util.Currency
import compose.icons.TablerIcons
import compose.icons.tablericons.Archive
import compose.icons.tablericons.ArrowUpCircle
import compose.icons.tablericons.BuildingBank
import compose.icons.tablericons.CreditCard
import compose.icons.tablericons.Users

object Constants {

    const val tweenDuration = 350

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
    )

    val DEFAULT_TYPE_ICON = TablerIcons.ArrowUpCircle
    val DEFAULT_CATEGORY_ICON = TablerIcons.Archive
    val DEFAULT_COUNTERPARTY_ICON = TablerIcons.Users
    val DEFAULT_METHOD_ICON = TablerIcons.CreditCard
    val DEFAULT_SOURCE_ICON = TablerIcons.BuildingBank
}