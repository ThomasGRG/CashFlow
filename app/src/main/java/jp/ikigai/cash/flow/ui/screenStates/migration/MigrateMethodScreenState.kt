package jp.ikigai.cash.flow.ui.screenStates.migration

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
    val counterParties: List<CounterParty> = emptyList(),
    val methods: List<Method> = emptyList(),
    val sources: List<Source> = emptyList(),
    val transactionTypes: List<TransactionType> = TransactionType.values().toList(),
    val currencies: List<CurrencyInfo> = Constants.currencyList,
    val loading: Boolean = true,
    val enabled: Boolean = false,
    val locale: Locale? = null
)
