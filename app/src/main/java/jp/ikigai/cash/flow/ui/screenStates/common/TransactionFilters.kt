package jp.ikigai.cash.flow.ui.screenStates.common

import androidx.annotation.StringRes
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Constants
import java.time.ZonedDateTime

data class TransactionFilters(
    val selectedAccounts: Set<Long> = emptySet(),
    val selectedAccountCount: String = "0",
    val selectedCategories: Set<Long> = emptySet(),
    val selectedCategoryCount: String = "0",
    val selectedCounterParties: Set<Long> = emptySet(),
    val selectedCounterPartyCount: String = "0",
    val includeNoCounterPartyTransactions: Boolean = true,
    val selectedMethods: Set<Long> = emptySet(),
    val selectedMethodCount: String = "0",
    val selectedTransactionTypes: List<Int> = listOf(1, 2),
    val filterAmountMin: Double = 0.0,
    val filterAmountMax: Double = 0.0,
    val filterAmountRange: String = "0+",
    val selectedCurrencies: Set<String> = Constants.currencyList.map { it.currency.currencyCode }
        .toSet(),
    val selectedCurrencyCount: String = "0",
    val startDate: ZonedDateTime? = null,
    val endDate: ZonedDateTime? = null,
    val startDateString: String = "",
    val endDateString: String = "",
    @StringRes val dateRangeStringRes: Int = R.string.all_time_date_range_label,
)
