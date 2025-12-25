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
import jp.ikigai.cash.flow.CashFlowDatabase
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.TransactionWithMetadata
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.Database
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.dto.ChipInfo
import jp.ikigai.cash.flow.data.dto.TransactionWithChips
import jp.ikigai.cash.flow.data.dto.export.AccountExport
import jp.ikigai.cash.flow.data.dto.export.CategoryExport
import jp.ikigai.cash.flow.data.dto.export.CommonExport
import jp.ikigai.cash.flow.data.dto.export.ExportData
import jp.ikigai.cash.flow.data.dto.export.TemplateExport
import jp.ikigai.cash.flow.data.dto.export.TransactionExport
import jp.ikigai.cash.flow.data.enums.SortDirection
import jp.ikigai.cash.flow.data.enums.TransactionType
import jp.ikigai.cash.flow.ui.screenStates.common.ExportTransactionsScreenState
import jp.ikigai.cash.flow.ui.screenStates.common.SortConfigState
import jp.ikigai.cash.flow.ui.screenStates.common.TransactionFilters
import jp.ikigai.cash.flow.utils.getCurrencyFormatterMap
import jp.ikigai.cash.flow.utils.getDateString
import jp.ikigai.cash.flow.utils.getHighlightedString
import jp.ikigai.cash.flow.utils.getNumberFormatter
import jp.ikigai.cash.flow.utils.toEpochMilli
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
import okio.sink
import java.io.IOException
import java.io.OutputStream
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class ExportTransactionsScreenViewModel(
    private val database: CashFlowDatabase = Database.database
) : ViewModel() {

    private val datePattern = "dd-LLL-yyyy"

    private var numberFormatter = getNumberFormatter()
    private var currencyFormatterMap = getCurrencyFormatterMap()

    private var loadDataJob: Job? = null
    private var loadTransactionsJob: Job? = null

    private val _state = MutableStateFlow(ExportTransactionsScreenState())
    val state: StateFlow<ExportTransactionsScreenState> = _state.asStateFlow()

    private val _filtersState = MutableStateFlow(TransactionFilters())
    val filtersState: StateFlow<TransactionFilters> = _filtersState.asStateFlow()

    private val _searchState = MutableStateFlow("")
    val searchState: StateFlow<String> = _searchState.asStateFlow()

    private val _sortConfigState = MutableStateFlow(
        SortConfigState(sortField = "transactionDateTime")
    )
    val sortConfigState: StateFlow<SortConfigState> = _sortConfigState.asStateFlow()

    private val _event: Channel<Event> = Channel(Int.MAX_VALUE)
    val event: Flow<Event> = _event.receiveAsFlow()

    init {
        loadDataJob = loadData()
    }

    override fun onCleared() {
        super.onCleared()
        _event.close()
    }

    private fun loadData() = viewModelScope.launch {
        val totalTransactionsCount = database
            .transactionQueries
            .getTotalCount()
            .executeAsOne()

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

        _state.update {
            it.copy(
                accounts = accounts,
                categories = categories,
                counterParties = counterParties,
                methods = methods,
                totalTransactionsCount = totalTransactionsCount,
            )
        }

        val selectedAccounts = accounts.map { it.accountId }.toSet()
        val selectedCategories = categories.map { it.categoryId }.toSet()
        val selectedCounterParties = counterParties.map { it.counterPartyId }.toSet()
        val selectedMethods = methods.map { it.methodId }.toSet()

        _filtersState.update { filters ->
            filters.copy(
                selectedAccounts = selectedAccounts,
                selectedAccountCount = numberFormatter.format(selectedAccounts.size).toString(),
                selectedCategories = selectedCategories,
                selectedCategoryCount = numberFormatter.format(selectedCategories.size).toString(),
                selectedCounterParties = selectedCounterParties,
                selectedCounterPartyCount = numberFormatter.format(selectedCounterParties.size)
                    .toString(),
                selectedMethods = selectedMethods,
                selectedMethodCount = numberFormatter.format(selectedMethods.size).toString(),
                selectedCurrencyCount = numberFormatter.format(filters.selectedCurrencies.size)
                    .toString()
            )
        }

        loadTransactionsJob = loadTransactions()
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun loadTransactions() = viewModelScope.launch {
        combine(
            _searchState
                .onEach {
                    _state.update {
                        it.copy(
                            loading = true
                        )
                    }
                }
                .debounce(300),
            _sortConfigState
                .onEach {
                    _state.update {
                        it.copy(
                            loading = true
                        )
                    }
                },
            _filtersState
                .onEach {
                    _state.update {
                        it.copy(
                            loading = true
                        )
                    }
                }
        ) { searchText, sortConfig, filters ->
            Triple(searchText, sortConfig, filters)
        }.flatMapLatest { (searchText, sortConfig, filters) ->
            database
                .transactionWithMetadataQueries
                .getTransactions(
                    searchText = searchText.trim(),
                    startDate = filters.startDate ?: ZonedDateTime.ofInstant(
                        Instant.EPOCH,
                        ZoneId.systemDefault()
                    ),
                    endDate = filters.endDate ?: ZonedDateTime.now(ZoneId.systemDefault()),
                    selectedCurrencies = filters.selectedCurrencies,
                    minAmount = filters.filterAmountMin,
                    maxAmount = if (filters.filterAmountMax <= filters.filterAmountMin) Double.MAX_VALUE else filters.filterAmountMax,
                    selectedTypes = filters.selectedTransactionTypes,
                    selectedAccountIds = filters.selectedAccounts,
                    selectedCategoryIds = filters.selectedCategories,
                    selectedCounterPartyIds = if (filters.includeNoCounterPartyTransactions) {
                        filters.selectedCounterParties + 0L
                    } else {
                        filters.selectedCounterParties
                    },
                    selectedMethodIds = filters.selectedMethods,
                    sortField = sortConfig.sortField,
                    sortDirection = sortConfig.sortDirection.name
                )
                .asFlow()
                .mapToList(Dispatchers.IO)
        }.collectLatest { transactions ->
            val transactionsMap = getTransactionsMap(
                transactions,
                searchState.value
            )

            _state.update {
                it.copy(
                    transactionsHashCode = transactions.hashCode(),
                    filteredTransactions = transactionsMap,
                    selectedLocalDates = getSelectedLocalDates(
                        transactionsMap,
                        it.selectedTransactions
                    ),
                    allSelected = getAllSelected(
                        transactionsMap,
                        it.selectedTransactions
                    ),
                    loading = false,
                    enabled = true
                )
            }
        }
    }

    private fun getSelectedLocalDates(
        filteredTransactions: Map<LocalDate, List<TransactionWithChips>>,
        selectedTransactions: Set<Long>
    ): Set<LocalDate> {
        return filteredTransactions
            .filter { entry ->
                val transactionIds =
                    entry.value.map { transactionWithChips -> transactionWithChips.id }
                transactionIds.all { id -> selectedTransactions.contains(id) }
            }
            .keys
    }

    private fun getAllSelected(
        filteredTransactions: Map<LocalDate, List<TransactionWithChips>>,
        selectedTransactions: Set<Long>
    ): Boolean {
        return filteredTransactions
            .values
            .flatten()
            .map { transactionWithChips -> transactionWithChips.id }
            .all { id -> selectedTransactions.contains(id) }
    }

    private fun getTransactionsMap(
        transactions: List<TransactionWithMetadata>,
        searchText: String
    ): Map<LocalDate, List<TransactionWithChips>> {
        val transactionsMap = transactions.groupBy { it.transactionDateTime.toLocalDate() }
        val transactionDetailsMap = mutableMapOf<LocalDate, List<TransactionWithChips>>()
        transactionsMap.forEach { (localDate, transactionsList) ->
            transactionDetailsMap[localDate] = transactionsList.map {
                getTransactionWithChips(it, searchText)
            }
        }
        return transactionDetailsMap
    }

    private fun getTransactionWithChips(
        transaction: TransactionWithMetadata,
        searchText: String
    ): TransactionWithChips {
        val currencyFormatter = currencyFormatterMap.getValue(transaction.transactionCurrency)

        val chips: MutableList<ChipInfo> = mutableListOf()

        if (transaction.transactionCounterPartyName != null) {
            chips.add(
                ChipInfo(
                    icon = Constants.DEFAULT_COUNTERPARTY_ICON,
                    value = transaction.transactionCounterPartyName,
                    resId = R.string.placeholder
                )
            )
        }
        if (transaction.transactionCategoryName != null) {
            chips.add(
                ChipInfo(
                    icon = transaction.transactionCategoryIcon ?: Constants.DEFAULT_CATEGORY_ICON,
                    value = transaction.transactionCategoryName,
                    resId = R.string.placeholder
                )
            )
        }
        if (transaction.transactionMethodName != null) {
            chips.add(
                ChipInfo(
                    icon = Constants.DEFAULT_METHOD_ICON,
                    value = transaction.transactionMethodName,
                    resId = R.string.placeholder
                )
            )
        }
        if (transaction.transactionAccountName != null) {
            chips.add(
                ChipInfo(
                    icon = Constants.DEFAULT_ACCOUNT_ICON,
                    value = transaction.transactionAccountName,
                    resId = R.string.placeholder
                )
            )
        }
        chips.add(
            ChipInfo(
                icon = TablerIcons.Alarm,
                value = transaction.transactionDateTime.format(DateTimeFormatter.ofPattern("hh:mm a")),
                resId = R.string.placeholder
            )
        )
        return TransactionWithChips(
            id = transaction.transactionId,
            annotatedTitle = getHighlightedString(transaction.transactionTitle, searchText),
            annotatedDescription = getHighlightedString(
                transaction.transactionDescription,
                searchText
            ),
            amount = currencyFormatter.format(transaction.transactionAmount).toString(),
            typeIcon = transaction.transactionType.icon,
            typeIconColor = transaction.transactionType.color,
            currency = transaction.transactionCurrency,
            chips = chips
        )
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun export(includeTemplates: Boolean, outputStream: OutputStream?) = viewModelScope.launch {
        loadDataJob?.cancelAndJoin()
        loadTransactionsJob?.cancelAndJoin()
        _state.update {
            it.copy(
                loading = true,
                enabled = false
            )
        }

        var result: Event

        val exportTemplates = if (includeTemplates) getExportTemplates() else emptyList()
        val exportTransactions = getExportTransactions(
            selectedTransactionIds = state.value.selectedTransactions,
            includeTemplates = includeTemplates,
        )
        val exportCategories = getExportCategories(
            exportTransactions.map { it.categoryId }.toSet()
        )
        val exportCounterParties = getExportCounterParties(
            exportTransactions.map { it.counterPartyId }.filter { it > 0 }.toSet()
        )
        val exportMethods = getExportMethods(
            exportTransactions.map { it.methodId }.toSet()
        )
        val exportAccounts = getExportAccounts(
            exportTransactions.map { it.accountId }.toSet()
        )

        val exportData = ExportData(
            transactions = exportTransactions,
            accounts = exportAccounts,
            categories = exportCategories,
            counterParties = exportCounterParties,
            methods = exportMethods,
            templates = exportTemplates
        )

        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        val adapter = moshi.adapter<ExportData>()

        outputStream?.sink()?.buffer().use { bufferedSink ->
            try {
                bufferedSink?.writeUtf8(adapter.indent("    ").serializeNulls().toJson(exportData))
                result = Event.ExportSuccess
            } catch (e: IOException) {
                result = Event.IOError
            }
        }

        _event.send(result)

        _state.update {
            it.copy(
                loading = false
            )
        }
    }

    private fun getExportTransactions(
        selectedTransactionIds: Set<Long>,
        includeTemplates: Boolean
    ): List<TransactionExport> {
        val transactions = database
            .transactionQueries
            .getByIds(selectedTransactionIds)
            .executeAsList()

        return transactions.map {
            TransactionExport(
                id = it.transactionId,
                title = it.transactionTitle,
                description = it.transactionDescription,
                amount = it.transactionAmount,
                type = it.transactionType.name,
                currency = it.transactionCurrency,
                time = it.transactionDateTime.toEpochMilli(),
                accountId = it.transactionAccountId,
                categoryId = it.transactionCategoryId,
                counterPartyId = it.transactionCounterPartyId,
                methodId = it.transactionMethodId,
                templateId = if (includeTemplates) {
                    it.transactionTemplateId
                } else {
                    0L
                },
            )
        }
    }

    private fun getExportTemplates(): List<TemplateExport> {
        val templates = database
            .transactionTemplateQueries
            .getAll()
            .executeAsList()

        return templates.map {
            TemplateExport(
                id = it.templateId,
                name = it.templateName,
                title = it.templateTitle,
                description = it.templateDescription,
                amount = it.templateAmount,
                type = it.templateType.name,
                accountId = it.templateAccountId,
                categoryId = it.templateCategoryId,
                counterPartyId = it.templateCounterPartyId,
                methodId = it.templateMethodId
            )
        }
    }

    private fun getExportAccounts(accountIds: Set<Long>): List<AccountExport> {
        val accounts = database
            .accountQueries
            .getByIds(accountIds)
            .executeAsList()

        return accounts.map {
            AccountExport(
                id = it.accountId,
                name = it.accountName,
                balance = it.balance,
                currency = it.currency
            )
        }
    }

    private fun getExportCategories(categoryIds: Set<Long>): List<CategoryExport> {
        val categories = database
            .categoryQueries
            .getByIds(categoryIds)
            .executeAsList()

        return categories.map {
            CategoryExport(
                id = it.categoryId,
                name = it.categoryName,
                iconName = it.icon.name
            )
        }
    }

    private fun getExportCounterParties(counterPartyIds: Set<Long>): List<CommonExport> {
        val counterParties = database
            .counterPartyQueries
            .getByIds(counterPartyIds)
            .executeAsList()

        return counterParties.map {
            CommonExport(
                id = it.counterPartyId,
                name = it.counterPartyName
            )
        }
    }

    private fun getExportMethods(methodIds: Set<Long>): List<CommonExport> {
        val methods = database
            .methodQueries
            .getByIds(methodIds)
            .executeAsList()

        return methods.map {
            CommonExport(
                id = it.methodId,
                name = it.methodName
            )
        }
    }

    fun toggleLocalDateSelected(localDate: LocalDate) {
        _state.update {
            val transactionIds = it.filteredTransactions
                .getOrDefault(localDate, emptyList())
                .map { transactionWithChips -> transactionWithChips.id }
                .toSet()

            val selectedLocalDates = it.selectedLocalDates.toMutableSet()
            val selectedTransactions = it.selectedTransactions.toMutableSet()

            if (it.selectedLocalDates.contains(localDate)) {
                selectedLocalDates.remove(localDate)
                selectedTransactions.removeAll(transactionIds)
            } else {
                selectedLocalDates.add(localDate)
                selectedTransactions.addAll(transactionIds)
            }

            it.copy(
                selectedLocalDates = selectedLocalDates,
                selectedTransactions = selectedTransactions,
                allSelected = getAllSelected(
                    it.filteredTransactions,
                    selectedTransactions
                )
            )
        }
    }

    fun toggleTransactionSelected(id: Long) {
        _state.update {
            val selectedTransactions = it.selectedTransactions.toMutableSet()

            if (selectedTransactions.contains(id)) {
                selectedTransactions.remove(id)
            } else {
                selectedTransactions.add(id)
            }

            it.copy(
                selectedLocalDates = getSelectedLocalDates(
                    it.filteredTransactions,
                    selectedTransactions
                ),
                selectedTransactions = selectedTransactions,
                allSelected = getAllSelected(
                    it.filteredTransactions,
                    selectedTransactions
                )
            )
        }
    }

    fun toggleSelection() {
        _state.update {
            val selectedTransactions = it.selectedTransactions.toMutableSet()

            if (it.allSelected) {
                selectedTransactions.clear()
            } else {
                val transactionIds = it.filteredTransactions
                    .values
                    .flatten()
                    .map { transactionWithChips -> transactionWithChips.id }
                selectedTransactions.addAll(transactionIds)
            }

            it.copy(
                selectedLocalDates = getSelectedLocalDates(
                    it.filteredTransactions,
                    selectedTransactions
                ),
                selectedTransactions = selectedTransactions,
                allSelected = !it.allSelected
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

    fun setSelectedAccounts(selectedAccounts: Set<Long>) {
        _filtersState.update {
            it.copy(
                selectedAccounts = selectedAccounts,
                selectedAccountCount = numberFormatter.format(selectedAccounts.size).toString()
            )
        }
    }

    fun setSelectedCategories(selectedCategories: Set<Long>) {
        _filtersState.update {
            it.copy(
                selectedCategories = selectedCategories,
                selectedCategoryCount = numberFormatter.format(selectedCategories.size).toString()
            )
        }
    }

    fun setSelectedCounterParties(
        selectedCounterParties: Set<Long>,
        includeNoCounterPartyTransactions: Boolean
    ) {
        _filtersState.update {
            it.copy(
                selectedCounterParties = selectedCounterParties,
                selectedCounterPartyCount = numberFormatter.format(selectedCounterParties.size)
                    .toString(),
                includeNoCounterPartyTransactions = includeNoCounterPartyTransactions
            )
        }
    }

    fun setSelectedMethods(selectedMethods: Set<Long>) {
        _filtersState.update {
            it.copy(
                selectedMethods = selectedMethods,
                selectedMethodCount = numberFormatter.format(selectedMethods.size).toString()
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
        numberFormatter = getNumberFormatter(locale)
        currencyFormatterMap = getCurrencyFormatterMap(locale)
        _state.update {
            it.copy(
                locale = locale
            )
        }
    }
}