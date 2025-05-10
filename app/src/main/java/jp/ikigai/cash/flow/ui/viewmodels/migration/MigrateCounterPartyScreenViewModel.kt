package jp.ikigai.cash.flow.ui.viewmodels.migration

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import compose.icons.TablerIcons
import compose.icons.tablericons.Alarm
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.notifications.ResultsChange
import io.realm.kotlin.query.Sort
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.Database
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.dto.ChipInfo
import jp.ikigai.cash.flow.data.dto.TransactionWithIcons
import jp.ikigai.cash.flow.data.entity.CounterParty
import jp.ikigai.cash.flow.data.entity.Transaction
import jp.ikigai.cash.flow.ui.screenStates.common.SortOptionsScreenState
import jp.ikigai.cash.flow.ui.screenStates.common.TransactionFilters
import jp.ikigai.cash.flow.ui.screenStates.migration.MigrateCounterPartyScreenState
import jp.ikigai.cash.flow.utils.getCurrencyFormatterMap
import jp.ikigai.cash.flow.utils.getDateString
import jp.ikigai.cash.flow.utils.getHighlightedString
import jp.ikigai.cash.flow.utils.getNumberFormatter
import jp.ikigai.cash.flow.utils.toEpochMilli
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
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class MigrateCounterPartyScreenViewModel(
    savedStateHandle: SavedStateHandle,
    private val realm: Realm = Realm.open(Database.config),
) : ViewModel() {

    private val datePattern = "dd-LLL-yyyy"

    private val counterPartyUuid: String = checkNotNull(savedStateHandle["id"])

    private var loadDataJob: Job? = null
    private var loadTransactionsJob: Job? = null

    private var numberFormatter = getNumberFormatter()
    private var currencyFormatterMap = getCurrencyFormatterMap()

    private val _state = MutableStateFlow(MigrateCounterPartyScreenState())
    val state: StateFlow<MigrateCounterPartyScreenState> = _state.asStateFlow()

    private val _filtersState = MutableStateFlow(TransactionFilters())
    val filtersState: StateFlow<TransactionFilters> = _filtersState.asStateFlow()

    private val _searchState = MutableStateFlow("")
    val searchState: StateFlow<String> = _searchState.asStateFlow()

    private val _sortOptionsState = MutableStateFlow(
        SortOptionsScreenState(Sort.DESCENDING, "time")
    )
    val sortOptionsState: StateFlow<SortOptionsScreenState> = _sortOptionsState.asStateFlow()

    private val _event: Channel<Event> = Channel(Int.MAX_VALUE)
    val event: Flow<Event> = _event.receiveAsFlow()

    private val counterPartyQuery =
        realm.query<CounterParty>("uuid != $0", counterPartyUuid).sort("frequency", Sort.DESCENDING)

    private val categoryMethodAndSourceQuery =
        realm.query<Transaction>("counterParty.uuid == $0", counterPartyUuid)

    init {
        loadDataJob = loadData()
    }

    override fun onCleared() {
        super.onCleared()
        realm.close()
        _event.close()
    }

    private fun loadData() = viewModelScope.launch {
        val counterParties = counterPartyQuery.find()
        val transactions = categoryMethodAndSourceQuery.find()

        val categories = transactions
            .mapNotNull { transaction -> transaction.category }
            .distinctBy { category -> category.uuid }

        val methods = transactions
            .mapNotNull { transaction -> transaction.method }
            .distinctBy { method -> method.uuid }

        val sources = transactions
            .mapNotNull { transaction -> transaction.source }
            .distinctBy { source -> source.uuid }
            .toMutableList()

        sources.forEach { source ->
            val formatter = currencyFormatterMap.getValue(source.currency)
            source.displayBalance = formatter.format(source.balance).toString()
        }

        _state.update {
            it.copy(
                categories = categories,
                counterParties = counterParties,
                methods = methods,
                sources = sources
            )
        }

        val selectedCategories = categories.map { it.uuid }.toSet()
        val selectedMethods = methods.map { it.uuid }.toSet()
        val selectedSources = sources.map { it.uuid }.toSet()

        _filtersState.update { filters ->
            filters.copy(
                selectedCategories = selectedCategories,
                selectedCategoryCount = numberFormatter.format(selectedCategories.size).toString(),
                selectedMethods = selectedMethods,
                selectedMethodCount = numberFormatter.format(selectedMethods.size).toString(),
                selectedSources = selectedSources,
                selectedSourceCount = numberFormatter.format(selectedSources.size).toString(),
                selectedCurrencyCount = numberFormatter.format(filters.selectedCurrencies.size)
                    .toString()
            )
        }

        loadTransactionsJob = loadTransactions()
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
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
            _sortOptionsState
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
        ) { searchText, sortOptions, filters ->
            Triple(searchText, sortOptions, filters)
        }.flatMapLatest { (searchText, sortOptions, filters) ->
            getTransactionQuery(
                searchText,
                sortOptions,
                filters
            )
        }.collectLatest { transactionChanges ->
            val transactionsMap = transactionChanges.list.groupBy(
                keySelector = { transaction -> transaction.time.toLocalDate() },
                valueTransform = { transaction ->
                    getTransactionWithIcons(
                        transaction,
                        searchState.value
                    )
                }
            )

            _state.update {
                it.copy(
                    transactionsHashCode = transactionChanges.list.hashCode(),
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
        filteredTransactions: Map<LocalDate, List<TransactionWithIcons>>,
        selectedTransactions: Set<String>
    ): Set<LocalDate> {
        return filteredTransactions
            .filter { entry ->
                val transactionUUIDs =
                    entry.value.map { transactionWithIcons -> transactionWithIcons.uuid }
                transactionUUIDs.all { uuid -> selectedTransactions.contains(uuid) }
            }
            .keys
    }

    private fun getAllSelected(
        filteredTransactions: Map<LocalDate, List<TransactionWithIcons>>,
        selectedTransactions: Set<String>
    ): Boolean {
        return filteredTransactions
            .values
            .flatten()
            .map { transaction -> transaction.uuid }
            .all { uuid -> selectedTransactions.contains(uuid) }
    }

    private fun getTransactionQuery(
        searchText: String,
        sortOptions: SortOptionsScreenState,
        filters: TransactionFilters
    ): Flow<ResultsChange<Transaction>> {
        var queryString =
            "time >= $0 && time <= $1 && currency IN $2 && amount >= $3 && amount <= $4 && typeId IN $5 && category.uuid IN $6 && method.uuid IN $7 && source.uuid IN $8"
        if (searchText.isNotBlank()) {
            queryString += " && (title CONTAINS[c] '${searchText.trim()}' || description CONTAINS[c] '${searchText.trim()}')"
        }
        queryString += " && counterParty.uuid == $9"
        return realm
            .query<Transaction>(
                queryString,
                filters.startDate?.toEpochMilli() ?: 0L,
                filters.endDate?.toEpochMilli() ?: Long.MAX_VALUE,
                filters.selectedCurrencies,
                filters.filterAmountMin,
                if (filters.filterAmountMax <= filters.filterAmountMin) Double.MAX_VALUE else filters.filterAmountMax,
                filters.selectedTransactionTypes,
                filters.selectedCategories,
                filters.selectedMethods,
                filters.selectedSources,
                counterPartyUuid
            )
            .sort("time", sortOptions.sortDirection)
            .asFlow()
    }

    private fun getTransactionWithIcons(
        transaction: Transaction,
        searchText: String
    ): TransactionWithIcons {
        val category = transaction.category!!
        val counterParty = transaction.counterParty
        val method = transaction.method!!
        val source = transaction.source!!
        val currencyFormatter = currencyFormatterMap.getValue(source.currency)
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
                icon = Constants.DEFAULT_SOURCE_ICON,
                value = source.name,
                resId = R.string.placeholder
            )
        )
        chips.add(
            ChipInfo(
                icon = TablerIcons.Alarm,
                value = transaction.time.format(DateTimeFormatter.ofPattern("hh:mm a")),
                resId = R.string.placeholder
            )
        )
        return TransactionWithIcons(
            uuid = transaction.uuid,
            annotatedTitle = getHighlightedString(transaction.title, searchText),
            annotatedDescription = getHighlightedString(transaction.description, searchText),
            amount = currencyFormatter.format(transaction.amount).toString(),
            typeIcon = transaction.type.icon,
            typeIconColor = transaction.type.color,
            currency = transaction.currency,
            chips = chips
        )
    }

    fun migrateTransactions(counterParty: CounterParty) = viewModelScope.launch {
        loadDataJob?.cancelAndJoin()
        loadTransactionsJob?.cancelAndJoin()
        _state.update {
            it.copy(
                loading = true,
                enabled = false,
            )
        }

        var result: Event

        val selectedTransactionUUIDs = state.value.selectedTransactions

        try {
            realm.write {
                val transactions = this
                    .query<Transaction>("uuid IN $0", selectedTransactionUUIDs)
                    .find()
                transactions.getOrNull(0)?.counterParty?.let { previousCounterParty ->
                    findLatest(previousCounterParty)?.let {
                        it.frequency -= transactions.size
                    }
                }
                findLatest(counterParty)?.let { newCounterParty ->
                    newCounterParty.frequency += transactions.size
                    transactions.forEach { transaction ->
                        transaction.counterParty = newCounterParty
                    }
                }
            }
            result = Event.MigrationSuccess
        } catch (exception: Exception) {
            result = Event.InternalError
        }

        _event.send(result)

        _state.update {
            it.copy(
                loading = false
            )
        }
    }

    fun toggleLocalDateSelected(localDate: LocalDate) {
        _state.update {
            val transactionUUIDs = it.filteredTransactions
                .getOrDefault(localDate, emptyList())
                .map { transactionWithIcons -> transactionWithIcons.uuid }
                .toSet()

            val selectedTransactions = it.selectedTransactions.toMutableSet()
            val selectedLocalDates = it.selectedLocalDates.toMutableSet()

            if (selectedLocalDates.contains(localDate)) {
                selectedLocalDates.remove(localDate)
                selectedTransactions.removeAll(transactionUUIDs)
            } else {
                selectedLocalDates.add(localDate)
                selectedTransactions.addAll(transactionUUIDs)
            }

            it.copy(
                selectedLocalDates = selectedLocalDates,
                selectedTransactions = selectedTransactions,
                selectedTransactionCount = selectedTransactions.size,
                allSelected = getAllSelected(
                    it.filteredTransactions,
                    selectedTransactions
                )
            )
        }
    }

    fun toggleTransactionSelected(uuid: String) {
        _state.update {
            val selectedTransactions = it.selectedTransactions.toMutableSet()
            if (selectedTransactions.contains(uuid)) {
                selectedTransactions.remove(uuid)
            } else {
                selectedTransactions.add(uuid)
            }
            it.copy(
                selectedLocalDates = getSelectedLocalDates(
                    it.filteredTransactions,
                    selectedTransactions
                ),
                selectedTransactions = selectedTransactions,
                selectedTransactionCount = selectedTransactions.size,
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
                val transactionUUIDs = it.filteredTransactions
                    .values
                    .flatten()
                    .map { transaction -> transaction.uuid }

                selectedTransactions.addAll(transactionUUIDs)
            }

            it.copy(
                selectedTransactions = selectedTransactions,
                selectedTransactionCount = selectedTransactions.size,
                selectedLocalDates = getSelectedLocalDates(
                    it.filteredTransactions,
                    selectedTransactions
                ),
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

    fun setSelectedCategories(selectedCategories: Set<String>) {
        _filtersState.update {
            it.copy(
                selectedCategories = selectedCategories,
                selectedCounterPartyCount = numberFormatter.format(selectedCategories.size)
                    .toString()
            )
        }
    }

    fun setSelectedMethods(selectedMethods: Set<String>) {
        _filtersState.update {
            it.copy(
                selectedMethods = selectedMethods,
                selectedMethodCount = numberFormatter.format(selectedMethods.size).toString()
            )
        }
    }

    fun setSelectedSources(selectedSources: Set<String>) {
        _filtersState.update {
            it.copy(
                selectedSources = selectedSources,
                selectedSourceCount = numberFormatter.format(selectedSources.size).toString()
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

    fun setSortDirection(sortDirection: Sort) {
        _sortOptionsState.update {
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