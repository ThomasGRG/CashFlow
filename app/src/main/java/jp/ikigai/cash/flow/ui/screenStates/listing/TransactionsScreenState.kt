package jp.ikigai.cash.flow.ui.screenStates.listing

import android.icu.util.Currency
import io.realm.kotlin.query.Sort
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.dto.CurrencyInfo
import jp.ikigai.cash.flow.data.dto.SelectTemplateInfoDTO
import jp.ikigai.cash.flow.data.dto.TransactionDetailsByDay
import jp.ikigai.cash.flow.data.entity.Category
import jp.ikigai.cash.flow.data.entity.CounterParty
import jp.ikigai.cash.flow.data.entity.Item
import jp.ikigai.cash.flow.data.entity.Method
import jp.ikigai.cash.flow.data.entity.Source
import jp.ikigai.cash.flow.data.enums.TransactionType
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

data class TransactionsScreenState(
    val transactionsHashCode: Int = 0,
    val transactions: Map<LocalDate, TransactionDetailsByDay> = emptyMap(),
    val income: String = "",
    val incomeTransactionsCount: String = "0",
    val expense: String = "",
    val expenseTransactionsCount: String = "0",
    val categories: List<Category> = emptyList(),
    val selectedCategories: Map<String, Boolean> = emptyMap(),
    val selectedCategoryCount: String = "0",
    val counterParties: List<CounterParty> = emptyList(),
    val selectedCounterParties: Map<String, Boolean> = emptyMap(),
    val selectedCounterPartyCount: String = "0",
    val includeNoCounterPartyTransactions: Boolean = true,
    val methods: List<Method> = emptyList(),
    val selectedMethods: Map<String, Boolean> = emptyMap(),
    val selectedMethodCount: String = "0",
    val sources: List<Source> = emptyList(),
    val selectedSources: Map<String, Boolean> = emptyMap(),
    val selectedSourceCount: String = "0",
    val items: List<Item> = emptyList(),
    val selectedItems: Map<String, Boolean> = emptyMap(),
    val selectedItemCount: String = "0",
    val includeNoItemTransactions: Boolean = true,
    val transactionTypes: List<TransactionType> = TransactionType.values().toList(),
    val selectedTransactionTypes: List<Int> = listOf(1, 2),
    val filterAmountMin: Double = 0.0,
    val filterAmountMax: Double = 0.0,
    val filterAmountRange: String = "0+",
    val templates: List<SelectTemplateInfoDTO> = emptyList(),
    val currencies: List<CurrencyInfo> = Constants.currencyList,
    val selectedCurrency: String = Currency.getInstance("INR").currencyCode,
    val startDate: LocalDate = YearMonth.now().atDay(1),
    val endDate: LocalDate = YearMonth.now().atEndOfMonth(),
    val startDateString: String = "",
    val endDateString: String = "",
    val searchText: String = "",
    val sortDirection: Sort = Sort.DESCENDING,
    val balance: String = "",
    val loading: Boolean = true,
    val locale: Locale? = null
)
