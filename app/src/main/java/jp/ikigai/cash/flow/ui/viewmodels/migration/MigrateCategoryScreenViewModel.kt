package jp.ikigai.cash.flow.ui.viewmodels.migration

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import compose.icons.TablerIcons
import compose.icons.tablericons.Alarm
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.Sort
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Database
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.dto.ChipInfo
import jp.ikigai.cash.flow.data.dto.TransactionWithIcons
import jp.ikigai.cash.flow.data.entity.Category
import jp.ikigai.cash.flow.data.entity.CounterParty
import jp.ikigai.cash.flow.data.entity.Method
import jp.ikigai.cash.flow.data.entity.Source
import jp.ikigai.cash.flow.data.entity.Transaction
import jp.ikigai.cash.flow.ui.screenStates.migration.MigrateCategoryScreenState
import jp.ikigai.cash.flow.utils.getCurrencyFormatter
import jp.ikigai.cash.flow.utils.getDateString
import jp.ikigai.cash.flow.utils.getEndOfDayInEpochMilli
import jp.ikigai.cash.flow.utils.getHighlightedString
import jp.ikigai.cash.flow.utils.getNumberFormatter
import jp.ikigai.cash.flow.utils.getStartOfDayInEpochMilli
import jp.ikigai.cash.flow.utils.toLocalDate
import jp.ikigai.cash.flow.utils.toZonedDateTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class MigrateCategoryScreenViewModel(
    savedStateHandle: SavedStateHandle,
    private val realm: Realm = Realm.open(Database.config),
) : ViewModel() {

    private val categoryUuid: String = checkNotNull(savedStateHandle["id"])

    private var loadDataJob: Job? = null

    private var numberFormatter = getNumberFormatter()

    private val _state = MutableStateFlow(MigrateCategoryScreenState())
    val state: StateFlow<MigrateCategoryScreenState> = _state.asStateFlow()

    private val _event: Channel<Event> = Channel(Int.MAX_VALUE)
    val event: Flow<Event> = _event.receiveAsFlow()

    init {
        loadDataJob = loadData()
        getCategories()
    }

    override fun onCleared() {
        super.onCleared()
        realm.close()
    }

    private fun getCategories() = viewModelScope.launch {
        realm
            .query<Category>("uuid != $0", categoryUuid)
            .sort("frequency", Sort.DESCENDING)
            .asFlow()
            .collectLatest { changes ->
                _state.update {
                    it.copy(
                        categories = changes.list
                    )
                }
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadData() = viewModelScope.launch {
        state.flatMapLatest {
            realm
                .query<Transaction>("category.uuid == $0", categoryUuid)
                .sort("time", it.sortDirection)
                .asFlow()
        }.collectLatest { transactionChanges ->
            _state.update {
                val counterParties = transactionChanges.list
                    .mapNotNull { transaction -> transaction.counterParty }
                    .distinctBy { counterParty -> counterParty.uuid }

                val methods = transactionChanges.list
                    .mapNotNull { transaction -> transaction.method }
                    .distinctBy { method -> method.uuid }

                val sources = transactionChanges.list
                    .mapNotNull { transaction -> transaction.source }
                    .distinctBy { source -> source.uuid }

                val selectedCounterPartiesMap =
                    getSelectedCounterParties(counterParties, it.selectedCounterParties)
                val selectedMethodsMap = getSelectedMethods(methods, it.selectedMethods)
                val selectedSourcesMap = getSelectedSources(sources, it.selectedSources)

                val selectedCounterParties =
                    selectedCounterPartiesMap.filter { entry -> entry.value }.keys
                val selectedMethods = selectedMethodsMap.filter { entry -> entry.value }.keys
                val selectedSources = selectedSourcesMap.filter { entry -> entry.value }.keys
                val selectedCurrencies = it.selectedCurrencies.filter { entry -> entry.value }.keys

                val filteredTransactionsMap = filterTransactions(
                    transactions = transactionChanges.list,
                    searchText = it.searchText.trim(),
                    maxAmount = if (it.filterAmountMax == 0.0) Double.MAX_VALUE else it.filterAmountMax,
                    minAmount = it.filterAmountMin,
                    minTime = it.startDate?.getStartOfDayInEpochMilli() ?: 0L,
                    maxTime = it.endDate?.getEndOfDayInEpochMilli() ?: Long.MAX_VALUE,
                    selectedCurrencies = selectedCurrencies,
                    includeNoCounterPartyTransactions = it.includeNoCounterPartyTransactions,
                    selectedCounterParties = selectedCounterParties,
                    selectedMethods = selectedMethods,
                    selectedSources = selectedSources,
                    selectedTransactionTypes = it.selectedTransactionTypes,
                    locale = it.locale
                )

                val selectedTransactions = getSelectedTransactions(
                    transactionChanges.list,
                    it.selectedTransactions
                )

                it.copy(
                    transactionsHashCode = transactionChanges.list.hashCode(),
                    filteredTransactions = filteredTransactionsMap,
                    selectedTransactions = selectedTransactions,
                    selectedLocalDates = getSelectedLocalDates(
                        filteredTransactionsMap,
                        selectedTransactions
                    ),
                    allSelected = getAllSelected(
                        filteredTransactionsMap,
                        selectedTransactions
                    ),
                    selectedTransactionCount = selectedTransactions.filter { entry -> entry.value }.size,
                    counterParties = counterParties,
                    selectedCounterParties = selectedCounterPartiesMap,
                    selectedCounterPartyCount = numberFormatter.format(selectedCounterParties.size)
                        .toString(),
                    methods = methods,
                    selectedMethods = selectedMethodsMap,
                    selectedMethodCount = numberFormatter.format(selectedMethods.size)
                        .toString(),
                    sources = sources,
                    selectedSources = selectedSourcesMap,
                    selectedSourceCount = numberFormatter.format(selectedSources.size)
                        .toString(),
                    selectedCurrencyCount = numberFormatter.format(selectedCurrencies.size)
                        .toString(),
                    loading = false,
                    enabled = true
                )
            }
        }
    }

    private fun getSelectedTransactions(
        transactions: List<Transaction>,
        selectedTransactions: Map<String, Boolean>
    ): Map<String, Boolean> {
        return transactions.associateBy(
            {
                it.uuid
            },
            {
                selectedTransactions.getOrDefault(it.uuid, false)
            }
        )
    }

    private fun getSelectedLocalDates(
        filteredTransactions: Map<LocalDate, List<TransactionWithIcons>>,
        selectedTransactions: Map<String, Boolean>
    ): Map<LocalDate, Boolean> {
        return filteredTransactions
            .entries
            .associate { entry ->
                val transactionUUIDs =
                    entry.value.map { transactionWithIcons -> transactionWithIcons.uuid }
                val selected = transactionUUIDs.all { uuid -> selectedTransactions[uuid] == true }
                entry.key to selected
            }
    }

    private fun getAllSelected(
        filteredTransactions: Map<LocalDate, List<TransactionWithIcons>>,
        selectedTransactions: Map<String, Boolean>
    ): Boolean {
        return filteredTransactions
            .values
            .flatten()
            .map { transaction -> transaction.uuid }
            .all { uuid -> selectedTransactions[uuid] == true }
    }

    private fun getSelectedCounterParties(
        counterParties: List<CounterParty>,
        selectedCounterParties: Map<String, Boolean>
    ): Map<String, Boolean> {
        return counterParties.associateBy(
            {
                it.uuid
            },
            {
                selectedCounterParties.getOrDefault(it.uuid, true)
            }
        )
    }

    private fun getSelectedMethods(
        methods: List<Method>,
        selectedMethods: Map<String, Boolean>
    ): Map<String, Boolean> {
        return methods.associateBy(
            {
                it.uuid
            },
            {
                selectedMethods.getOrDefault(it.uuid, true)
            }
        )
    }

    private fun getSelectedSources(
        sources: List<Source>,
        selectedSources: Map<String, Boolean>
    ): Map<String, Boolean> {
        return sources.associateBy(
            {
                it.uuid
            },
            {
                selectedSources.getOrDefault(it.uuid, true)
            }
        )
    }

    private fun filterTransactions(
        transactions: List<Transaction>,
        searchText: String,
        maxAmount: Double,
        minAmount: Double,
        minTime: Long,
        maxTime: Long,
        selectedCurrencies: Set<String>,
        includeNoCounterPartyTransactions: Boolean,
        selectedCounterParties: Set<String>,
        selectedMethods: Set<String>,
        selectedSources: Set<String>,
        selectedTransactionTypes: List<Int>,
        locale: Locale?
    ): Map<LocalDate, List<TransactionWithIcons>> {
        return transactions
            .filter { transaction ->
                val containsTitleOrDescription = if (searchText.isNotEmpty()) {
                    transaction.title.contains(
                        searchText,
                        true
                    ) || transaction.description.contains(
                        searchText,
                        true
                    )
                } else {
                    true
                }

                val containsCurrency = selectedCurrencies.contains(transaction.currency)
                val containsType = selectedTransactionTypes.contains(transaction.type.id)
                val containsMethod = selectedMethods.contains(transaction.method?.uuid)
                val containsSource = selectedSources.contains(transaction.source?.uuid)

                val containsCounterParty = if (includeNoCounterPartyTransactions) {
                    transaction.counterParty == null || selectedCounterParties.contains(transaction.counterParty?.uuid)
                } else {
                    selectedCounterParties.contains(transaction.counterParty?.uuid)
                }

                val amountBetween = transaction.amount in minAmount..maxAmount

                val timeBetween = transaction.time in minTime..maxTime

                containsTitleOrDescription && containsCurrency && containsType && containsMethod && containsSource && amountBetween && timeBetween && containsCounterParty
            }
            .groupBy(
                keySelector = { transaction -> transaction.time.toLocalDate() },
                valueTransform = { transaction ->
                    getTransactionWithIcons(
                        transaction,
                        searchText,
                        locale
                    )
                }
            )
    }

    private fun getTransactionWithIcons(
        transaction: Transaction,
        searchText: String,
        locale: Locale?
    ): TransactionWithIcons {
        val category = transaction.category!!
        val counterParty = transaction.counterParty
        val method = transaction.method!!
        val source = transaction.source!!
        val currencyFormatter = getCurrencyFormatter(locale, source.currency)
        val chips: MutableList<ChipInfo> = mutableListOf()
        if (counterParty != null) {
            chips.add(
                ChipInfo(
                    icon = counterParty.icon,
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
                icon = method.icon,
                value = method.name,
                resId = R.string.placeholder
            )
        )
        chips.add(
            ChipInfo(
                icon = source.icon,
                value = source.name,
                resId = R.string.placeholder
            )
        )
        chips.add(
            ChipInfo(
                icon = TablerIcons.Alarm,
                value = transaction.time.toZonedDateTime()
                    .format(DateTimeFormatter.ofPattern("hh:mm a")),
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

    fun migrateTransactions(category: Category) = viewModelScope.launch {
        loadDataJob?.cancelAndJoin()
        _state.update {
            it.copy(
                loading = true,
                enabled = false,
            )
        }

        val selectedTransactionUUIDs = state.value.selectedTransactions
            .filter { entry -> entry.value }
            .keys

        try {
            realm.write {
                val transactions = this
                    .query<Transaction>("uuid IN $0", selectedTransactionUUIDs)
                    .find()
                transactions.getOrNull(0)?.category?.let { previousCategory ->
                    findLatest(previousCategory)?.let {
                        it.frequency -= transactions.size
                    }
                }
                findLatest(category)?.let { newCategory ->
                    newCategory.frequency += transactions.size
                    transactions.forEach { transaction ->
                        transaction.category = newCategory
                    }
                }
            }
            _event.send(Event.MigrationSuccess)
        } catch (exception: Exception) {
            _event.send(Event.InternalError)
        }
        _state.update {
            it.copy(
                loading = false
            )
        }
    }

    fun toggleLocalDateSelected(localDate: LocalDate) {
        _state.update {
            val transactions = it.filteredTransactions.getOrDefault(localDate, emptyList())
            val selectedTransactions = it.selectedTransactions.toMutableMap()
            val selectedLocalDates = it.selectedLocalDates.toMutableMap()

            transactions.forEach { transaction ->
                selectedTransactions[transaction.uuid] = !selectedLocalDates[localDate]!!
            }

            selectedLocalDates[localDate] = !selectedLocalDates[localDate]!!

            it.copy(
                selectedLocalDates = selectedLocalDates.toMap(),
                selectedTransactions = selectedTransactions.toMap()
            )
        }
    }

    fun toggleTransactionSelected(uuid: String) {
        _state.update {
            val selectedTransactions = it.selectedTransactions.toMutableMap()
            selectedTransactions[uuid] = !selectedTransactions[uuid]!!
            it.copy(
                selectedTransactions = selectedTransactions.toMap()
            )
        }
    }

    fun toggleSelection() {
        _state.update {
            val selectedTransactions = it.selectedTransactions.toMutableMap()
            it.filteredTransactions
                .values
                .flatten()
                .forEach { transaction ->
                    selectedTransactions[transaction.uuid] = !it.allSelected
                }
            it.copy(
                selectedTransactions = selectedTransactions
            )
        }
    }

    fun setSelectedCurrencies(selectedCurrencies: Map<String, Boolean>) {
        _state.update {
            it.copy(
                loading = true,
                selectedCurrencies = selectedCurrencies
            )
        }
    }

    fun setStartDateAndEndDate(startDate: LocalDate?, endDate: LocalDate?) {
        _state.update {
            it.copy(
                startDate = startDate,
                endDate = endDate,
                startDateString = startDate?.getDateString() ?: "",
                endDateString = endDate?.getDateString() ?: "",
                dateRangeStringRes = if (startDate == null && endDate == null) {
                    R.string.all_time_date_range_label
                } else {
                    R.string.date_range_label
                },
                loading = true
            )
        }
    }

    fun setSelectedCounterParties(
        includeTransactionsWithNoCounterParty: Boolean,
        selectedCounterParties: Map<String, Boolean>
    ) {
        _state.update {
            it.copy(
                loading = true,
                selectedCounterParties = selectedCounterParties,
                includeNoCounterPartyTransactions = includeTransactionsWithNoCounterParty
            )
        }
    }

    fun setSelectedMethods(selectedMethods: Map<String, Boolean>) {
        _state.update {
            it.copy(
                loading = true,
                selectedMethods = selectedMethods
            )
        }
    }

    fun setSelectedSources(selectedSources: Map<String, Boolean>) {
        _state.update {
            it.copy(
                loading = true,
                selectedSources = selectedSources
            )
        }
    }

    fun setSelectedItems(
        includeTransactionsWithNoItems: Boolean,
        selectedItems: Map<String, Boolean>
    ) {
        _state.update {
            it.copy(
                loading = true,
                selectedItems = selectedItems,
                includeNoItemTransactions = includeTransactionsWithNoItems
            )
        }
    }

    fun setSelectedTransactionTypes(selectedTransactionTypes: List<Int>) {
        _state.update {
            it.copy(
                loading = true,
                selectedTransactionTypes = selectedTransactionTypes
            )
        }
    }

    fun setSortDirection(sortDirection: Sort) {
        _state.update {
            it.copy(
                sortDirection = sortDirection,
                loading = true
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
        _state.update {
            it.copy(
                loading = true,
                filterAmountMin = min,
                filterAmountMax = max,
                filterAmountRange = filterAmountRange
            )
        }
    }

    fun setSearchText(searchText: String) {
        _state.update {
            it.copy(
                loading = true,
                searchText = searchText,
            )
        }
    }

    fun setLocale(locale: Locale?) {
        numberFormatter = getNumberFormatter(locale)
        _state.update {
            it.copy(
                locale = locale
            )
        }
    }
}