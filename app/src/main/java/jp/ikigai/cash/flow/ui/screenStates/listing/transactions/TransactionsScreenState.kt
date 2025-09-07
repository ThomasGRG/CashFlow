package jp.ikigai.cash.flow.ui.screenStates.listing.transactions

import jp.ikigai.cash.flow.AccountWithTransactionMetadata
import jp.ikigai.cash.flow.CategoryWithTransactionMetadata
import jp.ikigai.cash.flow.CounterPartyWithTransactionMetadata
import jp.ikigai.cash.flow.MethodWithTransactionMetadata
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.TransactionHeader
import jp.ikigai.cash.flow.data.dto.CurrencyInfo
import jp.ikigai.cash.flow.data.dto.SelectTemplateInfoDTO
import jp.ikigai.cash.flow.data.dto.TransactionWithChips
import jp.ikigai.cash.flow.data.enums.TransactionType
import java.util.Locale

data class TransactionsScreenState(
    val transactionsHashCode: Int = 0,
    val transactions: Map<TransactionHeader, List<TransactionWithChips>> = emptyMap(),
    val income: String = "",
    val incomeTransactionsCount: String = "0",
    val expense: String = "",
    val expenseTransactionsCount: String = "0",
    val accounts: List<AccountWithTransactionMetadata> = emptyList(),
    val categories: List<CategoryWithTransactionMetadata> = emptyList(),
    val counterParties: List<CounterPartyWithTransactionMetadata> = emptyList(),
    val methods: List<MethodWithTransactionMetadata> = emptyList(),
    val transactionTypes: List<TransactionType> = TransactionType.entries,
    val templates: List<SelectTemplateInfoDTO> = emptyList(),
    val currencies: List<CurrencyInfo> = Constants.currencyList,
    val balance: String = "",
    val loading: Boolean = true,
    val locale: Locale? = null
)
