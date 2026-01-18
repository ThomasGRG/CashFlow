/*
CashFlow - Expense Tracker
Copyright (C) 2025 ThomasGRG

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 3 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package jp.ikigai.cash.flow.ui.viewmodels.migration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import compose.icons.TablerIcons
import compose.icons.tablericons.Alarm
import jp.ikigai.cash.flow.CashFlowDatabase
import jp.ikigai.cash.flow.MethodWithTransactionMetadata
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.TransactionWithMetadata
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.Database
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.dto.ChipInfo
import jp.ikigai.cash.flow.data.dto.TransactionWithChips
import jp.ikigai.cash.flow.data.enums.SortDirection
import jp.ikigai.cash.flow.data.enums.TransactionType
import jp.ikigai.cash.flow.ui.screenStates.common.SortConfigState
import jp.ikigai.cash.flow.ui.screenStates.common.TransactionFilters
import jp.ikigai.cash.flow.ui.screenStates.migration.MigrateMethodScreenState
import jp.ikigai.cash.flow.utils.getCurrencyFormatterMap
import jp.ikigai.cash.flow.utils.getDateString
import jp.ikigai.cash.flow.utils.getHighlightedString
import jp.ikigai.cash.flow.utils.getNumberFormatter
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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class MigrateMethodScreenViewModel(
    private val methodId: Long,
    private val database: CashFlowDatabase = Database.database
) : ViewModel() {

    private val datePattern = "dd-LLL-yyyy"

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
        val accountIds = database
            .accountQueries
            .getAccountIdsOfTransactionsWithMethodId(methodId)
            .executeAsList()
        val accounts = database
            .accountWithTransactionMetadataQueries
            .getByIds(accountIds)
            .executeAsList()
            .map { account ->
                val formatter = currencyFormatterMap.getValue(account.currency)
                account.copy(
                    formattedBalance = formatter.format(account.balance).toString()
                )
            }

        val categoryIds = database
            .categoryQueries
            .getCategoryIdsOfTransactionsWithMethodId(methodId)
            .executeAsList()
        val categories = database
            .categoryWithTransactionMetadataQueries
            .getByIds(categoryIds)
            .executeAsList()

        val counterPartyIds = database
            .counterPartyQueries
            .getCounterPartyIdsOfTransactionsWithMethodId(methodId)
            .executeAsList()
        val counterParties = database
            .counterPartyWithTransactionMetadataQueries
            .getByIds(counterPartyIds)
            .executeAsList()

        val methods = database
            .methodWithTransactionMetadataQueries
            .getAllMethodsExceptIdSortedByTransactionCountDesc(methodId)
            .executeAsList()

        _state.update {
            it.copy(
                accounts = accounts,
                categories = categories,
                counterParties = counterParties,
                methods = methods
            )
        }

        val selectedAccounts = accounts.map { it.accountId }.toSet()
        val selectedCategories = categories.map { it.categoryId }.toSet()
        val selectedCounterParties = counterParties.map { it.counterPartyId }.toSet()

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
                    selectedAccountIds = filters.selectedAccounts,
                    selectedCategoryIds = filters.selectedCategories,
                    selectedCounterPartyIds = if (filters.includeNoCounterPartyTransactions) {
                        filters.selectedCounterParties + 0L
                    } else {
                        filters.selectedCounterParties
                    },
                    selectedMethodIds = listOf(methodId),
                    sortField = sortConfig.sortField,
                    sortDirection = sortConfig.sortDirection.name
                )
                .asFlow()
                .mapToList(Dispatchers.IO)
        }.collectLatest { transactions ->
            val transactionsMap = transactions.groupBy(
                keySelector = { transaction -> transaction.transactionDateTime.toLocalDate() },
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

    fun migrateTransactions(method: MethodWithTransactionMetadata) = viewModelScope.launch {
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
            database
                .transactionQueries
                .migrateMethodForTransactions(
                    methodId = method.methodId,
                    transactionIds = selectedTransactionIds
                )
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