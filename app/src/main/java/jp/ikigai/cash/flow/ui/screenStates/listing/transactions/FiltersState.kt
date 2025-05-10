package jp.ikigai.cash.flow.ui.screenStates.listing.transactions

import android.icu.util.Currency
import jp.ikigai.cash.flow.utils.getMonthEndDate
import jp.ikigai.cash.flow.utils.getMonthStartDate
import java.time.ZonedDateTime

data class FiltersState(
    val selectedCategories: Map<String, Boolean> = emptyMap(),
    val selectedCategoryCount: String = "0",
    val selectedCounterParties: Map<String, Boolean> = emptyMap(),
    val selectedCounterPartyCount: String = "0",
    val includeNoCounterPartyTransactions: Boolean = true,
    val selectedMethods: Map<String, Boolean> = emptyMap(),
    val selectedMethodCount: String = "0",
    val selectedSources: Map<String, Boolean> = emptyMap(),
    val selectedSourceCount: String = "0",
    val selectedTransactionTypes: List<Int> = listOf(1, 2),
    val filterAmountMin: Double = 0.0,
    val filterAmountMax: Double = 0.0,
    val filterAmountRange: String = "0+",
    val selectedCurrency: String = Currency.getInstance("INR").currencyCode,
    val startDate: ZonedDateTime = getMonthStartDate(),
    val endDate: ZonedDateTime = getMonthEndDate(),
    val startDateString: String = "",
    val endDateString: String = ""
)
