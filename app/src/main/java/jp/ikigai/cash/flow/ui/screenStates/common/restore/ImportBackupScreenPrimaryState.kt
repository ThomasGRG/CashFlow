package jp.ikigai.cash.flow.ui.screenStates.common.restore

import jp.ikigai.cash.flow.AccountWithTransactionMetadata
import jp.ikigai.cash.flow.CategoryWithTransactionMetadata
import jp.ikigai.cash.flow.CounterPartyWithTransactionMetadata
import jp.ikigai.cash.flow.MethodWithTransactionMetadata
import jp.ikigai.cash.flow.TempAccountWithFormattedBalance
import jp.ikigai.cash.flow.TempCategory
import jp.ikigai.cash.flow.TempCounterParty
import jp.ikigai.cash.flow.TempMethod
import jp.ikigai.cash.flow.TempTransactionTemplateWithMetadata
import jp.ikigai.cash.flow.TempTransactionWithMetadata
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.dto.CurrencyInfo
import jp.ikigai.cash.flow.data.dto.TemplateWithChips
import jp.ikigai.cash.flow.data.dto.TransactionWithChips
import jp.ikigai.cash.flow.data.enums.TransactionType
import java.time.LocalDate
import java.util.Locale

data class ImportBackupScreenPrimaryState(
    val locale: Locale? = null,
    val loading: Boolean = false,
    val enabled: Boolean = false,
    val dataLoadComplete: Boolean = false,
    val transactionTypes: List<TransactionType> = TransactionType.entries,
    val currencies: List<CurrencyInfo> = Constants.currencyList,
    //Transaction
    val transactionsHashCode: Int = 0,
    val tempTransactions: List<TempTransactionWithMetadata> = emptyList(),
    val filteredTransactions: Map<LocalDate, List<TransactionWithChips>> = emptyMap(),
    val selectedTempTransactions: Set<Long> = emptySet(),
    //Account
    val currencyAccountMap: Map<String, List<AccountWithTransactionMetadata>> = emptyMap(),
    val tempAccounts: List<TempAccountWithFormattedBalance> = emptyList(),
    val conflictingTempAccounts: Set<Long> = emptySet(),
    val accountMappings: Map<Long, AccountWithTransactionMetadata> = emptyMap(),
    val restoreBalanceAccounts: Set<Long> = emptySet(),
    val selectedTempAccounts: Set<Long> = emptySet(),
    val selectedTempAccountCount: String = "",
    //Category
    val dbCategories: List<CategoryWithTransactionMetadata> = emptyList(),
    val tempCategories: List<TempCategory> = emptyList(),
    val conflictingTempCategories: Set<Long> = emptySet(),
    val categoryMappings: Map<Long, CategoryWithTransactionMetadata> = emptyMap(),
    val selectedTempCategories: Set<Long> = emptySet(),
    val selectedTempCategoryCount: String = "",
    //CounterParty
    val dbCounterParties: List<CounterPartyWithTransactionMetadata> = emptyList(),
    val tempCounterParties: List<TempCounterParty> = emptyList(),
    val conflictingTempCounterParties: Set<Long> = emptySet(),
    val counterPartyMappings: Map<Long, CounterPartyWithTransactionMetadata> = emptyMap(),
    val selectedTempCounterParties: Set<Long> = emptySet(),
    val selectedTempCounterPartyCount: String = "",
    //Method
    val dbMethods: List<MethodWithTransactionMetadata> = emptyList(),
    val tempMethods: List<TempMethod> = emptyList(),
    val conflictingTempMethods: Set<Long> = emptySet(),
    val methodMappings: Map<Long, MethodWithTransactionMetadata> = emptyMap(),
    val selectedTempMethods: Set<Long> = emptySet(),
    val selectedTempMethodCount: String = "",
    //Templates
    val tempTransactionTemplates: List<TempTransactionTemplateWithMetadata> = emptyList(),
    val tempTransactionTemplatesWithIcons: List<TemplateWithChips> = emptyList(),
    val selectedTempTransactionTemplates: Set<Long> = emptySet()
)
