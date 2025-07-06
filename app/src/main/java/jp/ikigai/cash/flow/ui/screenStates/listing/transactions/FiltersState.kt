package jp.ikigai.cash.flow.ui.screenStates.listing.transactions

import android.icu.util.Currency
import jp.ikigai.cash.flow.data.enums.TransactionType
import jp.ikigai.cash.flow.utils.getMonthEndDate
import jp.ikigai.cash.flow.utils.getMonthStartDate
import java.time.ZonedDateTime

data class FiltersState(
    val selectedAccounts: Map<Long, Boolean> = emptyMap(),
    val selectedAccountCount: String = "0",
    val selectedCategories: Map<Long, Boolean> = emptyMap(),
    val selectedCategoryCount: String = "0",
    val selectedCounterParties: Map<Long, Boolean> = emptyMap(),
    val selectedCounterPartyCount: String = "0",
    val includeNoCounterPartyTransactions: Boolean = true,
    val selectedMethods: Map<Long, Boolean> = emptyMap(),
    val selectedMethodCount: String = "0",
    val selectedTransactionTypes: List<TransactionType> = TransactionType.entries,
    val filterAmountMin: Double = 0.0,
    val filterAmountMax: Double = 0.0,
    val filterAmountRange: String = "0+",
    val selectedCurrency: String = Currency.getInstance("INR").currencyCode,
    val startDate: ZonedDateTime = getMonthStartDate(),
    val endDate: ZonedDateTime = getMonthEndDate(),
    val startDateString: String = "",
    val endDateString: String = ""
)
