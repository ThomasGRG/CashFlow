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
import jp.ikigai.cash.flow.data.Database
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.dto.ChipInfo
import jp.ikigai.cash.flow.data.dto.MigrateScreenFlows
import jp.ikigai.cash.flow.data.dto.TransactionWithIcons
import jp.ikigai.cash.flow.data.entity.Category
import jp.ikigai.cash.flow.data.entity.CounterParty
import jp.ikigai.cash.flow.data.entity.Method
import jp.ikigai.cash.flow.data.entity.Source
import jp.ikigai.cash.flow.data.entity.Transaction
import jp.ikigai.cash.flow.ui.screenStates.migration.MigrateMethodScreenState
import jp.ikigai.cash.flow.utils.getCurrencyFormatterMap
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class MigrateMethodScreenViewModel(
    savedStateHandle: SavedStateHandle,
    private val realm: Realm = Realm.open(Database.config),
) : ViewModel() {

    private val methodUuid: String = checkNotNull(savedStateHandle["id"])

    private var loadDataJob: Job? = null

    private var numberFormatter = getNumberFormatter()
    private var currencyFormatterMap = getCurrencyFormatterMap()

    private val _state = MutableStateFlow(MigrateMethodScreenState())
    val state: StateFlow<MigrateMethodScreenState> = _state.asStateFlow()

    private val _event: Channel<Event> = Channel(Int.MAX_VALUE)
    val event: Flow<Event> = _event.receiveAsFlow()

    private val methodQuery =
        realm.query<Method>("uuid != $0", methodUuid).sort("frequency", Sort.DESCENDING)

    private val categoryCounterPartyAndSourceQuery =
        realm.query<Transaction>("method.uuid == $0", methodUuid)

    init {
        loadDataJob = loadData()
    }

    override fun onCleared() {
        super.onCleared()
        realm.close()
        _event.close()
    }

    private fun loadData() = viewModelScope.launch {
        combine(
            methodQuery.asFlow(),
            categoryCounterPartyAndSourceQuery.asFlow(),
            getTransactionQuery()
        ) { methodChanges, categoryCounterPartyAndSourceChanges, transactionChanges ->
            val categories = categoryCounterPartyAndSourceChanges.list
                .mapNotNull { transaction -> transaction.category }
                .distinctBy { category -> category.uuid }

            val counterParties = categoryCounterPartyAndSourceChanges.list
                .mapNotNull { transaction -> transaction.counterParty }
                .distinctBy { counterParty -> counterParty.uuid }

            val sources = categoryCounterPartyAndSourceChanges.list
                .mapNotNull { transaction -> transaction.source }
                .distinctBy { source -> source.uuid }
                .toMutableList()

            sources.forEach { source ->
                val formatter = currencyFormatterMap.getValue(source.currency)
                source.displayBalance = formatter.format(source.balance).toString()
            }

            MigrateScreenFlows(
                categories = categories,
                counterParties = counterParties,
                methods = methodChanges.list,
                sources = sources,
                transactions = transactionChanges.list
            )
        }.collectLatest { migrateScreenFlows ->
            _state.update {
                val selectedCategoriesMap =
                    getSelectedCategories(migrateScreenFlows.categories, it.selectedCategories)
                val selectedCounterPartiesMap =
                    getSelectedCounterParties(
                        migrateScreenFlows.counterParties,
                        it.selectedCounterParties
                    )
                val selectedSourcesMap =
                    getSelectedSources(migrateScreenFlows.sources, it.selectedSources)

                val selectedCategories =
                    selectedCategoriesMap.filter { entry -> entry.value }.keys
                val selectedCounterParties =
                    selectedCounterPartiesMap.filter { entry -> entry.value }.keys
                val selectedSources = selectedSourcesMap.filter { entry -> entry.value }.keys
                val selectedCurrencies = it.selectedCurrencies.filter { entry -> entry.value }.keys

                val filteredTransactions = migrateScreenFlows.transactions.groupBy(
                    keySelector = { transaction -> transaction.time.toLocalDate() },
                    valueTransform = { transaction ->
                        getTransactionWithIcons(
                            transaction,
                            it.searchText
                        )
                    }
                )

                it.copy(
                    transactionsHashCode = migrateScreenFlows.transactions.hashCode(),
                    filteredTransactions = filteredTransactions,
                    selectedLocalDates = getSelectedLocalDates(
                        filteredTransactions,
                        it.selectedTransactions
                    ),
                    allSelected = getAllSelected(
                        filteredTransactions,
                        it.selectedTransactions
                    ),
                    categories = migrateScreenFlows.categories,
                    selectedCategories = selectedCategoriesMap,
                    selectedCategoryCount = numberFormatter.format(selectedCategories.size)
                        .toString(),
                    counterParties = migrateScreenFlows.counterParties,
                    selectedCounterParties = selectedCounterPartiesMap,
                    selectedCounterPartyCount = numberFormatter.format(selectedCounterParties.size)
                        .toString(),
                    methods = migrateScreenFlows.methods,
                    sources = migrateScreenFlows.sources,
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

    private fun getSelectedCategories(
        categories: List<Category>,
        selectedCategories: Map<String, Boolean>
    ): Map<String, Boolean> {
        return categories.associateBy(
            {
                it.uuid
            },
            {
                selectedCategories.getOrDefault(it.uuid, true)
            }
        )
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

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getTransactionQuery(): Flow<ResultsChange<Transaction>> {
        return state.flatMapLatest {
            var queryString =
                "time >= $0 && time <= $1 && currency IN $2 && amount >= $3 && amount <= $4 && typeId IN $5 && category.uuid IN $6 && method.uuid == $7 && source.uuid IN $8"
            if (it.searchText.isNotBlank()) {
                queryString += " && (title CONTAINS[c] '${it.searchText.trim()}' || description CONTAINS[c] '${it.searchText.trim()}')"
            }
            queryString += if (it.includeNoCounterPartyTransactions) {
                " && (counterParty == nil || counterParty.uuid IN $9)"
            } else {
                " && counterParty.uuid IN $9"
            }
            realm.query<Transaction>(
                queryString,
                it.startDate?.getStartOfDayInEpochMilli() ?: 0L,
                it.endDate?.getEndOfDayInEpochMilli() ?: Long.MAX_VALUE,
                it.selectedCurrencies.filter { entry -> entry.value }.keys,
                it.filterAmountMin,
                if (it.filterAmountMax <= it.filterAmountMin) Double.MAX_VALUE else it.filterAmountMax,
                it.selectedTransactionTypes,
                it.selectedCategories.filter { entry -> entry.value }.keys,
                methodUuid,
                it.selectedSources.filter { entry -> entry.value }.keys,
                it.selectedCounterParties.filter { entry -> entry.value }.keys
            ).sort("time", it.sortDirection).asFlow()
        }
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

    fun migrateTransactions(method: Method) = viewModelScope.launch {
        loadDataJob?.cancelAndJoin()
        _state.update {
            it.copy(
                loading = true,
                enabled = false,
            )
        }

        val selectedTransactionUUIDs = state.value.selectedTransactions

        try {
            realm.write {
                val transactions = this
                    .query<Transaction>("uuid IN $0", selectedTransactionUUIDs)
                    .find()
                transactions.getOrNull(0)?.method?.let { previousMethod ->
                    findLatest(previousMethod)?.let {
                        it.frequency -= transactions.size
                    }
                }
                findLatest(method)?.let { newMethod ->
                    newMethod.frequency += transactions.size
                    transactions.forEach { transaction ->
                        transaction.method = newMethod
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
            val transactionUUIDs = it.filteredTransactions
                .getOrDefault(localDate, emptyList())
                .map { transactionWithIcons -> transactionWithIcons.uuid }

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
                loading = true,
                startDate = startDate,
                endDate = endDate,
                startDateString = startDate?.getDateString() ?: "",
                endDateString = endDate?.getDateString() ?: "",
                dateRangeStringRes = if (startDate == null && endDate == null) {
                    R.string.all_time_date_range_label
                } else {
                    R.string.date_range_label
                }
            )
        }
    }

    fun setSelectedCategories(
        selectedCategories: Map<String, Boolean>
    ) {
        _state.update {
            it.copy(
                loading = true,
                selectedCategories = selectedCategories
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

    fun setSelectedSources(selectedSources: Map<String, Boolean>) {
        _state.update {
            it.copy(
                loading = true,
                selectedSources = selectedSources
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
        currencyFormatterMap = getCurrencyFormatterMap(locale)
        _state.update {
            it.copy(
                locale = locale
            )
        }
    }
}