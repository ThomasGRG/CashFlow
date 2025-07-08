package jp.ikigai.cash.flow.ui.viewmodels.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import compose.icons.TablerIcons
import compose.icons.tablericons.Alarm
import compose.icons.tablericons.ChartLine
import compose.icons.tablericons.FileText
import compose.icons.tablericons.History
import compose.icons.tablericons.Typography
import jp.ikigai.cash.flow.AccountWithTransactionMetadata
import jp.ikigai.cash.flow.CashFlowDatabase
import jp.ikigai.cash.flow.CategoryWithTransactionMetadata
import jp.ikigai.cash.flow.CounterPartyWithTransactionMetadata
import jp.ikigai.cash.flow.MethodWithTransactionMetadata
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.TempTransactionTemplateWithMetadata
import jp.ikigai.cash.flow.TempTransactionWithMetadata
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.Database
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.dto.ChipInfo
import jp.ikigai.cash.flow.data.dto.TemplateWithChips
import jp.ikigai.cash.flow.data.dto.TransactionWithChips
import jp.ikigai.cash.flow.data.dto.export.ExportData
import jp.ikigai.cash.flow.data.enums.SortDirection
import jp.ikigai.cash.flow.data.enums.TransactionType
import jp.ikigai.cash.flow.ui.screenStates.common.SortConfigState
import jp.ikigai.cash.flow.ui.screenStates.common.restore.ImportBackupScreenFiltersState
import jp.ikigai.cash.flow.ui.screenStates.common.restore.ImportBackupScreenPrimaryState
import jp.ikigai.cash.flow.ui.screenStates.common.restore.ImportBackupScreenSecondaryState
import jp.ikigai.cash.flow.utils.getCurrencyFormatterMap
import jp.ikigai.cash.flow.utils.getDateString
import jp.ikigai.cash.flow.utils.getHighlightedString
import jp.ikigai.cash.flow.utils.getIconForCategory
import jp.ikigai.cash.flow.utils.getNumberFormatter
import jp.ikigai.cash.flow.utils.toZonedDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okio.buffer
import okio.source
import okio.use
import java.io.InputStream
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class ImportBackupScreenViewModel(
    private val database: CashFlowDatabase = Database.database
) : ViewModel() {

    private val datePattern = "dd-LLL-yyyy"

    private var numberFormatter = getNumberFormatter()
    private var currencyFormatterMap = getCurrencyFormatterMap()

    private var loadDataJob: Job? = null
    private var loadTransactionsJob: Job? = null

    //region StateFlows

    private val _primaryState = MutableStateFlow(ImportBackupScreenPrimaryState())
    val primaryState: StateFlow<ImportBackupScreenPrimaryState> = _primaryState.asStateFlow()

    private val _secondaryState = MutableStateFlow(ImportBackupScreenSecondaryState())
    val secondaryState: StateFlow<ImportBackupScreenSecondaryState> = _secondaryState.asStateFlow()

    private val _searchState = MutableStateFlow("")
    val searchState: StateFlow<String> = _searchState.asStateFlow()

    private val _sortConfigState = MutableStateFlow(
        SortConfigState(sortField = "tempTransactionDateTime")
    )
    val sortConfigState: StateFlow<SortConfigState> = _sortConfigState.asStateFlow()

    private val _filtersState = MutableStateFlow(ImportBackupScreenFiltersState())
    val filtersState: StateFlow<ImportBackupScreenFiltersState> = _filtersState.asStateFlow()

    //endregion

    private val _event: Channel<Event?> = Channel(Int.MAX_VALUE)
    val event: Flow<Event?> = _event.receiveAsFlow()

    override fun onCleared() {
        super.onCleared()
        _event.close()
    }

    private fun clearTempData() {
        database.tempAccountQueries.deleteAll()
        database.tempCategoryQueries.deleteAll()
        database.tempCounterPartyQueries.deleteAll()
        database.tempMethodQueries.deleteAll()
        database.tempTransactionTemplateQueries.deleteAll()
        database.tempTransactionQueries.deleteAll()
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun loadFile(inputStream: InputStream?) = viewModelScope.launch {
        _primaryState.update {
            it.copy(
                loading = true
            )
        }

        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        val adapter = moshi.adapter<ExportData>()

        inputStream?.source()?.buffer().use { bufferedSource ->
            if (bufferedSource != null) {
                try {
                    val data = adapter.fromJson(bufferedSource)

                    database.transaction {
                        clearTempData()

                        data?.accounts?.forEach { account ->
                            database.tempAccountQueries.insert(
                                tempAccountId = account.id,
                                tempAccountName = account.name,
                                tempAccountBalance = account.balance,
                                tempAccountCurrency = account.currency,
                            )
                        }

                        data?.categories?.forEach { category ->
                            database.tempCategoryQueries.insert(
                                tempCategoryId = category.id,
                                tempCategoryName = category.name,
                                tempCategoryIcon = category.iconName.getIconForCategory()
                            )
                        }

                        data?.counterParties?.forEach { counterParty ->
                            database.tempCounterPartyQueries.insert(
                                tempCounterPartyId = counterParty.id,
                                tempCounterPartyName = counterParty.name
                            )
                        }

                        data?.methods?.forEach { method ->
                            database.tempMethodQueries.insert(
                                tempMethodId = method.id,
                                tempMethodName = method.name
                            )
                        }

                        data?.templates?.forEach { template ->
                            database.tempTransactionTemplateQueries.insert(
                                tempTransactionTemplateId = template.id,
                                tempTransactionTemplateName = template.name,
                                tempTransactionTemplateTitle = template.title,
                                tempTransactionTemplateDescription = template.description,
                                tempTransactionTemplateAmount = template.amount,
                                tempTransactionTemplateType = TransactionType.valueOf(template.type),
                                tempTransactionTemplateAccountId = template.accountId,
                                tempTransactionTemplateCategoryId = template.categoryId,
                                tempTransactionTemplateCounterPartyId = template.counterPartyId,
                                tempTransactionTemplateMethodId = template.methodId
                            )
                        }

                        data?.transactions?.forEach { transaction ->
                            database.tempTransactionQueries.insert(
                                tempTransactionTitle = transaction.title,
                                tempTransactionDescription = transaction.description,
                                tempTransactionAmount = transaction.amount,
                                tempTransactionCurrency = transaction.currency,
                                tempTransactionType = TransactionType.valueOf(transaction.type),
                                tempTransactionDateTime = transaction.time.toZonedDateTime(),
                                tempTransactionAccountId = transaction.accountId,
                                tempTransactionCategoryId = transaction.categoryId,
                                tempTransactionCounterPartyId = transaction.counterPartyId,
                                tempTransactionMethodId = transaction.methodId,
                                tempTransactionTemplateId = transaction.templateId
                            )
                        }
                    }

                    _primaryState.update {
                        it.copy(dataLoadComplete = true)
                    }

                    loadDataJob = loadData()
                } catch (exception: Exception) {
                    _event.send(Event.InternalError)
                    _primaryState.update {
                        it.copy(
                            loading = false
                        )
                    }
                }
            }
        }
    }

    private fun loadData() = viewModelScope.launch {
        val accounts = database
            .accountWithTransactionMetadataQueries
            .getAllAccountsSortedByTransactionCountDesc()
            .executeAsList()
            .map { account ->
                val formatter = currencyFormatterMap.getValue(account.currency)
                account.copy(
                    formattedBalance = formatter.format(account.balance).toString()
                )
            }

        val categories = database
            .categoryWithTransactionMetadataQueries
            .getAllCategoriesSortedByTransactionCountDesc()
            .executeAsList()

        val counterParties = database
            .counterPartyWithTransactionMetadataQueries
            .getAllCounterPartiesSortedByTransactionCountDesc()
            .executeAsList()

        val methods = database
            .methodWithTransactionMetadataQueries
            .getAllMethodsSortedByTransactionCountDesc()
            .executeAsList()

        val tempAccounts = database
            .tempAccountQueries
            .tempAccountWithFormattedBalance()
            .executeAsList()
            .map { tempAccount ->
                val formatter = currencyFormatterMap.getValue(tempAccount.tempAccountCurrency)
                tempAccount.copy(
                    formattedBalance = formatter.format(tempAccount.tempAccountBalance).toString()
                )
            }

        val tempCategories = database
            .tempCategoryQueries
            .getAll()
            .executeAsList()

        val tempCounterParties = database
            .tempCounterPartyQueries
            .getAll()
            .executeAsList()

        val tempMethods = database
            .tempMethodQueries
            .getAll()
            .executeAsList()

        val tempTransactionTemplates = database
            .tempTransactionTemplateQueries
            .tempTransactionTemplateWithMetadata()
            .executeAsList()

        val conflictingTempAccounts = tempAccounts
            .filter { tempAccount ->
                val dbAccount = accounts.find {
                    it.accountName == tempAccount.tempAccountName
                }
                dbAccount != null
            }
            .map { tempAccount -> tempAccount.tempAccountId }

        val conflictingTempCategories = tempCategories
            .filter { tempCategory ->
                val dbCategory = categories.find {
                    it.categoryName == tempCategory.tempCategoryName
                }
                dbCategory != null
            }
            .map { tempCategory -> tempCategory.tempCategoryId }

        val conflictingTempCounterParties = tempCounterParties
            .filter { tempCounterParty ->
                val dbCounterParty = counterParties.find {
                    it.counterPartyName == tempCounterParty.tempCounterPartyName
                }
                dbCounterParty != null
            }
            .map { tempCounterParty -> tempCounterParty.tempCounterPartyId }

        val conflictingTempMethods = tempMethods
            .filter { tempMethod ->
                val dbMethod = methods.find {
                    it.methodName == tempMethod.tempMethodName
                }
                dbMethod != null
            }
            .map { tempMethod -> tempMethod.tempMethodId }

        val accountMappings = tempAccounts.associateBy(
            { tempAccount ->
                tempAccount.tempAccountId
            },
            { tempAccount ->
                val dbAccount = accounts.find { account ->
                    account.accountName == tempAccount.tempAccountName
                }
                dbAccount ?: AccountWithTransactionMetadata(
                    accountId = 0,
                    accountName = "",
                    currency = "INR",
                    balance = 0.0,
                    formattedBalance = "",
                    transactionCount = 0,
                    lastUsed = null
                )
            }
        )

        val categoryMappings = tempCategories.associateBy(
            { tempCategory ->
                tempCategory.tempCategoryId
            },
            { tempCategory ->
                val dbCategory = categories.find { category ->
                    category.categoryName == tempCategory.tempCategoryName
                }
                dbCategory ?: CategoryWithTransactionMetadata(
                    categoryId = 0,
                    categoryName = "",
                    icon = Constants.DEFAULT_CATEGORY_ICON,
                    transactionCount = 0,
                    lastUsed = null
                )
            }
        )

        val counterPartyMappings = tempCounterParties.associateBy(
            { tempCounterParty ->
                tempCounterParty.tempCounterPartyId
            },
            { tempCounterParty ->
                val dbCounterParty = counterParties.find { counterParty ->
                    counterParty.counterPartyName == tempCounterParty.tempCounterPartyName
                }
                dbCounterParty ?: CounterPartyWithTransactionMetadata(
                    counterPartyId = 0,
                    counterPartyName = "",
                    transactionCount = 0,
                    lastUsed = null
                )
            }
        )

        val methodMappings = tempMethods.associateBy(
            { tempMethod ->
                tempMethod.tempMethodId
            },
            { tempMethod ->
                val dbMethod = methods.find { method ->
                    method.methodName == tempMethod.tempMethodName
                }
                dbMethod ?: MethodWithTransactionMetadata(
                    methodId = 0,
                    methodName = "",
                    transactionCount = 0,
                    lastUsed = null
                )
            }
        )

        val currencyAccountMap = accounts.groupBy { account ->
            account.currency
        }

        val selectedTempAccounts = tempAccounts
            .map { it.tempAccountId }
            .toSet()

        val selectedTempCategories = tempCategories
            .map { it.tempCategoryId }
            .toSet()

        val selectedTempCounterParties = tempCounterParties
            .map { it.tempCounterPartyId }
            .toSet()

        val selectedTempMethods = tempMethods
            .map { it.tempMethodId }
            .toSet()

        _primaryState.update {
            it.copy(
                tempAccounts = tempAccounts,
                accountMappings = accountMappings,
                conflictingTempAccounts = conflictingTempAccounts.toSet(),
                selectedTempAccounts = selectedTempAccounts,
                selectedTempAccountCount = numberFormatter.format(selectedTempAccounts.size)
                    .toString(),
                currencyAccountMap = currencyAccountMap,
                dbCategories = categories,
                tempCategories = tempCategories,
                categoryMappings = categoryMappings,
                conflictingTempCategories = conflictingTempCategories.toSet(),
                selectedTempCategories = selectedTempCategories,
                selectedTempCategoryCount = numberFormatter.format(selectedTempCategories.size)
                    .toString(),
                dbCounterParties = counterParties,
                tempCounterParties = tempCounterParties,
                counterPartyMappings = counterPartyMappings,
                conflictingTempCounterParties = conflictingTempCounterParties.toSet(),
                selectedTempCounterParties = selectedTempCounterParties,
                selectedTempCounterPartyCount = numberFormatter.format(selectedTempCounterParties.size)
                    .toString(),
                dbMethods = methods,
                tempMethods = tempMethods,
                methodMappings = methodMappings,
                conflictingTempMethods = conflictingTempMethods.toSet(),
                selectedTempMethods = selectedTempMethods,
                selectedTempMethodCount = numberFormatter.format(selectedTempMethods.size)
                    .toString(),
                tempTransactionTemplates = tempTransactionTemplates,
                tempTransactionTemplatesWithIcons = getTemplateWithIcons(
                    tempTransactionTemplates = tempTransactionTemplates
                )
            )
        }

        _filtersState.update {
            it.copy(
                selectedCurrencyCount = numberFormatter.format(it.selectedCurrencies.size)
                    .toString()
            )
        }

        loadTransactionsJob = loadTransactions()
        updateSecondaryState()
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun loadTransactions() = viewModelScope.launch {
        combine(
            _searchState
                .onEach {
                    _primaryState.update {
                        it.copy(
                            loading = true
                        )
                    }
                }
                .debounce(300),
            _sortConfigState
                .onEach {
                    _primaryState.update {
                        it.copy(
                            loading = true
                        )
                    }
                },
            _filtersState
                .onEach {
                    _primaryState.update {
                        it.copy(
                            loading = true
                        )
                    }
                }
        ) { searchText, sortConfig, filters ->
            Triple(searchText, sortConfig, filters)
        }.flatMapLatest { (searchText, sortConfig, filters) ->
            database
                .tempTransactionWithMetadataQueries
                .getTempTransactions(
                    searchText = searchText,
                    startDate = filters.startDate ?: ZonedDateTime.ofInstant(
                        Instant.EPOCH,
                        ZoneId.systemDefault()
                    ),
                    endDate = filters.endDate ?: ZonedDateTime.now(ZoneId.systemDefault()),
                    selectedCurrencies = filters.selectedCurrencies,
                    minAmount = filters.filterAmountMin,
                    maxAmount = if (filters.filterAmountMax <= filters.filterAmountMin) Double.MAX_VALUE else filters.filterAmountMax,
                    selectedTypes = filters.selectedTransactionTypes,
                    sortField = sortConfig.sortField,
                    sortDirection = sortConfig.sortDirection.name
                )
                .asFlow()
                .mapToList(Dispatchers.IO)
        }.collectLatest { tempTransactions ->
            val filteredTransactionsMap = tempTransactions.groupBy(
                keySelector = { tempTransaction ->
                    tempTransaction.tempTransactionDateTime.toLocalDate()
                },
                valueTransform = { tempTransaction ->
                    getTempTransactionWithChips(
                        tempTransaction,
                        searchState.value
                    )
                }
            )

            _primaryState.update {
                it.copy(
                    transactionsHashCode = tempTransactions.hashCode(),
                    tempTransactions = tempTransactions,
                    filteredTransactions = filteredTransactionsMap,
                    loading = false,
                    enabled = true
                )
            }
        }
    }

    private fun updateSecondaryState() = viewModelScope.launch {
        primaryState.collectLatest { mainScreenState ->
            val enabledTempTransactions = getEnabledTempTransactions(
                selectedTempAccounts = mainScreenState.selectedTempAccounts,
                selectedTempCategories = mainScreenState.selectedTempCategories,
                selectedTempCounterParties = mainScreenState.selectedTempCounterParties,
                selectedTempMethods = mainScreenState.selectedTempMethods,
                tempTransactions = mainScreenState.tempTransactions
            )

            val enabledTempTransactionTemplates = getEnabledTempTransactionTemplates(
                selectedTempAccounts = mainScreenState.selectedTempAccounts,
                selectedTempCategories = mainScreenState.selectedTempCategories,
                selectedTempCounterParties = mainScreenState.selectedTempCounterParties,
                selectedTempMethods = mainScreenState.selectedTempMethods,
                tempTransactionTemplates = mainScreenState.tempTransactionTemplates
            )

            val enabledLocalDates = getEnabledLocalDates(
                filteredTransactions = mainScreenState.filteredTransactions,
                enabledTempTransactions = enabledTempTransactions
            )

            val selectedLocalDates = getSelectedLocalDates(
                filteredTransactions = mainScreenState.filteredTransactions,
                enabledTransactions = enabledTempTransactions,
                selectedTransactions = mainScreenState.selectedTempTransactions
            )

            val allSelected = getAllSelected(
                filteredTransactions = mainScreenState.filteredTransactions,
                enabledTransactions = enabledTempTransactions,
                selectedTransactions = mainScreenState.selectedTempTransactions
            )

            val selectedTransactionsCount = mainScreenState.selectedTempTransactions.count { id ->
                enabledTempTransactions.contains(id)
            }

            val selectedTempTransactionTemplateCount =
                mainScreenState.selectedTempTransactionTemplates.count { id ->
                    enabledTempTransactionTemplates.contains(id)
                }

            _secondaryState.update {
                it.copy(
                    enabledTempTransactions = enabledTempTransactions,
                    selectedTransactionsCount = if (selectedTransactionsCount > 0) {
                        numberFormatter.format(selectedTransactionsCount).toString()
                    } else {
                        ""
                    },
                    enabledTempTransactionTemplates = enabledTempTransactionTemplates,
                    selectedTempTransactionTemplateCount = numberFormatter
                        .format(selectedTempTransactionTemplateCount)
                        .toString(),
                    enabledLocalDates = enabledLocalDates,
                    selectedLocalDates = selectedLocalDates,
                    allSelected = allSelected
                )
            }
        }
    }

    fun importData() = viewModelScope.launch {
        loadDataJob?.cancelAndJoin()
        loadTransactionsJob?.cancelAndJoin()
        _primaryState.update {
            it.copy(
                loading = true,
                enabled = false
            )
        }

        var result: Event

        val tempCategories = primaryState.value.tempCategories
        val selectedTempCategoryIds = primaryState.value.selectedTempCategories
        val categoryMappings = primaryState
            .value
            .categoryMappings
            .filter { entry ->
                selectedTempCategoryIds.contains(entry.key)
            }
        val tempIdToCategoryNameMap = categoryMappings
            .filter { (tempCategoryId, _) -> tempCategoryId > 0 }
            .entries
            .associateBy(
                keySelector = { (tempCategoryId, _) ->
                    tempCategoryId
                },
                valueTransform = { (_, category) ->
                    category.categoryName
                }
            )
            .toMutableMap()

        val tempCounterParties = primaryState.value.tempCounterParties
        val selectedTempCounterPartyIds = primaryState.value.selectedTempCounterParties
        val counterPartyMappings = primaryState
            .value
            .counterPartyMappings
            .filter { entry ->
                selectedTempCounterPartyIds.contains(entry.key)
            }
        val tempIdToCounterPartyNameMap = counterPartyMappings
            .filter { (tempCounterPartyId, _) -> tempCounterPartyId > 0 }
            .entries
            .associateBy(
                keySelector = { (tempCounterPartyId, _) ->
                    tempCounterPartyId
                },
                valueTransform = { (_, counterParty) ->
                    counterParty.counterPartyName
                }
            )
            .toMutableMap()

        val tempMethods = primaryState.value.tempMethods
        val selectedTempMethodIds = primaryState.value.selectedTempMethods
        val methodMappings = primaryState
            .value
            .methodMappings
            .filter { entry ->
                selectedTempMethodIds.contains(entry.key)
            }
        val tempIdToMethodNameMap = methodMappings
            .filter { (tempMethodId, _) -> tempMethodId > 0 }
            .entries
            .associateBy(
                keySelector = { (tempMethodId, _) ->
                    tempMethodId
                },
                valueTransform = { (_, method) ->
                    method.methodName
                }
            )
            .toMutableMap()

        val tempAccounts = primaryState.value.tempAccounts
        val selectedTempAccountIds = primaryState.value.selectedTempAccounts
        val accountMappings = primaryState
            .value
            .accountMappings
            .filter { entry ->
                selectedTempAccountIds.contains(entry.key)
            }
        val restoreBalanceAccounts = primaryState.value.restoreBalanceAccounts
        val tempIdToAccountNameMap = accountMappings
            .filter { (tempAccountId, _) -> tempAccountId > 0 }
            .entries
            .associateBy(
                keySelector = { (tempAccountId, _) ->
                    tempAccountId
                },
                valueTransform = { (_, account) ->
                    account.accountName
                }
            )
            .toMutableMap()

        val enabledTempTransactionTemplateIds =
            secondaryState.value.enabledTempTransactionTemplates
        val selectedTempTransactionTemplateIds = primaryState
            .value
            .selectedTempTransactionTemplates
            .filter { id ->
                enabledTempTransactionTemplateIds.contains(id)
            }
        val tempTransactionTemplates = primaryState
            .value
            .tempTransactionTemplates
            .filter { tempTransactionTemplate ->
                selectedTempTransactionTemplateIds.contains(
                    tempTransactionTemplate.tempTransactionTemplateId
                )
            }
        val tempIdToTemplateNameMap: MutableMap<Long, String> = mutableMapOf()

        val enabledTempTransactionIds = secondaryState.value.enabledTempTransactions
        val selectedTempTransactionIds = primaryState
            .value
            .selectedTempTransactions
            .filter { id ->
                enabledTempTransactionIds.contains(id)
            }
        val tempTransactions = primaryState
            .value
            .tempTransactions
            .filter { tempTransaction ->
                selectedTempTransactionIds.contains(
                    tempTransaction.tempTransactionId
                )
            }

        try {
            database.transaction {
                categoryMappings
                    .filter { (_, category) -> category.categoryId == 0L }
                    .forEach { (tempCategoryId, _) ->
                        val tempCategory = tempCategories.find { tempCategory ->
                            tempCategory.tempCategoryId == tempCategoryId
                        }
                        if (tempCategory != null) {
                            database
                                .categoryQueries
                                .insert(
                                    categoryName = tempCategory.tempCategoryName,
                                    icon = tempCategory.tempCategoryIcon
                                )
                            tempIdToCategoryNameMap[tempCategory.tempCategoryId] =
                                tempCategory.tempCategoryName
                        }
                    }

                counterPartyMappings
                    .filter { (_, counterParty) -> counterParty.counterPartyId == 0L }
                    .forEach { (tempCounterPartyId, _) ->
                        val tempCounterParty = tempCounterParties.find { tempCounterParty ->
                            tempCounterParty.tempCounterPartyId == tempCounterPartyId
                        }
                        if (tempCounterParty != null) {
                            database
                                .counterPartyQueries
                                .insert(
                                    counterPartyName = tempCounterParty.tempCounterPartyName
                                )
                            tempIdToCounterPartyNameMap[tempCounterParty.tempCounterPartyId] =
                                tempCounterParty.tempCounterPartyName
                        }
                    }

                methodMappings
                    .filter { (_, method) -> method.methodId == 0L }
                    .forEach { (tempMethodId, _) ->
                        val tempMethod = tempMethods.find { tempMethod ->
                            tempMethod.tempMethodId == tempMethodId
                        }
                        if (tempMethod != null) {
                            database
                                .methodQueries
                                .insert(
                                    methodName = tempMethod.tempMethodName
                                )
                            tempIdToMethodNameMap[tempMethod.tempMethodId] =
                                tempMethod.tempMethodName
                        }
                    }

                accountMappings
                    .filter { (tempAccountId, _) -> restoreBalanceAccounts.contains(tempAccountId) }
                    .forEach { (tempAccountId, account) ->
                        val tempAccount = tempAccounts.find { tempAccount ->
                            tempAccount.tempAccountId == tempAccountId
                        }
                        if (tempAccount != null) {
                            database
                                .accountQueries
                                .updateBalance(
                                    balance = tempAccount.tempAccountBalance,
                                    accountId = account.accountId
                                )
                        }
                    }

                accountMappings
                    .filter { (_, account) -> account.accountId == 0L }
                    .forEach { (tempAccountId, _) ->
                        val tempAccount = tempAccounts.find { tempAccount ->
                            tempAccount.tempAccountId == tempAccountId
                        }
                        if (tempAccount != null) {
                            database
                                .accountQueries
                                .insert(
                                    accountName = tempAccount.tempAccountName,
                                    balance = tempAccount.tempAccountBalance,
                                    currency = tempAccount.tempAccountCurrency
                                )
                            tempIdToAccountNameMap[tempAccount.tempAccountId] =
                                tempAccount.tempAccountName
                        }
                    }

                val nameToAccountIdMap = database
                    .accountQueries
                    .getAll()
                    .executeAsList()
                    .associateBy(
                        keySelector = {
                            it.accountName
                        },
                        valueTransform = {
                            it.accountId
                        }
                    )

                val nameToCategoryIdMap = database
                    .categoryQueries
                    .getAll()
                    .executeAsList()
                    .associateBy(
                        keySelector = {
                            it.categoryName
                        },
                        valueTransform = {
                            it.categoryId
                        }
                    )

                val nameToCounterPartyIdMap = database
                    .counterPartyQueries
                    .getAll()
                    .executeAsList()
                    .associateBy(
                        keySelector = {
                            it.counterPartyName
                        },
                        valueTransform = {
                            it.counterPartyId
                        }
                    )

                val nameToMethodIdMap = database
                    .methodQueries
                    .getAll()
                    .executeAsList()
                    .associateBy(
                        keySelector = {
                            it.methodName
                        },
                        valueTransform = {
                            it.methodId
                        }
                    )

                val templateNames = database
                    .transactionTemplateQueries
                    .getTemplateNames()
                    .executeAsList()

                tempTransactionTemplates.forEach { tempTransactionTemplate ->
                    var index = 1
                    var templateName = tempTransactionTemplate.tempTransactionTemplateName
                    while (templateNames.contains(templateName)) {
                        templateName =
                            "${tempTransactionTemplate.tempTransactionTemplateName} (${index})"
                        index += 1
                    }
                    tempIdToTemplateNameMap[tempTransactionTemplate.tempTransactionTemplateId] =
                        templateName
                    database
                        .transactionTemplateQueries
                        .insert(
                            templateName = templateName,
                            templateTitle = tempTransactionTemplate.tempTransactionTemplateTitle,
                            templateDescription = tempTransactionTemplate.tempTransactionTemplateDescription,
                            templateAmount = tempTransactionTemplate.tempTransactionTemplateAmount,
                            templateType = tempTransactionTemplate.tempTransactionTemplateType,
                            templateAccountId = nameToAccountIdMap[
                                tempIdToAccountNameMap[
                                    tempTransactionTemplate.tempTransactionTemplateAccountId
                                ]
                            ] ?: 0L,
                            templateCategoryId = nameToCategoryIdMap[
                                tempIdToCategoryNameMap[
                                    tempTransactionTemplate.tempTransactionTemplateCategoryId
                                ]
                            ] ?: 0L,
                            templateCounterPartyId = nameToCounterPartyIdMap[
                                tempIdToCounterPartyNameMap[
                                    tempTransactionTemplate.tempTransactionTemplateCounterPartyId
                                ]
                            ] ?: 0L,
                            templateMethodId = nameToMethodIdMap[
                                tempIdToMethodNameMap[
                                    tempTransactionTemplate.tempTransactionTemplateMethodId
                                ]
                            ] ?: 0L
                        )
                }

                val nameToTemplateIdMap = database
                    .transactionTemplateQueries
                    .getAll()
                    .executeAsList()
                    .associateBy(
                        keySelector = {
                            it.templateName
                        },
                        valueTransform = {
                            it.templateId
                        }
                    )

                tempTransactions.forEach { tempTransaction ->
                    database
                        .transactionQueries
                        .insert(
                            transactionTitle = tempTransaction.tempTransactionTitle,
                            transactionDescription = tempTransaction.tempTransactionDescription,
                            transactionAmount = tempTransaction.tempTransactionAmount,
                            transactionCurrency = tempTransaction.tempTransactionCurrency,
                            transactionType = tempTransaction.tempTransactionType,
                            transactionDateTime = tempTransaction.tempTransactionDateTime,
                            transactionAccountId = nameToAccountIdMap[
                                tempIdToAccountNameMap[
                                    tempTransaction.tempTransactionAccountId
                                ]
                            ] ?: 0L,
                            transactionCategoryId = nameToCategoryIdMap[
                                tempIdToCategoryNameMap[
                                    tempTransaction.tempTransactionCategoryId
                                ]
                            ] ?: 0L,
                            transactionCounterPartyId = nameToCounterPartyIdMap[
                                tempIdToCounterPartyNameMap[
                                    tempTransaction.tempTransactionCounterPartyId
                                ]
                            ] ?: 0L,
                            transactionMethodId = nameToMethodIdMap[
                                tempIdToMethodNameMap[
                                    tempTransaction.tempTransactionMethodId
                                ]
                            ] ?: 0L,
                            transactionTemplateId = nameToTemplateIdMap[
                                tempIdToTemplateNameMap[
                                    tempTransaction.tempTransactionTemplateId
                                ]
                            ] ?: 0L
                        )
                }

                clearTempData()
            }
            result = Event.ImportSuccess
        } catch (exception: Exception) {
            result = Event.InternalError
        }

        _event.send(result)

        _primaryState.update {
            it.copy(
                loading = false
            )
        }
    }

    //region Transaction

    private fun getTempTransactionWithChips(
        tempTransaction: TempTransactionWithMetadata,
        searchText: String
    ): TransactionWithChips {
        val currencyFormatter =
            currencyFormatterMap.getValue(tempTransaction.tempTransactionCurrency)

        val chips: MutableList<ChipInfo> = mutableListOf()

        if (tempTransaction.tempTransactionCounterPartyName != null) {
            chips.add(
                ChipInfo(
                    icon = Constants.DEFAULT_COUNTERPARTY_ICON,
                    value = tempTransaction.tempTransactionCounterPartyName,
                    resId = R.string.placeholder
                )
            )
        }
        if (tempTransaction.tempTransactionCategoryName != null) {
            chips.add(
                ChipInfo(
                    icon = tempTransaction.tempTransactionCategoryIcon
                        ?: Constants.DEFAULT_CATEGORY_ICON,
                    value = tempTransaction.tempTransactionCategoryName,
                    resId = R.string.placeholder
                )
            )
        }
        if (tempTransaction.tempTransactionMethodName != null) {
            chips.add(
                ChipInfo(
                    icon = Constants.DEFAULT_METHOD_ICON,
                    value = tempTransaction.tempTransactionMethodName,
                    resId = R.string.placeholder
                )
            )
        }
        if (tempTransaction.tempTransactionAccountName != null) {
            chips.add(
                ChipInfo(
                    icon = Constants.DEFAULT_ACCOUNT_ICON,
                    value = tempTransaction.tempTransactionAccountName,
                    resId = R.string.placeholder
                )
            )
        }
        chips.add(
            ChipInfo(
                icon = TablerIcons.Alarm,
                value = tempTransaction.tempTransactionDateTime.format(
                    DateTimeFormatter.ofPattern("hh:mm a")
                ),
                resId = R.string.placeholder
            )
        )
        return TransactionWithChips(
            id = tempTransaction.tempTransactionId,
            annotatedTitle = getHighlightedString(tempTransaction.tempTransactionTitle, searchText),
            annotatedDescription = getHighlightedString(
                tempTransaction.tempTransactionDescription,
                searchText
            ),
            amount = currencyFormatter.format(tempTransaction.tempTransactionAmount).toString(),
            typeIcon = tempTransaction.tempTransactionType.icon,
            typeIconColor = tempTransaction.tempTransactionType.color,
            currency = tempTransaction.tempTransactionCurrency,
            chips = chips
        )
    }

    private fun getEnabledTempTransactions(
        selectedTempAccounts: Set<Long>,
        selectedTempCategories: Set<Long>,
        selectedTempCounterParties: Set<Long>,
        selectedTempMethods: Set<Long>,
        tempTransactions: List<TempTransactionWithMetadata>,
    ): Set<Long> {
        return tempTransactions
            .filter { tempTransaction ->
                val accountValid =
                    selectedTempAccounts.contains(tempTransaction.tempTransactionAccountId)
                val categoryValid =
                    selectedTempCategories.contains(tempTransaction.tempTransactionCategoryId)
                val counterPartyValid =
                    tempTransaction.tempTransactionCounterPartyId == 0L || selectedTempCounterParties.contains(
                        tempTransaction.tempTransactionCounterPartyId
                    )
                val methodValid =
                    selectedTempMethods.contains(tempTransaction.tempTransactionMethodId)
                accountValid && categoryValid && counterPartyValid && methodValid
            }
            .map { tempTransaction -> tempTransaction.tempTransactionId }
            .toSet()
    }

    private fun getEnabledLocalDates(
        filteredTransactions: Map<LocalDate, List<TransactionWithChips>>,
        enabledTempTransactions: Set<Long>
    ): Set<LocalDate> {
        return filteredTransactions
            .filter { entry ->
                entry.value
                    .map { transactionWithChips -> transactionWithChips.id }
                    .any { id -> enabledTempTransactions.contains(id) }
            }
            .keys
    }

    private fun getSelectedLocalDates(
        filteredTransactions: Map<LocalDate, List<TransactionWithChips>>,
        enabledTransactions: Set<Long>,
        selectedTransactions: Set<Long>
    ): Set<LocalDate> {
        return filteredTransactions
            .filter { entry ->
                val transactionIds = entry.value
                    .filter { transactionWithChips ->
                        enabledTransactions.contains(transactionWithChips.id)
                    }
                    .map { transactionWithChips -> transactionWithChips.id }
                transactionIds.isNotEmpty() && transactionIds.all { uuid ->
                    selectedTransactions.contains(uuid)
                }
            }.keys
    }

    private fun getAllSelected(
        filteredTransactions: Map<LocalDate, List<TransactionWithChips>>,
        enabledTransactions: Set<Long>,
        selectedTransactions: Set<Long>
    ): Boolean {
        return filteredTransactions
            .values
            .flatten()
            .filter { transactionWithChips -> enabledTransactions.contains(transactionWithChips.id) }
            .map { transactionWithChips -> transactionWithChips.id }
            .all { id -> selectedTransactions.contains(id) }
    }

    //endregion

    //region Template

    private fun getEnabledTempTransactionTemplates(
        selectedTempAccounts: Set<Long>,
        selectedTempCategories: Set<Long>,
        selectedTempCounterParties: Set<Long>,
        selectedTempMethods: Set<Long>,
        tempTransactionTemplates: List<TempTransactionTemplateWithMetadata>,
    ): Set<Long> {
        return tempTransactionTemplates
            .filter { tempTransactionTemplate ->
                val accountValid =
                    tempTransactionTemplate.tempTransactionTemplateAccountId == 0L || selectedTempAccounts.contains(
                        tempTransactionTemplate.tempTransactionTemplateAccountId
                    )
                val categoryValid =
                    tempTransactionTemplate.tempTransactionTemplateCategoryId == 0L || selectedTempCategories.contains(
                        tempTransactionTemplate.tempTransactionTemplateCategoryId
                    )
                val counterPartyValid =
                    tempTransactionTemplate.tempTransactionTemplateCounterPartyId == 0L || selectedTempCounterParties.contains(
                        tempTransactionTemplate.tempTransactionTemplateCounterPartyId
                    )
                val methodValid =
                    tempTransactionTemplate.tempTransactionTemplateMethodId == 0L || selectedTempMethods.contains(
                        tempTransactionTemplate.tempTransactionTemplateMethodId
                    )
                accountValid && categoryValid && counterPartyValid && methodValid
            }
            .map { tempTransactionTemplate -> tempTransactionTemplate.tempTransactionTemplateId }
            .toSet()
    }

    fun toggleTransactionTemplateSelected(tempTransactionTemplateId: Long) {
        _primaryState.update {
            val selectedTempTransactionTemplates =
                it.selectedTempTransactionTemplates.toMutableSet()

            if (selectedTempTransactionTemplates.contains(tempTransactionTemplateId)) {
                selectedTempTransactionTemplates.remove(tempTransactionTemplateId)
            } else {
                selectedTempTransactionTemplates.add(tempTransactionTemplateId)
            }

            it.copy(
                selectedTempTransactionTemplates = selectedTempTransactionTemplates
            )
        }
    }

    private fun getTemplateWithIcons(
        tempTransactionTemplates: List<TempTransactionTemplateWithMetadata>
    ): List<TemplateWithChips> {
        val hasBeenUsedComparator = ZonedDateTime.ofInstant(Instant.EPOCH, ZoneId.systemDefault())
        return tempTransactionTemplates.map { tempTransactionTemplate ->
            val chips: MutableList<ChipInfo> = mutableListOf()

            if (tempTransactionTemplate.tempTransactionTemplateTitle.isNotEmpty()) {
                chips.add(
                    ChipInfo(
                        resId = R.string.placeholder,
                        value = tempTransactionTemplate.tempTransactionTemplateTitle,
                        icon = TablerIcons.Typography
                    )
                )
            }
            if (tempTransactionTemplate.tempTransactionTemplateDescription.isNotEmpty()) {
                chips.add(
                    ChipInfo(
                        resId = R.string.placeholder,
                        value = tempTransactionTemplate.tempTransactionTemplateDescription,
                        icon = TablerIcons.FileText
                    )
                )
            }
            if (tempTransactionTemplate.tempTransactionTemplateCategoryName != null) {
                chips.add(
                    ChipInfo(
                        resId = R.string.placeholder,
                        value = tempTransactionTemplate.tempTransactionTemplateCategoryName,
                        icon = tempTransactionTemplate.tempTransactionTemplateCategoryIcon
                            ?: Constants.DEFAULT_CATEGORY_ICON
                    )
                )
            }
            if (tempTransactionTemplate.tempTransactionTemplateCounterPartyName != null) {
                chips.add(
                    ChipInfo(
                        resId = R.string.placeholder,
                        value = tempTransactionTemplate.tempTransactionTemplateCounterPartyName,
                        icon = Constants.DEFAULT_COUNTERPARTY_ICON
                    )
                )
            }
            if (tempTransactionTemplate.tempTransactionTemplateMethodName != null) {
                chips.add(
                    ChipInfo(
                        resId = R.string.placeholder,
                        value = tempTransactionTemplate.tempTransactionTemplateMethodName,
                        icon = Constants.DEFAULT_METHOD_ICON
                    )
                )
            }
            if (tempTransactionTemplate.tempTransactionTemplateAccountName != null) {
                chips.add(
                    ChipInfo(
                        resId = R.string.placeholder,
                        value = tempTransactionTemplate.tempTransactionTemplateAccountName,
                        icon = Constants.DEFAULT_ACCOUNT_ICON
                    )
                )
            }
            chips.add(
                ChipInfo(
                    resId = R.string.frequency_of_use_label,
                    value = numberFormatter
                        .format(tempTransactionTemplate.transactionCount)
                        .toString(),
                    icon = TablerIcons.ChartLine
                )
            )
            if (tempTransactionTemplate.lastUsed != null && tempTransactionTemplate.lastUsed > hasBeenUsedComparator) {
                chips.add(
                    ChipInfo(
                        resId = R.string.last_used_datetime_label,
                        value = tempTransactionTemplate.lastUsed.format(
                            DateTimeFormatter.ofPattern(
                                "dd MMM yyyy, hh:mm a"
                            )
                        ),
                        icon = TablerIcons.History
                    )
                )
            } else {
                chips.add(
                    ChipInfo(
                        resId = R.string.last_used_never_label,
                        value = "",
                        icon = TablerIcons.History
                    )
                )
            }
            val formattedAmount = if (tempTransactionTemplate.tempTransactionTemplateAmount > 0) {
                if (tempTransactionTemplate.tempTransactionTemplateCurrency != null) {
                    currencyFormatterMap
                        .getValue(tempTransactionTemplate.tempTransactionTemplateCurrency)
                        .format(tempTransactionTemplate.tempTransactionTemplateAmount)
                        .toString()
                } else {
                    numberFormatter
                        .format(tempTransactionTemplate.tempTransactionTemplateAmount)
                        .toString()
                }
            } else {
                ""
            }
            TemplateWithChips(
                id = tempTransactionTemplate.tempTransactionTemplateId,
                annotatedName = getHighlightedString(
                    tempTransactionTemplate.tempTransactionTemplateName,
                    ""
                ),
                amount = formattedAmount,
                typeIcon = tempTransactionTemplate.tempTransactionTemplateType.icon,
                typeIconColor = tempTransactionTemplate.tempTransactionTemplateType.color,
                chips = chips
            )
        }
    }

    //endregion

    //region Category

    fun setCategoryMapping(tempCategoryId: Long, dbCategory: CategoryWithTransactionMetadata) {
        _primaryState.update {
            val categoryMappings = it.categoryMappings.toMutableMap()
            categoryMappings[tempCategoryId] = dbCategory

            it.copy(
                categoryMappings = categoryMappings
            )
        }
    }

    fun toggleCategorySelected(id: Long) {
        _primaryState.update {
            val selectedTempCategories = it.selectedTempCategories.toMutableSet()

            if (selectedTempCategories.contains(id)) {
                selectedTempCategories.remove(id)
            } else {
                selectedTempCategories.add(id)
            }

            it.copy(
                selectedTempCategories = selectedTempCategories,
                selectedTempCategoryCount = numberFormatter.format(selectedTempCategories.size)
                    .toString()
            )
        }
    }

    //endregion

    //region CounterParty

    fun setSelectedCounterParty(
        tempCounterPartyId: Long,
        dbCounterParty: CounterPartyWithTransactionMetadata
    ) {
        _primaryState.update {
            val counterPartyMappings = it.counterPartyMappings.toMutableMap()
            counterPartyMappings[tempCounterPartyId] = dbCounterParty

            it.copy(
                counterPartyMappings = counterPartyMappings
            )
        }
    }

    fun toggleCounterPartySelected(id: Long) {
        _primaryState.update {
            val selectedTempCounterParties = it.selectedTempCounterParties.toMutableSet()

            if (selectedTempCounterParties.contains(id)) {
                selectedTempCounterParties.remove(id)
            } else {
                selectedTempCounterParties.add(id)
            }

            it.copy(
                selectedTempCounterParties = selectedTempCounterParties,
                selectedTempCounterPartyCount = numberFormatter.format(selectedTempCounterParties.size)
                    .toString()
            )
        }
    }

    //endregion

    //region Method

    fun setSelectedMethod(tempMethodId: Long, dbMethod: MethodWithTransactionMetadata) {
        _primaryState.update {
            val methodMappings = it.methodMappings.toMutableMap()
            methodMappings[tempMethodId] = dbMethod

            it.copy(
                methodMappings = methodMappings
            )
        }
    }

    fun toggleMethodSelected(id: Long) {
        _primaryState.update {
            val selectedTempMethods = it.selectedTempMethods.toMutableSet()

            if (selectedTempMethods.contains(id)) {
                selectedTempMethods.remove(id)
            } else {
                selectedTempMethods.add(id)
            }

            it.copy(
                selectedTempMethods = selectedTempMethods,
                selectedTempMethodCount = numberFormatter.format(selectedTempMethods.size)
                    .toString()
            )
        }
    }

    //endregion

    //region Account

    fun setSelectedAccount(tempAccountId: Long, dbAccount: AccountWithTransactionMetadata) {
        _primaryState.update {
            val accountMappings = it.accountMappings.toMutableMap()
            accountMappings[tempAccountId] = dbAccount

            val restoreBalanceAccounts = it.restoreBalanceAccounts.toMutableSet()
            restoreBalanceAccounts.remove(tempAccountId)

            it.copy(
                accountMappings = accountMappings,
                restoreBalanceAccounts = restoreBalanceAccounts
            )
        }
    }

    fun toggleAccountSelected(id: Long) {
        _primaryState.update {
            val selectedTempAccounts = it.selectedTempAccounts.toMutableSet()

            if (selectedTempAccounts.contains(id)) {
                selectedTempAccounts.remove(id)
            } else {
                selectedTempAccounts.add(id)
            }

            it.copy(
                selectedTempAccounts = selectedTempAccounts,
                selectedTempAccountCount = numberFormatter.format(selectedTempAccounts.size)
                    .toString()
            )
        }
    }

    fun toggleRestoreBalance(tempAccountId: Long) {
        _primaryState.update {
            val restoreBalanceAccounts = it.restoreBalanceAccounts.toMutableSet()

            if (!restoreBalanceAccounts.contains(tempAccountId)) {
                val mappedAccount = it.accountMappings[tempAccountId]
                val sameMappedAccountIds = it.accountMappings
                    .filter { entry ->
                        entry.key != tempAccountId && entry.value.accountId == mappedAccount?.accountId
                    }
                    .keys
                restoreBalanceAccounts.removeAll(sameMappedAccountIds)
                restoreBalanceAccounts.add(tempAccountId)
            } else {
                restoreBalanceAccounts.remove(tempAccountId)
            }

            it.copy(
                restoreBalanceAccounts = restoreBalanceAccounts
            )
        }
    }

    //endregion

    fun checkPageValidity(currentPage: Int) = viewModelScope.launch {
        val mainState = _primaryState.value
        var eventToSend: Event? = null
        if (currentPage == 0) {
            val atLeastOneRestoreCategorySelected = mainState.selectedTempCategories.isNotEmpty()
            if (atLeastOneRestoreCategorySelected) {
                if (
                    mainState.tempCategories
                        .filter { tempCategory ->
                            mainState.selectedTempCategories.contains(tempCategory.tempCategoryId) && mainState.conflictingTempCategories.contains(
                                tempCategory.tempCategoryId
                            )
                        }
                        .any { tempCategory ->
                            val mappedCategory =
                                mainState.categoryMappings[tempCategory.tempCategoryId]
                            mappedCategory != null && mappedCategory.categoryId == 0L
                        }
                ) {
                    eventToSend = Event.MappingInvalid
                }
            } else {
                eventToSend = Event.CategoryMapRequired
            }
        }
        if (
            currentPage == 1 &&
            mainState.tempCounterParties
                .filter { tempCounterParty ->
                    mainState.selectedTempCounterParties.contains(tempCounterParty.tempCounterPartyId) && mainState.conflictingTempCounterParties.contains(
                        tempCounterParty.tempCounterPartyId
                    )
                }
                .any { tempCounterParty ->
                    val mappedCounterParty =
                        mainState.counterPartyMappings[tempCounterParty.tempCounterPartyId]
                    mappedCounterParty != null && mappedCounterParty.counterPartyId == 0L
                }
        ) {
            eventToSend = Event.MappingInvalid
        }
        if (currentPage == 2) {
            val atLeastOneRestoreMethodSelected = mainState.selectedTempMethods.isNotEmpty()
            if (atLeastOneRestoreMethodSelected) {
                if (
                    mainState.tempMethods
                        .filter { tempMethod ->
                            mainState.selectedTempMethods.contains(tempMethod.tempMethodId) && mainState.conflictingTempMethods.contains(
                                tempMethod.tempMethodId
                            )
                        }
                        .any { tempMethod ->
                            val mappedMethod = mainState.methodMappings[tempMethod.tempMethodId]
                            mappedMethod != null && mappedMethod.methodId == 0L
                        }
                ) {
                    eventToSend = Event.MappingInvalid
                }
            } else {
                eventToSend = Event.MethodMapRequired
            }
        }
        if (currentPage == 3) {
            val atLeastOneRestoreAccountSelected = mainState.selectedTempAccounts.isNotEmpty()
            if (atLeastOneRestoreAccountSelected) {
                if (
                    mainState.tempAccounts
                        .filter { tempAccount ->
                            mainState.selectedTempAccounts.contains(tempAccount.tempAccountId) && mainState.conflictingTempAccounts.contains(
                                tempAccount.tempAccountId
                            )
                        }
                        .any { tempAccount ->
                            val mappedAccount = mainState.accountMappings[tempAccount.tempAccountId]
                            mappedAccount != null && mappedAccount.accountId == 0L
                        }
                ) {
                    eventToSend = Event.MappingInvalid
                }
            } else {
                eventToSend = Event.AccountMapRequired
            }
        }
        _event.send(eventToSend)
    }

    fun toggleLocalDateSelected(
        localDateSelected: Boolean,
        transactions: List<TransactionWithChips>
    ) {
        _primaryState.update {
            val transactionIds = transactions
                .map { transactionWithChips -> transactionWithChips.id }
                .toSet()

            val selectedTempTransactions = it.selectedTempTransactions.toMutableSet()

            if (localDateSelected) {
                selectedTempTransactions.removeAll(transactionIds)
            } else {
                selectedTempTransactions.addAll(transactionIds)
            }

            it.copy(
                selectedTempTransactions = selectedTempTransactions
            )
        }
    }

    fun toggleTransactionSelected(id: Long) {
        _primaryState.update {
            val selectedTempTransactions = it.selectedTempTransactions.toMutableSet()

            if (selectedTempTransactions.contains(id)) {
                selectedTempTransactions.remove(id)
            } else {
                selectedTempTransactions.add(id)
            }

            it.copy(
                selectedTempTransactions = selectedTempTransactions
            )
        }
    }

    fun toggleSelection(allSelected: Boolean) {
        _primaryState.update {
            val selectedTempTransactions = it.selectedTempTransactions.toMutableSet()

            if (allSelected) {
                selectedTempTransactions.clear()
            } else {
                val transactionIds = it.filteredTransactions
                    .values
                    .flatten()
                    .map { transactionWithChips -> transactionWithChips.id }

                selectedTempTransactions.addAll(transactionIds)
            }

            it.copy(
                selectedTempTransactions = selectedTempTransactions
            )
        }
    }

    fun setSelectedCurrencies(selectedCurrencies: Set<String>) {
        _filtersState.update {
            it.copy(
                selectedCurrencies = selectedCurrencies,
                selectedCurrencyCount = numberFormatter.format(selectedCurrencies.size).toString()
            )
        }
    }

    fun setStartDateAndEndDate(startDate: ZonedDateTime?, endDate: ZonedDateTime?) {
        _filtersState.update {
            it.copy(
                startDate = startDate,
                endDate = endDate,
                startDateString = startDate?.getDateString(datePattern) ?: "",
                endDateString = endDate?.getDateString(datePattern) ?: "",
                dateRangeStringRes = if (startDate == null && endDate == null) {
                    R.string.all_time_date_range_label
                } else {
                    R.string.date_range_label
                }
            )
        }
    }

    fun setSelectedTransactionTypes(selectedTransactionTypes: List<TransactionType>) {
        _filtersState.update {
            it.copy(
                selectedTransactionTypes = selectedTransactionTypes
            )
        }
    }

    fun setSortDirection(sortDirection: SortDirection) {
        _sortConfigState.update {
            it.copy(
                sortDirection = sortDirection
            )
        }
    }

    fun setFilterAmounts(min: Double, max: Double) {
        val filterAmountRange = if (min > 0 && max > 0 && min < max) {
            "$min - $max"
        } else if (min > 0 && max == 0.0) {
            "$min+"
        } else if (min == 0.0 && max > 0) {
            "0 - $max"
        } else {
            "0+"
        }
        _filtersState.update {
            it.copy(
                filterAmountMin = min,
                filterAmountMax = max,
                filterAmountRange = filterAmountRange
            )
        }
    }

    fun setSearchText(searchText: String) {
        _searchState.update {
            searchText
        }
    }

    fun setLocale(locale: Locale?) {
        currencyFormatterMap = getCurrencyFormatterMap(locale)
        numberFormatter = getNumberFormatter(locale)
        _primaryState.update {
            it.copy(
                locale = locale
            )
        }
    }
}