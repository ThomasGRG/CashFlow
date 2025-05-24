package jp.ikigai.cash.flow.ui.viewmodels.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import compose.icons.TablerIcons
import compose.icons.tablericons.Alarm
import compose.icons.tablericons.ChartLine
import compose.icons.tablericons.FileText
import compose.icons.tablericons.History
import compose.icons.tablericons.Typography
import io.objectbox.Box
import io.objectbox.BoxStore
import io.objectbox.exception.UniqueViolationException
import io.objectbox.kotlin.boxFor
import io.objectbox.kotlin.flow
import io.objectbox.query.QueryBuilder
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.dto.ChipInfo
import jp.ikigai.cash.flow.data.dto.TemplateWithChips
import jp.ikigai.cash.flow.data.dto.TransactionWithChips
import jp.ikigai.cash.flow.data.dto.export.ExportData
import jp.ikigai.cash.flow.data.enums.TransactionType
import jp.ikigai.cash.flow.data.store.DataStore
import jp.ikigai.cash.flow.data.store.entity.Account
import jp.ikigai.cash.flow.data.store.entity.Account_
import jp.ikigai.cash.flow.data.store.entity.Category
import jp.ikigai.cash.flow.data.store.entity.Category_
import jp.ikigai.cash.flow.data.store.entity.CounterParty
import jp.ikigai.cash.flow.data.store.entity.CounterParty_
import jp.ikigai.cash.flow.data.store.entity.Method
import jp.ikigai.cash.flow.data.store.entity.Method_
import jp.ikigai.cash.flow.data.store.entity.Transaction
import jp.ikigai.cash.flow.data.store.entity.TransactionTemplate
import jp.ikigai.cash.flow.data.store.entity.temp.TempAccount
import jp.ikigai.cash.flow.data.store.entity.temp.TempAccount_
import jp.ikigai.cash.flow.data.store.entity.temp.TempCategory
import jp.ikigai.cash.flow.data.store.entity.temp.TempCategory_
import jp.ikigai.cash.flow.data.store.entity.temp.TempCounterParty
import jp.ikigai.cash.flow.data.store.entity.temp.TempCounterParty_
import jp.ikigai.cash.flow.data.store.entity.temp.TempMethod
import jp.ikigai.cash.flow.data.store.entity.temp.TempMethod_
import jp.ikigai.cash.flow.data.store.entity.temp.TempTransaction
import jp.ikigai.cash.flow.data.store.entity.temp.TempTransactionTemplate
import jp.ikigai.cash.flow.data.store.entity.temp.TempTransactionTemplate_
import jp.ikigai.cash.flow.data.store.entity.temp.TempTransaction_
import jp.ikigai.cash.flow.ui.screenStates.common.SortOptionsState
import jp.ikigai.cash.flow.ui.screenStates.common.restore.ImportBackupScreenFiltersState
import jp.ikigai.cash.flow.ui.screenStates.common.restore.ImportBackupScreenPrimaryState
import jp.ikigai.cash.flow.ui.screenStates.common.restore.ImportBackupScreenSecondaryState
import jp.ikigai.cash.flow.utils.getCurrencyFormatterMap
import jp.ikigai.cash.flow.utils.getDateString
import jp.ikigai.cash.flow.utils.getHighlightedString
import jp.ikigai.cash.flow.utils.getIconForCategory
import jp.ikigai.cash.flow.utils.getNumberFormatter
import jp.ikigai.cash.flow.utils.toEpochMilli
import jp.ikigai.cash.flow.utils.toZonedDateTime
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
import kotlinx.coroutines.flow.onCompletion
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
import kotlin.random.Random

class ImportBackupScreenViewModel(
    private val store: BoxStore = DataStore.store
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

    private val _sortOptionsState = MutableStateFlow(
        SortOptionsState<TempTransaction>(
            sortField = TempTransaction_.time
        )
    )
    val sortOptionsState: StateFlow<SortOptionsState<TempTransaction>> =
        _sortOptionsState.asStateFlow()

    private val _filtersState = MutableStateFlow(ImportBackupScreenFiltersState())
    val filtersState: StateFlow<ImportBackupScreenFiltersState> = _filtersState.asStateFlow()

    //endregion

    private val _event: Channel<Event?> = Channel(Int.MAX_VALUE)
    val event: Flow<Event?> = _event.receiveAsFlow()

    //region Boxes

    private val accountBox: Box<Account> = store.boxFor()
    private val tempAccountBox: Box<TempAccount> = store.boxFor()

    private val categoryBox: Box<Category> = store.boxFor()
    private val tempCategoryBox: Box<TempCategory> = store.boxFor()

    private val counterPartyBox: Box<CounterParty> = store.boxFor()
    private val tempCounterPartyBox: Box<TempCounterParty> = store.boxFor()

    private val methodBox: Box<Method> = store.boxFor()
    private val tempMethodBox: Box<TempMethod> = store.boxFor()

    private val templateBox: Box<TransactionTemplate> = store.boxFor()
    private val tempTemplateBox: Box<TempTransactionTemplate> = store.boxFor()

    private val transactionBox: Box<Transaction> = store.boxFor()
    private val tempTransactionBox: Box<TempTransaction> = store.boxFor()

    //endregion

    //region Queries

    private val accountQuery = accountBox
        .query()
        .orderDesc(Account_.frequency)
        .build()

    private val tempAccountQuery = tempAccountBox
        .query()
        .orderDesc(TempAccount_.frequency)
        .build()

    private val categoryQuery = categoryBox
        .query()
        .orderDesc(Category_.frequency)
        .build()

    private val tempCategoryQuery = tempCategoryBox
        .query()
        .orderDesc(TempCategory_.frequency)
        .build()

    private val counterPartyQuery = counterPartyBox
        .query()
        .orderDesc(CounterParty_.frequency)
        .build()

    private val tempCounterPartyQuery = tempCounterPartyBox
        .query()
        .orderDesc(TempCounterParty_.frequency)
        .build()

    private val methodQuery = methodBox
        .query()
        .orderDesc(Method_.frequency)
        .build()

    private val tempMethodQuery = tempMethodBox
        .query()
        .orderDesc(TempMethod_.frequency)
        .build()

    private val tempTransactionTemplatesQuery = tempTemplateBox
        .query()
        .orderDesc(TempTransactionTemplate_.frequency)
        .build()

    //endregion

    override fun onCleared() {
        super.onCleared()
        _event.close()
        accountQuery.close()
        tempAccountQuery.close()
        categoryQuery.close()
        tempCategoryQuery.close()
        counterPartyQuery.close()
        tempCounterPartyQuery.close()
        methodQuery.close()
        tempMethodQuery.close()
        tempTransactionTemplatesQuery.close()
    }

    private fun clearTempData() {
        tempAccountBox.removeAll()
        tempCategoryBox.removeAll()
        tempCounterPartyBox.removeAll()
        tempMethodBox.removeAll()
        tempTemplateBox.removeAll()
        tempTransactionBox.removeAll()
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

                    val tempAccountMap: MutableMap<Long, Long> = mutableMapOf()
                    val tempCategoryMap: MutableMap<Long, Long> = mutableMapOf()
                    val tempCounterPartyMap: MutableMap<Long, Long> = mutableMapOf()
                    val tempMethodMap: MutableMap<Long, Long> = mutableMapOf()

                    store.runInTx {
                        clearTempData()

                        data?.accounts?.forEach { account ->
                            val tempAccount = TempAccount(
                                name = account.name,
                                currency = account.currency,
                                balance = account.balance,
                                frequency = account.frequency,
                                lastUsed = account.lastUsed.toZonedDateTime()
                            )
                            tempAccountMap[account.id] = tempAccountBox.put(tempAccount)
                        }

                        data?.categories?.forEach { category ->
                            val tempCategory = TempCategory(
                                name = category.name,
                                icon = category.iconName.getIconForCategory(),
                                frequency = category.frequency,
                                lastUsed = category.lastUsed.toZonedDateTime()
                            )
                            tempCategoryMap[category.id] = tempCategoryBox.put(tempCategory)
                        }

                        data?.counterParties?.forEach { counterParty ->
                            val tempCounterParty = TempCounterParty(
                                name = counterParty.name,
                                frequency = counterParty.frequency,
                                lastUsed = counterParty.lastUsed.toZonedDateTime()
                            )
                            tempCounterPartyMap[counterParty.id] =
                                tempCounterPartyBox.put(tempCounterParty)
                        }

                        data?.methods?.forEach { method ->
                            val tempMethod = TempMethod(
                                name = method.name,
                                frequency = method.frequency,
                                lastUsed = method.lastUsed.toZonedDateTime()
                            )
                            tempMethodMap[method.id] = tempMethodBox.put(tempMethod)
                        }

                        data?.transactions?.forEach { transaction ->
                            val tempTransaction = TempTransaction(
                                title = transaction.title,
                                description = transaction.description,
                                amount = transaction.amount,
                                type = TransactionType.values()
                                    .find { transactionType -> transactionType.id == transaction.typeId }
                                    ?: TransactionType.DEBIT,
                                currency = transaction.currency,
                                time = transaction.time.toZonedDateTime()
                            )
                            tempTransaction.account.targetId =
                                tempAccountMap[transaction.accountId] ?: 0L
                            tempTransaction.category.targetId =
                                tempCategoryMap[transaction.categoryId] ?: 0L
                            tempTransaction.counterParty.targetId =
                                tempCounterPartyMap[transaction.counterPartyId] ?: 0L
                            tempTransaction.method.targetId =
                                tempMethodMap[transaction.methodId] ?: 0L
                            tempTransactionBox.put(tempTransaction)
                        }

                        data?.templates?.forEach { template ->
                            val tempTemplate = TempTransactionTemplate(
                                name = template.name,
                                title = template.title,
                                description = template.description,
                                amount = template.amount,
                                type = TransactionType.values()
                                    .find { transactionType -> transactionType.id == template.typeId }
                                    ?: TransactionType.DEBIT,
                                frequency = template.frequency,
                                lastUsed = template.lastUsed.toZonedDateTime()
                            )
                            tempTemplate.account.targetId = tempAccountMap[template.accountId] ?: 0L
                            tempTemplate.category.targetId =
                                tempCategoryMap[template.categoryId] ?: 0L
                            tempTemplate.counterParty.targetId =
                                tempCounterPartyMap[template.counterPartyId] ?: 0L
                            tempTemplate.method.targetId = tempMethodMap[template.methodId] ?: 0L
                            tempTemplateBox.put(tempTemplate)
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
        val accounts = accountQuery.find().map { account ->
            val formatter = currencyFormatterMap.getValue(account.currency)
            account.copy(
                formattedBalance = formatter.format(account.balance).toString()
            )
        }
        val categories = categoryQuery.find()
        val counterParties = counterPartyQuery.find()
        val methods = methodQuery.find()

        val tempAccounts = tempAccountQuery.find().map { tempAccount ->
            val formatter = currencyFormatterMap.getValue(tempAccount.currency)
            tempAccount.copy(
                formattedBalance = formatter.format(tempAccount.balance).toString()
            )
        }
        val tempCategories = tempCategoryQuery.find()
        val tempCounterParties = tempCounterPartyQuery.find()
        val tempMethods = tempMethodQuery.find()
        val tempTransactionTemplates = tempTransactionTemplatesQuery.find()

        val conflictingTempAccounts = tempAccounts
            .filter { tempAccount ->
                val dbAccount = accounts.find { it.name == tempAccount.name }
                dbAccount != null
            }
            .map { tempAccount -> tempAccount.id }

        val conflictingTempCategories = tempCategories
            .filter { tempCategory ->
                val dbCategory = categories.find { it.name == tempCategory.name }
                dbCategory != null
            }
            .map { tempCategory -> tempCategory.id }

        val conflictingTempCounterParties = tempCounterParties
            .filter { tempCounterParty ->
                val dbCounterParty = counterParties.find { it.name == tempCounterParty.name }
                dbCounterParty != null
            }
            .map { tempCounterParty -> tempCounterParty.id }

        val conflictingTempMethods = tempMethods
            .filter { tempMethod ->
                val dbMethod = methods.find { it.name == tempMethod.name }
                dbMethod != null
            }
            .map { tempMethod -> tempMethod.id }

        val accountMappings = tempAccounts.associateBy(
            { tempAccount ->
                tempAccount.id
            },
            { tempAccount ->
                val dbAccount = accounts.find { account ->
                    account.name == tempAccount.name
                }
                dbAccount ?: Account()
            }
        )

        val categoryMappings = tempCategories.associateBy(
            { tempCategory ->
                tempCategory.id
            },
            { tempCategory ->
                val dbCategory = categories.find { category ->
                    category.name == tempCategory.name
                }
                dbCategory ?: Category()
            }
        )

        val counterPartyMappings = tempCounterParties.associateBy(
            { tempCounterParty ->
                tempCounterParty.id
            },
            { tempCounterParty ->
                val dbCounterParty = counterParties.find { counterParty ->
                    counterParty.name == tempCounterParty.name
                }
                dbCounterParty ?: CounterParty()
            }
        )

        val methodMappings = tempMethods.associateBy(
            { tempMethod ->
                tempMethod.id
            },
            { tempMethod ->
                val dbMethod = methods.find { method ->
                    method.name == tempMethod.name
                }
                dbMethod ?: Method()
            }
        )

        val currencyAccountMap = accounts.groupBy { account -> account.currency }

        val selectedTempAccounts = tempAccounts
            .map { it.id }
            .toSet()

        val selectedTempCategories = tempCategories
            .map { it.id }
            .toSet()

        val selectedTempCounterParties = tempCounterParties
            .map { it.id }
            .toSet()

        val selectedTempMethods = tempMethods
            .map { it.id }
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
            _sortOptionsState
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
        ) { searchText, sortOptions, filters ->
            Triple(searchText, sortOptions, filters)
        }.flatMapLatest { (searchText, sortOptions, filters) ->
            getTempTransactionQuery(
                searchText,
                sortOptions,
                filters
            )
        }.collectLatest { tempTransactions ->
            val filteredTransactionsMap = tempTransactions.groupBy(
                keySelector = { tempTransaction -> tempTransaction.time.toLocalDate() },
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
        val categoryMappings = primaryState.value.categoryMappings
            .filter { entry -> selectedTempCategoryIds.contains(entry.key) }
            .toMutableMap()

        val tempCounterParties = primaryState.value.tempCounterParties
        val selectedTempCounterPartyIds = primaryState.value.selectedTempCounterParties
        val counterPartyMappings = primaryState.value.counterPartyMappings
            .filter { entry -> selectedTempCounterPartyIds.contains(entry.key) }
            .toMutableMap()

        val tempMethods = primaryState.value.tempMethods
        val selectedTempMethodIds = primaryState.value.selectedTempMethods
        val methodMappings = primaryState.value.methodMappings
            .filter { entry -> selectedTempMethodIds.contains(entry.key) }
            .toMutableMap()

        val tempAccounts = primaryState.value.tempAccounts
        val selectedTempAccountIds = primaryState.value.selectedTempAccounts
        val accountMappings = primaryState.value.accountMappings
            .filter { entry -> selectedTempAccountIds.contains(entry.key) }
            .toMutableMap()
        val restoreBalanceAccounts = primaryState.value.restoreBalanceAccounts

        val enabledTempTransactionTemplateIds =
            secondaryState.value.enabledTempTransactionTemplates
        val selectedTempTransactionTemplateIds =
            primaryState.value.selectedTempTransactionTemplates
                .filter { id -> enabledTempTransactionTemplateIds.contains(id) }
        val tempTransactionTemplates = primaryState.value.tempTransactionTemplates
            .filter { tempTransactionTemplate ->
                selectedTempTransactionTemplateIds.contains(
                    tempTransactionTemplate.id
                )
            }

        val enabledTempTransactionIds = secondaryState.value.enabledTempTransactions
        val selectedTempTransactionIds = _primaryState.value.selectedTempTransactions
            .filter { id -> enabledTempTransactionIds.contains(id) }
        val tempTransactions = primaryState.value.tempTransactions
            .filter { tempTransaction -> selectedTempTransactionIds.contains(tempTransaction.id) }

        val epoch = ZonedDateTime.ofInstant(Instant.EPOCH, ZoneId.systemDefault())

        try {
            val renameRequiredTemplates = mutableListOf<TransactionTemplate>()

            tempTransactionTemplates.forEach { tempTransactionTemplate ->
                val account = accountMappings[tempTransactionTemplate.account.targetId]
                val category = categoryMappings[tempTransactionTemplate.category.targetId]
                val counterParty =
                    counterPartyMappings[tempTransactionTemplate.counterParty.targetId]
                val method = methodMappings[tempTransactionTemplate.method.targetId]

                val template = TransactionTemplate(
                    name = tempTransactionTemplate.name,
                    title = tempTransactionTemplate.title,
                    description = tempTransactionTemplate.description,
                    amount = tempTransactionTemplate.amount,
                    type = tempTransactionTemplate.type,
                    frequency = tempTransactionTemplate.frequency,
                    lastUsed = tempTransactionTemplate.lastUsed
                )

                template.account.target = account
                template.category.target = category
                template.counterParty.target = counterParty
                template.method.target = method

                try {
                    templateBox.put(template)
                } catch (exception: UniqueViolationException) {
                    renameRequiredTemplates.add(template)
                }
            }

            store.runInTx {
                categoryMappings
                    .entries
                    .filter { (_, category) -> category.id > 0 }
                    .distinctBy { (_, category) -> category.id }
                    .forEach { (tempCategoryId, category) ->
                        val filteredTransactions = tempTransactions.filter { tempTransaction ->
                            tempTransaction.category.targetId == tempCategoryId
                        }
                        categoryBox.put(
                            category.copy(
                                frequency = category.frequency + filteredTransactions.size,
                                lastUsed = maxOf(
                                    category.lastUsed,
                                    filteredTransactions.maxOfOrNull { tempTransaction ->
                                        tempTransaction.time
                                    } ?: epoch
                                )
                            )
                        )
                    }

                categoryMappings
                    .filter { (_, category) -> category.id == 0L }
                    .forEach { (tempCategoryId, _) ->
                        val tempCategory = tempCategories.find { tempCategory ->
                            tempCategory.id == tempCategoryId
                        }
                        if (tempCategory != null) {
                            val filteredTransactions = tempTransactions.filter { tempTransaction ->
                                tempTransaction.category.targetId == tempCategory.id
                            }
                            val category = Category(
                                name = tempCategory.name,
                                icon = tempCategory.icon,
                                frequency = filteredTransactions.size,
                                lastUsed = filteredTransactions.maxOfOrNull { tempTransaction ->
                                    tempTransaction.time
                                } ?: epoch
                            )
                            categoryMappings[tempCategory.id] = category.copy(
                                id = categoryBox.put(category)
                            )
                        }
                    }

                counterPartyMappings
                    .entries
                    .filter { (_, counterParty) -> counterParty.id > 0 }
                    .distinctBy { (_, counterParty) -> counterParty.id }
                    .forEach { (tempCounterPartyId, counterParty) ->
                        val filteredTransactions = tempTransactions.filter { tempTransaction ->
                            tempTransaction.counterParty.targetId == tempCounterPartyId
                        }
                        counterPartyBox.put(
                            counterParty.copy(
                                frequency = counterParty.frequency + filteredTransactions.size,
                                lastUsed = maxOf(
                                    counterParty.lastUsed,
                                    filteredTransactions.maxOfOrNull { tempTransaction ->
                                        tempTransaction.time
                                    } ?: epoch
                                )
                            )
                        )
                    }

                counterPartyMappings
                    .filter { (_, counterParty) -> counterParty.id == 0L }
                    .forEach { (tempCounterPartyId, _) ->
                        val tempCounterParty = tempCounterParties.find { tempCounterParty ->
                            tempCounterParty.id == tempCounterPartyId
                        }
                        if (tempCounterParty != null) {
                            val filteredTransactions = tempTransactions.filter { tempTransaction ->
                                tempTransaction.counterParty.targetId == tempCounterParty.id
                            }
                            val counterParty = CounterParty(
                                name = tempCounterParty.name,
                                frequency = filteredTransactions.size,
                                lastUsed = filteredTransactions.maxOfOrNull { tempTransaction ->
                                    tempTransaction.time
                                } ?: epoch
                            )
                            counterPartyMappings[tempCounterParty.id] = counterParty.copy(
                                id = counterPartyBox.put(counterParty)
                            )
                        }
                    }

                methodMappings
                    .entries
                    .filter { (_, method) -> method.id > 0 }
                    .distinctBy { (_, method) -> method.id }
                    .forEach { (tempMethodId, method) ->
                        val filteredTransactions = tempTransactions.filter { tempTransaction ->
                            tempTransaction.method.targetId == tempMethodId
                        }
                        methodBox.put(
                            method.copy(
                                frequency = method.frequency + filteredTransactions.size,
                                lastUsed = maxOf(
                                    method.lastUsed,
                                    filteredTransactions.maxOfOrNull { tempTransaction ->
                                        tempTransaction.time
                                    } ?: epoch
                                )
                            )
                        )
                    }

                methodMappings
                    .filter { (_, method) -> method.id == 0L }
                    .forEach { (tempMethodId, _) ->
                        val tempMethod = tempMethods.find { tempMethod ->
                            tempMethod.id == tempMethodId
                        }
                        if (tempMethod != null) {
                            val filteredTransactions = tempTransactions.filter { tempTransaction ->
                                tempTransaction.method.targetId == tempMethod.id
                            }
                            val method = Method(
                                name = tempMethod.name,
                                frequency = filteredTransactions.size,
                                lastUsed = filteredTransactions.maxOfOrNull { tempTransaction ->
                                    tempTransaction.time
                                } ?: epoch
                            )
                            methodMappings[tempMethod.id] = method.copy(
                                id = methodBox.put(method)
                            )
                        }
                    }

                accountMappings
                    .entries
                    .filter { (_, account) -> account.id > 0 }
                    .distinctBy { (_, account) -> account.id }
                    .forEach { (tempAccountId, account) ->
                        val filteredTransactions = tempTransactions.filter { tempTransaction ->
                            tempTransaction.account.targetId == tempAccountId
                        }
                        accountBox.put(
                            account.copy(
                                frequency = account.frequency + filteredTransactions.size,
                                lastUsed = maxOf(
                                    account.lastUsed,
                                    filteredTransactions.maxOfOrNull { tempTransaction ->
                                        tempTransaction.time
                                    } ?: epoch
                                )
                            )
                        )
                    }

                accountMappings
                    .filter { (tempAccountId, _) -> restoreBalanceAccounts.contains(tempAccountId) }
                    .forEach { (tempAccountId, account) ->
                        val tempAccount = tempAccounts.find { tempAccount ->
                            tempAccount.id == tempAccountId
                        }
                        if (tempAccount != null) {
                            accountBox.put(
                                account.copy(
                                    balance = tempAccount.balance
                                )
                            )
                        }
                    }

                accountMappings
                    .filter { (_, account) -> account.id == 0L }
                    .forEach { (tempAccountId, _) ->
                        val tempAccount = tempAccounts.find { tempAccount ->
                            tempAccount.id == tempAccountId
                        }
                        if (tempAccount != null) {
                            val filteredTransactions = tempTransactions.filter { tempTransaction ->
                                tempTransaction.account.targetId == tempAccount.id
                            }
                            val account = Account(
                                name = tempAccount.name,
                                balance = tempAccount.balance,
                                currency = tempAccount.currency,
                                frequency = filteredTransactions.size,
                                lastUsed = filteredTransactions.maxOfOrNull { tempTransaction ->
                                    tempTransaction.time
                                } ?: epoch
                            )
                            accountMappings[tempAccount.id] = account.copy(
                                id = accountBox.put(account)
                            )
                        }
                    }

                renameRequiredTemplates.forEach { template ->
                    templateBox.put(
                        template.copy(
                            name = "${template.name} - ${Random.nextInt(0, 100)}"
                        )
                    )
                }

                tempTransactions.forEach { tempTransaction ->
                    val account = accountMappings[tempTransaction.account.targetId]
                    val category = categoryMappings[tempTransaction.category.targetId]
                    val counterParty =
                        counterPartyMappings[tempTransaction.counterParty.targetId]
                    val method = methodMappings[tempTransaction.method.targetId]

                    if (account != null && category != null && method != null) {
                        val transaction = Transaction(
                            title = tempTransaction.title,
                            description = tempTransaction.description,
                            amount = tempTransaction.amount,
                            time = tempTransaction.time,
                            currency = tempTransaction.currency,
                            type = tempTransaction.type
                        )

                        transaction.account.target = account
                        transaction.category.target = category
                        transaction.counterParty.target = counterParty
                        transaction.method.target = method

                        transactionBox.put(transaction)
                    }
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

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getTempTransactionQuery(
        searchText: String,
        sortOptions: SortOptionsState<TempTransaction>,
        filters: ImportBackupScreenFiltersState
    ): Flow<MutableList<TempTransaction>> {
        val transactionQueryBuilder = tempTransactionBox
            .query(
                TempTransaction_.time.between(
                    filters.startDate?.toEpochMilli() ?: 0L,
                    filters.endDate?.toEpochMilli() ?: Long.MAX_VALUE
                )
                    .and(
                        TempTransaction_.currency.oneOf(filters.selectedCurrencies.toTypedArray())
                    )
                    .and(
                        TempTransaction_.amount.between(
                            filters.filterAmountMin,
                            if (filters.filterAmountMax <= filters.filterAmountMin) Double.MAX_VALUE else filters.filterAmountMax
                        )
                    )
                    .and(
                        TempTransaction_.type.oneOf(
                            filters.selectedTransactionTypes.toIntArray()
                        )
                    )
            )
        val query = if (searchText.isNotBlank()) {
            transactionQueryBuilder
                .and()
                .apply(
                    TempTransaction_.title.contains(
                        searchText.trim(),
                        QueryBuilder.StringOrder.CASE_INSENSITIVE
                    )
                        .or(
                            TempTransaction_.description.contains(
                                searchText.trim(),
                                QueryBuilder.StringOrder.CASE_INSENSITIVE
                            )
                        )
                )
                .order(sortOptions.sortField, sortOptions.sortFlags)
                .build()
        } else {
            transactionQueryBuilder
                .order(sortOptions.sortField, sortOptions.sortFlags)
                .build()
        }
        return query.flow().onCompletion {
            query.close()
        }
    }

    private fun getTempTransactionWithChips(
        tempTransaction: TempTransaction,
        searchText: String
    ): TransactionWithChips {
        val account = tempTransaction.account.target
        val category = tempTransaction.category.target
        val counterParty = tempTransaction.counterParty.target
        val method = tempTransaction.method.target

        val currencyFormatter = currencyFormatterMap.getValue(account.currency)

        val chips: MutableList<ChipInfo> = mutableListOf()

        if (counterParty != null) {
            chips.add(
                ChipInfo(
                    icon = Constants.DEFAULT_COUNTERPARTY_ICON,
                    value = counterParty.name,
                    resId = R.string.placeholder
                )
            )
        }
        chips.add(
            ChipInfo(
                icon = category.icon,
                value = category.name,
                resId = R.string.placeholder
            )
        )
        chips.add(
            ChipInfo(
                icon = Constants.DEFAULT_METHOD_ICON,
                value = method.name,
                resId = R.string.placeholder
            )
        )
        chips.add(
            ChipInfo(
                icon = Constants.DEFAULT_ACCOUNT_ICON,
                value = account.name,
                resId = R.string.placeholder
            )
        )
        chips.add(
            ChipInfo(
                icon = TablerIcons.Alarm,
                value = tempTransaction.time.format(DateTimeFormatter.ofPattern("hh:mm a")),
                resId = R.string.placeholder
            )
        )
        return TransactionWithChips(
            id = tempTransaction.id,
            annotatedTitle = getHighlightedString(tempTransaction.title, searchText),
            annotatedDescription = getHighlightedString(tempTransaction.description, searchText),
            amount = currencyFormatter.format(tempTransaction.amount).toString(),
            typeIcon = tempTransaction.type.icon,
            typeIconColor = tempTransaction.type.color,
            currency = tempTransaction.currency,
            chips = chips
        )
    }

    private fun getEnabledTempTransactions(
        selectedTempAccounts: Set<Long>,
        selectedTempCategories: Set<Long>,
        selectedTempCounterParties: Set<Long>,
        selectedTempMethods: Set<Long>,
        tempTransactions: List<TempTransaction>,
    ): Set<Long> {
        return tempTransactions
            .filter { tempTransaction ->
                val accountValid = selectedTempAccounts.contains(tempTransaction.account.targetId)
                val categoryValid =
                    selectedTempCategories.contains(tempTransaction.category.targetId)
                val counterPartyValid =
                    tempTransaction.counterParty.targetId == 0L || selectedTempCounterParties.contains(
                        tempTransaction.counterParty.targetId
                    )
                val methodValid = selectedTempMethods.contains(tempTransaction.method.targetId)
                accountValid && categoryValid && counterPartyValid && methodValid
            }
            .map { tempTransaction -> tempTransaction.id }
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
        tempTransactionTemplates: List<TempTransactionTemplate>,
    ): Set<Long> {
        return tempTransactionTemplates
            .filter { tempTransactionTemplate ->
                val accountValid =
                    tempTransactionTemplate.account.targetId == 0L || selectedTempAccounts.contains(
                        tempTransactionTemplate.account.targetId
                    )
                val categoryValid =
                    tempTransactionTemplate.category.targetId == 0L || selectedTempCategories.contains(
                        tempTransactionTemplate.category.targetId
                    )
                val counterPartyValid =
                    tempTransactionTemplate.counterParty.targetId == 0L || selectedTempCounterParties.contains(
                        tempTransactionTemplate.counterParty.targetId
                    )
                val methodValid =
                    tempTransactionTemplate.method.targetId == 0L || selectedTempMethods.contains(
                        tempTransactionTemplate.method.targetId
                    )
                accountValid && categoryValid && counterPartyValid && methodValid
            }
            .map { tempTransactionTemplate -> tempTransactionTemplate.id }
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
        tempTransactionTemplates: List<TempTransactionTemplate>
    ): List<TemplateWithChips> {
        val hasBeenUsedComparator = ZonedDateTime.ofInstant(Instant.EPOCH, ZoneId.systemDefault())
        return tempTransactionTemplates.map { tempTransactionTemplate ->
            val account = tempTransactionTemplate.account.target
            val category = tempTransactionTemplate.category.target
            val counterParty = tempTransactionTemplate.counterParty.target
            val method = tempTransactionTemplate.method.target

            val chips: MutableList<ChipInfo> = mutableListOf()

            if (tempTransactionTemplate.title.isNotEmpty()) {
                chips.add(
                    ChipInfo(
                        resId = R.string.placeholder,
                        value = tempTransactionTemplate.title,
                        icon = TablerIcons.Typography
                    )
                )
            }
            if (tempTransactionTemplate.description.isNotEmpty()) {
                chips.add(
                    ChipInfo(
                        resId = R.string.placeholder,
                        value = tempTransactionTemplate.description,
                        icon = TablerIcons.FileText
                    )
                )
            }
            if (category != null) {
                chips.add(
                    ChipInfo(
                        resId = R.string.placeholder,
                        value = category.name,
                        icon = category.icon
                    )
                )
            }
            if (counterParty != null) {
                chips.add(
                    ChipInfo(
                        resId = R.string.placeholder,
                        value = counterParty.name,
                        icon = Constants.DEFAULT_COUNTERPARTY_ICON
                    )
                )
            }
            if (method != null) {
                chips.add(
                    ChipInfo(
                        resId = R.string.placeholder,
                        value = method.name,
                        icon = Constants.DEFAULT_METHOD_ICON
                    )
                )
            }
            if (account != null) {
                chips.add(
                    ChipInfo(
                        resId = R.string.placeholder,
                        value = account.name,
                        icon = Constants.DEFAULT_ACCOUNT_ICON
                    )
                )
            }
            chips.add(
                ChipInfo(
                    resId = R.string.frequency_of_use_label,
                    value = numberFormatter.format(tempTransactionTemplate.frequency).toString(),
                    icon = TablerIcons.ChartLine
                )
            )
            if (tempTransactionTemplate.lastUsed > hasBeenUsedComparator) {
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
            val formattedAmount = if (tempTransactionTemplate.amount > 0) {
                if (account != null) {
                    currencyFormatterMap.getValue(account.currency)
                        .format(tempTransactionTemplate.amount)
                        .toString()
                } else {
                    numberFormatter.format(tempTransactionTemplate.amount).toString()
                }
            } else {
                ""
            }
            TemplateWithChips(
                id = tempTransactionTemplate.id,
                annotatedName = getHighlightedString(tempTransactionTemplate.name, ""),
                amount = formattedAmount,
                typeIcon = tempTransactionTemplate.type.icon,
                typeIconColor = tempTransactionTemplate.type.color,
                chips = chips
            )
        }
    }

    //endregion

    //region Category

    fun setCategoryMapping(tempCategoryId: Long, dbCategory: Category) {
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

    fun setSelectedCounterParty(tempCounterPartyId: Long, dbCounterParty: CounterParty) {
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

    fun setSelectedMethod(tempMethodId: Long, dbMethod: Method) {
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

    fun setSelectedAccount(tempAccountId: Long, dbAccount: Account) {
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
                        entry.key != tempAccountId && entry.value.id == mappedAccount?.id
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
                            mainState.selectedTempCategories.contains(tempCategory.id) && mainState.conflictingTempCategories.contains(
                                tempCategory.id
                            )
                        }
                        .any { tempCategory ->
                            val mappedCategory = mainState.categoryMappings[tempCategory.id]
                            mappedCategory != null && mappedCategory.id == 0L
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
                    mainState.selectedTempCounterParties.contains(tempCounterParty.id) && mainState.conflictingTempCounterParties.contains(
                        tempCounterParty.id
                    )
                }
                .any { tempCounterParty ->
                    val mappedCounterParty = mainState.counterPartyMappings[tempCounterParty.id]
                    mappedCounterParty != null && mappedCounterParty.id == 0L
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
                            mainState.selectedTempMethods.contains(tempMethod.id) && mainState.conflictingTempMethods.contains(
                                tempMethod.id
                            )
                        }
                        .any { tempMethod ->
                            val mappedMethod = mainState.methodMappings[tempMethod.id]
                            mappedMethod != null && mappedMethod.id == 0L
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
                            mainState.selectedTempAccounts.contains(tempAccount.id) && mainState.conflictingTempAccounts.contains(
                                tempAccount.id
                            )
                        }
                        .any { tempAccount ->
                            val mappedAccount = mainState.accountMappings[tempAccount.id]
                            mappedAccount != null && mappedAccount.id == 0L
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

    fun setSelectedTransactionTypes(selectedTransactionTypes: List<Int>) {
        _filtersState.update {
            it.copy(
                selectedTransactionTypes = selectedTransactionTypes
            )
        }
    }

    fun setSortFlags(sortFlags: Int) {
        _sortOptionsState.update {
            it.copy(
                sortFlags = sortFlags
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