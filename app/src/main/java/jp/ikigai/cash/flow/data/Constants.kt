/*
CashFlow - Expense Tracker
Copyright (C) 2025 ThomasGRG

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 3 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

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

    const val TWEEN_DURATION = 350

    const val WAIT_DIALOG_MINIMUM_SCREEN_TIME = 500L

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
    val DEFAULT_ACCOUNT_ICON = TablerIcons.BuildingBank
}