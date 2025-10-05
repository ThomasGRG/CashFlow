package jp.ikigai.cash.flow.ui.screenStates.common

import jp.ikigai.cash.flow.AccountWithTransactionMetadata
import jp.ikigai.cash.flow.CategoryWithTransactionMetadata
import jp.ikigai.cash.flow.CounterPartyWithTransactionMetadata
import jp.ikigai.cash.flow.MethodWithTransactionMetadata
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.dto.CurrencyInfo
import jp.ikigai.cash.flow.data.dto.TransactionWithChips
import jp.ikigai.cash.flow.data.enums.TransactionType
import java.time.LocalDate
import java.util.Locale

data class ExportTransactionsScreenState(
    val totalTransactionsCount: Long = 0L,
    val transactionsHashCode: Int = 0,
    val filteredTransactions: Map<LocalDate, List<TransactionWithChips>> = emptyMap(),
    val selectedTransactions: Set<Long> = emptySet(),
    val selectedLocalDates: Set<LocalDate> = emptySet(),
    val allSelected: Boolean = false,
    val accounts: List<AccountWithTransactionMetadata> = emptyList(),
    val categories: List<CategoryWithTransactionMetadata> = emptyList(),
    val counterParties: List<CounterPartyWithTransactionMetadata> = emptyList(),
    val methods: List<MethodWithTransactionMetadata> = emptyList(),
    val transactionTypes: List<TransactionType> = TransactionType.entries,
    val currencies: List<CurrencyInfo> = Constants.currencyList,
    val loading: Boolean = true,
    val enabled: Boolean = false,
    val locale: Locale? = null
)
