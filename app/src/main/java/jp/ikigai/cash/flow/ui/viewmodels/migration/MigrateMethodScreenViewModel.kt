package jp.ikigai.cash.flow.ui.viewmodels.migration

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import compose.icons.TablerIcons
import compose.icons.tablericons.Alarm
import io.objectbox.Box
import io.objectbox.BoxStore
import io.objectbox.kotlin.boxFor
import io.objectbox.kotlin.flow
import io.objectbox.query.Query
import io.objectbox.query.QueryBuilder
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.dto.ChipInfo
import jp.ikigai.cash.flow.data.dto.TransactionWithChips
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
import jp.ikigai.cash.flow.data.store.entity.Transaction_
import jp.ikigai.cash.flow.ui.screenStates.common.SortOptionsState
import jp.ikigai.cash.flow.ui.screenStates.common.TransactionFilters
import jp.ikigai.cash.flow.ui.screenStates.migration.MigrateMethodScreenState
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
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class MigrateMethodScreenViewModel(
    savedStateHandle: SavedStateHandle,
    store: BoxStore = DataStore.store
) : ViewModel() {

    private val datePattern = "dd-LLL-yyyy"

    private val methodId: Long = checkNotNull(savedStateHandle["id"])

    private var loadDataJob: Job? = null
    private var loadTransactionsJob: Job? = null

    private var numberFormatter = getNumberFormatter()
    private var currencyFormatterMap = getCurrencyFormatterMap()

    private val _state = MutableStateFlow(MigrateMethodScreenState())
    val state: StateFlow<MigrateMethodScreenState> = _state.asStateFlow()

    private val _filtersState = MutableStateFlow(TransactionFilters())
    val filtersState: StateFlow<TransactionFilters> = _filtersState.asStateFlow()

    private val _searchState = MutableStateFlow("")
    val searchState: StateFlow<String> = _searchState.asStateFlow()

    private val _sortOptionsState = MutableStateFlow(
        SortOptionsState<Transaction>(
            sortField = Transaction_.time
        )
    )
    val sortOptionsState: StateFlow<SortOptionsState<Transaction>> =
        _sortOptionsState.asStateFlow()

    private val _event: Channel<Event> = Channel(Int.MAX_VALUE)
    val event: Flow<Event> = _event.receiveAsFlow()

    private val accountBox: Box<Account> = store.boxFor()
    private val categoryBox: Box<Category> = store.boxFor()
    private val counterPartyBox: Box<CounterParty> = store.boxFor()
    private val methodBox: Box<Method> = store.boxFor()
    private val transactionBox: Box<Transaction> = store.boxFor()

    private val accountQuery = getAccountQuery()

    private val categoryQuery = getCategoryQuery()

    private val counterPartyQuery = getCounterPartyQuery()

    private val methodQuery = methodBox
        .query(
            Method_.id.notEqual(methodId)
        )
        .orderDesc(Method_.frequency)
        .build()

    init {
        loadDataJob = loadData()
    }

    override fun onCleared() {
        super.onCleared()
        _event.close()
        accountQuery.close()
        categoryQuery.close()
        counterPartyQuery.close()
        methodQuery.close()
    }

    private fun getAccountQuery(): Query<Account> {
        val queryBuilder = accountBox.query()

        queryBuilder
            .backlink(Transaction_.account)
            .apply(
                Transaction_.methodId.equal(methodId)
            )

        return queryBuilder.orderDesc(Account_.frequency).build()
    }

    private fun getCategoryQuery(): Query<Category> {
        val queryBuilder = categoryBox.query()

        queryBuilder
            .backlink(Transaction_.category)
            .apply(
                Transaction_.methodId.equal(methodId)
            )

        return queryBuilder.orderDesc(Category_.frequency).build()
    }

    private fun getCounterPartyQuery(): Query<CounterParty> {
        val queryBuilder = counterPartyBox.query()

        queryBuilder
            .backlink(Transaction_.counterParty)
            .apply(
                Transaction_.methodId.equal(methodId)
            )

        return queryBuilder.orderDesc(CounterParty_.frequency).build()
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

        _state.update {
            it.copy(
                accounts = accounts,
                categories = categories,
                counterParties = counterParties,
                methods = methods
            )
        }

        val selectedAccounts = accounts.map { it.id }.toSet()
        val selectedCategories = categories.map { it.id }.toSet()
        val selectedCounterParties = counterParties.map { it.id }.toSet()

        _filtersState.update { filters ->
            filters.copy(
                selectedAccounts = selectedAccounts,
                selectedAccountCount = numberFormatter.format(selectedAccounts.size).toString(),
                selectedCategories = selectedCategories,
                selectedCategoryCount = numberFormatter.format(selectedCategories.size).toString(),
                selectedCounterParties = selectedCounterParties,
                selectedCounterPartyCount = numberFormatter.format(selectedCounterParties.size)
                    .toString(),
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
        }.collectLatest { transactions ->
            val transactionsMap = transactions.groupBy(
                keySelector = { transaction -> transaction.time.toLocalDate() },
                valueTransform = { transaction ->
                    getTransactionWithChips(
                        transaction,
                        searchState.value
                    )
                }
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

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getTransactionQuery(
        searchText: String,
        sortOptions: SortOptionsState<Transaction>,
        filters: TransactionFilters
    ): Flow<MutableList<Transaction>> {
        val selectedCounterPartyIds = if (filters.includeNoCounterPartyTransactions) {
            filters.selectedCounterParties + 0L
        } else {
            filters.selectedCounterParties
        }
        val transactionQueryBuilder = transactionBox
            .query(
                Transaction_.time.between(
                    filters.startDate?.toEpochMilli() ?: 0L,
                    filters.endDate?.toEpochMilli() ?: Long.MAX_VALUE
                )
                    .and(
                        Transaction_.currency.oneOf(filters.selectedCurrencies.toTypedArray())
                    )
                    .and(
                        Transaction_.amount.between(
                            filters.filterAmountMin,
                            if (filters.filterAmountMax <= filters.filterAmountMin) Double.MAX_VALUE else filters.filterAmountMax
                        )
                    )
                    .and(
                        Transaction_.type.oneOf(
                            filters.selectedTransactionTypes.toIntArray()
                        )
                    )
                    .and(
                        Transaction_.accountId.oneOf(
                            filters.selectedAccounts.toLongArray()
                        )
                    )
                    .and(
                        Transaction_.categoryId.oneOf(
                            filters.selectedCategories.toLongArray()
                        )
                    )
                    .and(
                        Transaction_.counterPartyId.oneOf(
                            selectedCounterPartyIds.toLongArray()
                        )
                    )
                    .and(
                        Transaction_.methodId.equal(
                            methodId
                        )
                    )
            )
        val query = if (searchText.isNotBlank()) {
            transactionQueryBuilder
                .and()
                .apply(
                    Transaction_.title.contains(
                        searchText.trim(),
                        QueryBuilder.StringOrder.CASE_INSENSITIVE
                    )
                        .or(
                            Transaction_.description.contains(
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

    private fun getTransactionWithChips(
        transaction: Transaction,
        searchText: String
    ): TransactionWithChips {
        val account = transaction.account.target
        val category = transaction.category.target
        val counterParty = transaction.counterParty.target
        val method = transaction.method.target

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
                value = transaction.time.format(DateTimeFormatter.ofPattern("hh:mm a")),
                resId = R.string.placeholder
            )
        )
        return TransactionWithChips(
            id = transaction.id,
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
        loadTransactionsJob?.cancelAndJoin()
        _state.update {
            it.copy(
                loading = true,
                enabled = false,
            )
        }

        var result: Event

        val selectedTransactionIds = state.value.selectedTransactions

        try {
            val transactionsQuery = transactionBox
                .query(
                    Transaction_.id.oneOf(selectedTransactionIds.toLongArray())
                )
                .build()

            transactionsQuery.find().forEach { transaction ->
                transaction.method.setAndPutTarget(method)
            }

            transactionsQuery.close()

            val getLastUsedOfPreviousMethodQuery = transactionBox
                .query(
                    Transaction_.methodId.equal(methodId)
                )
                .orderDesc(Transaction_.time)
                .build()

            val getPreviousMethodQuery = methodBox
                .query(
                    Method_.id.equal(methodId)
                )
                .build()

            getPreviousMethodQuery.findUnique()?.let { previousMethod ->
                methodBox.put(
                    previousMethod.copy(
                        frequency = previousMethod.frequency - selectedTransactionIds.size,
                        lastUsed = getLastUsedOfPreviousMethodQuery.findFirst()?.time
                            ?: ZonedDateTime.ofInstant(
                                Instant.EPOCH, ZoneId.systemDefault()
                            )
                    )
                )
            }

            getPreviousMethodQuery.close()
            getLastUsedOfPreviousMethodQuery.close()

            val getLastUsedOfMethodQuery = transactionBox
                .query(
                    Transaction_.methodId.equal(method.id)
                )
                .orderDesc(Transaction_.time)
                .build()

            methodBox.put(
                method.copy(
                    frequency = method.frequency + selectedTransactionIds.size,
                    lastUsed = getLastUsedOfMethodQuery.findFirst()!!.time
                )
            )

            getLastUsedOfMethodQuery.close()

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
            val transactionIds = it.filteredTransactions
                .getOrDefault(localDate, emptyList())
                .map { transactionWithChips -> transactionWithChips.id }
                .toSet()

            val selectedTransactions = it.selectedTransactions.toMutableSet()
            val selectedLocalDates = it.selectedLocalDates.toMutableSet()

            if (selectedLocalDates.contains(localDate)) {
                selectedLocalDates.remove(localDate)
                selectedTransactions.removeAll(transactionIds)
            } else {
                selectedLocalDates.add(localDate)
                selectedTransactions.addAll(transactionIds)
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
                val transactionIds = it.filteredTransactions
                    .values
                    .flatten()
                    .map { transactionWithChips -> transactionWithChips.id }

                selectedTransactions.addAll(transactionIds)
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
        numberFormatter = getNumberFormatter(locale)
        currencyFormatterMap = getCurrencyFormatterMap(locale)
        _state.update {
            it.copy(
                locale = locale
            )
        }
    }
}