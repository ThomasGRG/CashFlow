package jp.ikigai.cash.flow.ui.screenStates.common.restore

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

data class ImportBackupScreenPrimaryState(
    val locale: Locale? = null,
    val loading: Boolean = false,
    val enabled: Boolean = false,
    val dataLoadComplete: Boolean = false,
    val transactionTypes: List<TransactionType> = TransactionType.values().toList(),
    val currencies: List<CurrencyInfo> = Constants.currencyList,
    //Transaction
    val transactionsHashCode: Int = 0,
    val tempTransactions: List<TempTransaction> = emptyList(),
    val filteredTransactions: Map<LocalDate, List<TransactionWithIcons>> = emptyMap(),
    val selectedTempTransactions: Set<String> = emptySet(),
    val selectedTransactionsCount: String = "",
    //Category
    val dbCategories: List<Category> = emptyList(),
    val tempCategories: List<TempCategory> = emptyList(),
    val conflictingTempCategories: Set<String> = emptySet(),
    val categoryMappings: Map<String, Category> = emptyMap(),
    val selectedTempCategories: Set<String> = emptySet(),
    val selectedTempCategoryCount: String = "",
    //CounterParty
    val dbCounterParties: List<CounterParty> = emptyList(),
    val tempCounterParties: List<TempCounterParty> = emptyList(),
    val conflictingTempCounterParties: Set<String> = emptySet(),
    val counterPartyMappings: Map<String, CounterParty> = emptyMap(),
    val selectedTempCounterParties: Set<String> = emptySet(),
    val selectedTempCounterPartyCount: String = "",
    //Method
    val dbMethods: List<Method> = emptyList(),
    val tempMethods: List<TempMethod> = emptyList(),
    val conflictingTempMethods: Set<String> = emptySet(),
    val methodMappings: Map<String, Method> = emptyMap(),
    val selectedTempMethods: Set<String> = emptySet(),
    val selectedTempMethodCount: String = "",
    //Source
    val currencySourceMap: Map<String, List<Source>> = emptyMap(),
    val tempSources: List<TempSource> = emptyList(),
    val conflictingTempSources: Set<String> = emptySet(),
    val sourceMappings: Map<String, Source> = emptyMap(),
    val restoreBalanceSources: Set<String> = emptySet(),
    val selectedTempSources: Set<String> = emptySet(),
    val selectedTempSourceCount: String = "",
    //Templates
    val tempTransactionTemplates: List<TempTransactionTemplate> = emptyList(),
    val tempTransactionTemplatesWithIcons: List<TransactionTemplateWithIcons> = emptyList(),
    val selectedTempTransactionTemplates: Set<String> = emptySet(),
    val selectedTempTransactionTemplateCount: String = "",
)
