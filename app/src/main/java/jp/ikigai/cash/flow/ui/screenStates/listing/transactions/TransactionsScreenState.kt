package jp.ikigai.cash.flow.ui.screenStates.listing.transactions

import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.dto.CurrencyInfo
import jp.ikigai.cash.flow.data.dto.SelectTemplateInfoDTO
import jp.ikigai.cash.flow.data.dto.TransactionsWithTotalAmount
import jp.ikigai.cash.flow.data.enums.TransactionType
import jp.ikigai.cash.flow.data.store.entity.Account
import jp.ikigai.cash.flow.data.store.entity.Category
import jp.ikigai.cash.flow.data.store.entity.CounterParty
import jp.ikigai.cash.flow.data.store.entity.Method
import java.time.LocalDate
import java.util.Locale

data class TransactionsScreenState(
    val transactionsHashCode: Int = 0,
    val transactions: Map<LocalDate, TransactionsWithTotalAmount> = emptyMap(),
    val income: String = "",
    val incomeTransactionsCount: String = "0",
    val expense: String = "",
    val expenseTransactionsCount: String = "0",
    val accounts: List<Account> = emptyList(),
    val categories: List<Category> = emptyList(),
    val counterParties: List<CounterParty> = emptyList(),
    val methods: List<Method> = emptyList(),
    val transactionTypes: List<TransactionType> = TransactionType.values().toList(),
    val templates: List<SelectTemplateInfoDTO> = emptyList(),
    val currencies: List<CurrencyInfo> = Constants.currencyList,
    val balance: String = "",
    val loading: Boolean = true,
    val locale: Locale? = null
)
