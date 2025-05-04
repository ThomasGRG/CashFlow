package jp.ikigai.cash.flow.ui.screenStates.common.restore

import androidx.annotation.StringRes
import io.realm.kotlin.query.Sort
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.dto.CurrencyInfo
import jp.ikigai.cash.flow.data.dto.TransactionTemplateWithIcons
import jp.ikigai.cash.flow.data.dto.TransactionWithIcons
import jp.ikigai.cash.flow.data.entity.Category
import jp.ikigai.cash.flow.data.entity.CounterParty
import jp.ikigai.cash.flow.data.entity.Method
import jp.ikigai.cash.flow.data.entity.Source
import jp.ikigai.cash.flow.data.entity.temp.TempCategory
import jp.ikigai.cash.flow.data.entity.temp.TempCounterParty
import jp.ikigai.cash.flow.data.entity.temp.TempMethod
import jp.ikigai.cash.flow.data.entity.temp.TempSource
import jp.ikigai.cash.flow.data.entity.temp.TempTransaction
import jp.ikigai.cash.flow.data.entity.temp.TempTransactionTemplate
import jp.ikigai.cash.flow.data.enums.TransactionType
import java.time.LocalDate
import java.util.Locale

data class ImportBackupScreenMainState(
    val locale: Locale? = null,
    val loading: Boolean = false,
    val enabled: Boolean = false,
    val dataLoadComplete: Boolean = false,
    val sortDirection: Sort = Sort.DESCENDING,
    //Transaction
    val transactionsHashCode: Int = 0,
    val tempTransactions: List<TempTransaction> = emptyList(),
    val filteredTransactions: Map<LocalDate, List<TransactionWithIcons>> = emptyMap(),
    //Category
    val dbCategories: List<Category> = emptyList(),
    val tempCategories: List<TempCategory> = emptyList(),
    val conflictingTempCategories: Set<String> = emptySet(),
    val categoryMappings: Map<String, Category> = emptyMap(),
    val selectedTempCategories: Map<String, Boolean> = emptyMap(),
    val selectedTempCategoryCount: String = "",
    //CounterParty
    val dbCounterParties: List<CounterParty> = emptyList(),
    val tempCounterParties: List<TempCounterParty> = emptyList(),
    val conflictingTempCounterParties: Set<String> = emptySet(),
    val counterPartyMappings: Map<String, CounterParty> = emptyMap(),
    val selectedTempCounterParties: Map<String, Boolean> = emptyMap(),
    val selectedTempCounterPartyCount: String = "",
    //Method
    val dbMethods: List<Method> = emptyList(),
    val tempMethods: List<TempMethod> = emptyList(),
    val conflictingTempMethods: Set<String> = emptySet(),
    val methodMappings: Map<String, Method> = emptyMap(),
    val selectedTempMethods: Map<String, Boolean> = emptyMap(),
    val selectedTempMethodCount: String = "",
    //Source
    val currencySourceMap: Map<String, List<Source>> = emptyMap(),
    val tempSources: List<TempSource> = emptyList(),
    val conflictingTempSources: Set<String> = emptySet(),
    val sourceMappings: Map<String, Source> = emptyMap(),
    val restoreBalanceSources: Set<String> = emptySet(),
    val selectedTempSources: Map<String, Boolean> = emptyMap(),
    val selectedTempSourceCount: String = "",
    //Templates
    val tempTransactionTemplates: List<TempTransactionTemplate> = emptyList(),
    val tempTransactionTemplatesWithIcons: List<TransactionTemplateWithIcons> = emptyList(),
    val selectedTempTransactionTemplates: Set<String> = emptySet(),
    //Filters
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
    val searchText: String = ""
)
