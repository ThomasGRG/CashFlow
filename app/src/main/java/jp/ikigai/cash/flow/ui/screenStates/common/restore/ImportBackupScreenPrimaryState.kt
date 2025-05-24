package jp.ikigai.cash.flow.ui.screenStates.common.restore

import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.dto.CurrencyInfo
import jp.ikigai.cash.flow.data.dto.TemplateWithChips
import jp.ikigai.cash.flow.data.dto.TransactionWithChips
import jp.ikigai.cash.flow.data.enums.TransactionType
import jp.ikigai.cash.flow.data.store.entity.Account
import jp.ikigai.cash.flow.data.store.entity.Category
import jp.ikigai.cash.flow.data.store.entity.CounterParty
import jp.ikigai.cash.flow.data.store.entity.Method
import jp.ikigai.cash.flow.data.store.entity.temp.TempAccount
import jp.ikigai.cash.flow.data.store.entity.temp.TempCategory
import jp.ikigai.cash.flow.data.store.entity.temp.TempCounterParty
import jp.ikigai.cash.flow.data.store.entity.temp.TempMethod
import jp.ikigai.cash.flow.data.store.entity.temp.TempTransaction
import jp.ikigai.cash.flow.data.store.entity.temp.TempTransactionTemplate
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
    val filteredTransactions: Map<LocalDate, List<TransactionWithChips>> = emptyMap(),
    val selectedTempTransactions: Set<Long> = emptySet(),
    //Account
    val currencyAccountMap: Map<String, List<Account>> = emptyMap(),
    val tempAccounts: List<TempAccount> = emptyList(),
    val conflictingTempAccounts: Set<Long> = emptySet(),
    val accountMappings: Map<Long, Account> = emptyMap(),
    val restoreBalanceAccounts: Set<Long> = emptySet(),
    val selectedTempAccounts: Set<Long> = emptySet(),
    val selectedTempAccountCount: String = "",
    //Category
    val dbCategories: List<Category> = emptyList(),
    val tempCategories: List<TempCategory> = emptyList(),
    val conflictingTempCategories: Set<Long> = emptySet(),
    val categoryMappings: Map<Long, Category> = emptyMap(),
    val selectedTempCategories: Set<Long> = emptySet(),
    val selectedTempCategoryCount: String = "",
    //CounterParty
    val dbCounterParties: List<CounterParty> = emptyList(),
    val tempCounterParties: List<TempCounterParty> = emptyList(),
    val conflictingTempCounterParties: Set<Long> = emptySet(),
    val counterPartyMappings: Map<Long, CounterParty> = emptyMap(),
    val selectedTempCounterParties: Set<Long> = emptySet(),
    val selectedTempCounterPartyCount: String = "",
    //Method
    val dbMethods: List<Method> = emptyList(),
    val tempMethods: List<TempMethod> = emptyList(),
    val conflictingTempMethods: Set<Long> = emptySet(),
    val methodMappings: Map<Long, Method> = emptyMap(),
    val selectedTempMethods: Set<Long> = emptySet(),
    val selectedTempMethodCount: String = "",
    //Templates
    val tempTransactionTemplates: List<TempTransactionTemplate> = emptyList(),
    val tempTransactionTemplatesWithIcons: List<TemplateWithChips> = emptyList(),
    val selectedTempTransactionTemplates: Set<Long> = emptySet()
)
