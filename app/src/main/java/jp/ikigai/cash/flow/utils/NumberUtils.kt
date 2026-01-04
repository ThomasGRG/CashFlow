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

fun getCurrencyFormatterMap(
    locale: Locale? = null,
    notation: Notation = Notation.simple()
): Map<String, LocalizedNumberFormatter> {
    return Constants.currencyList.associateBy(
        keySelector = {
            it.currency.currencyCode
        },
        valueTransform = {
            NumberFormatter
                .withLocale(locale ?: Locale.getDefault())
                .unit(Currency.getInstance(it.currency.currencyCode))
                .notation(notation)
        }
    )
}