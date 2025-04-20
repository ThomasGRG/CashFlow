package jp.ikigai.cash.flow.ui.screenStates.migration

import androidx.annotation.StringRes
import io.realm.kotlin.query.Sort
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.dto.CurrencyInfo
import jp.ikigai.cash.flow.data.dto.TransactionWithIcons
import jp.ikigai.cash.flow.data.entity.Category
import jp.ikigai.cash.flow.data.entity.CounterParty
import jp.ikigai.cash.flow.data.entity.Method
import jp.ikigai.cash.flow.data.entity.Source
import jp.ikigai.cash.flow.data.enums.TransactionType
import java.time.LocalDate
import java.util.Locale

data class MigrateMethodScreenState(
    val transactionsHashCode: Int = 0,
    val filteredTransactions: Map<LocalDate, List<TransactionWithIcons>> = emptyMap(),
    val selectedTransactions: Set<String> = emptySet(),
    val selectedTransactionCount: Int = 0,
    val selectedLocalDates: Set<LocalDate> = emptySet(),
    val allSelected: Boolean = false,
    val categories: List<Category> = emptyList(),
    val selectedCategories: Map<String, Boolean> = emptyMap(),
    val selectedCategoryCount: String = "0",
    val counterParties: List<CounterParty> = emptyList(),
    val selectedCounterParties: Map<String, Boolean> = emptyMap(),
    val selectedCounterPartyCount: String = "0",
    val includeNoCounterPartyTransactions: Boolean = true,
    val methods: List<Method> = emptyList(),
    val sources: List<Source> = emptyList(),
    val selectedSources: Map<String, Boolean> = emptyMap(),
    val selectedSourceCount: String = "0",
    val transactionTypes: List<TransactionType> = TransactionType.values().toList(),
    val selectedTransactionTypes: List<Int> = listOf(1, 2),
    val filterAmountMin: Double = 0.0,
    val filterAmountMax: Double = 0.0,
    val filterAmountRange: String = "0+",
    val currencies: List<CurrencyInfo> = Constants.currencyList,
    val selectedCurrencies: Map<String, Boolean> = Constants.currencyList.associate { it.currency.currencyCode to true },
    val selectedCurrencyCount: String = "0",
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val startDateString: String = "",
    val endDateString: String = "",
    @StringRes val dateRangeStringRes: Int = R.string.all_time_date_range_label,
    val searchText: String = "",
    val sortDirection: Sort = Sort.DESCENDING,
    val loading: Boolean = true,
    val enabled: Boolean = false,
    val migrateOngoing: Boolean = false,
    val locale: Locale? = null
)
