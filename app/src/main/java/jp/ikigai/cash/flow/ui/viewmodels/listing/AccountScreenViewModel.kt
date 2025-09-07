package jp.ikigai.cash.flow.ui.viewmodels.listing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import compose.icons.TablerIcons
import compose.icons.tablericons.ChartLine
import compose.icons.tablericons.History
import jp.ikigai.cash.flow.AccountWithTransactionMetadata
import jp.ikigai.cash.flow.CashFlowDatabase
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.Database
import jp.ikigai.cash.flow.data.dto.AccountListingDTO
import jp.ikigai.cash.flow.data.dto.ChipInfo
import jp.ikigai.cash.flow.data.enums.SortDirection
import jp.ikigai.cash.flow.data.preferences.CashFlowPreferencesDataStore
import jp.ikigai.cash.flow.ui.screenStates.common.SortConfigState
import jp.ikigai.cash.flow.ui.screenStates.listing.AccountScreenState
import jp.ikigai.cash.flow.utils.getCurrencyFormatterMap
import jp.ikigai.cash.flow.utils.getHighlightedString
import jp.ikigai.cash.flow.utils.getNumberFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class AccountScreenViewModel(
    private val preferencesDataStore: CashFlowPreferencesDataStore,
    private val database: CashFlowDatabase = Database.database
) : ViewModel() {

    private var numberFormatter = getNumberFormatter()
    private var currencyFormatterMap = getCurrencyFormatterMap()

    private val _state = MutableStateFlow(AccountScreenState())
    val state: StateFlow<AccountScreenState> = _state.asStateFlow()

    private val _searchState = MutableStateFlow("")
    val searchState: StateFlow<String> = _searchState.asStateFlow()

    private val _sortConfigState = MutableStateFlow(
        SortConfigState(sortField = "transactionCount")
    )
    val sortConfigState: StateFlow<SortConfigState> = _sortConfigState.asStateFlow()

    init {
        getSortPreferences()
        getSources()
        getCount()
    }

    private fun getSortPreferences() = viewModelScope.launch {
        preferencesDataStore
            .getAccountsScreenSortConfig()
            .take(1)
            .collectLatest { sortState ->
                _sortConfigState.update {
                    it.copy(
                        sortField = sortState.sortField,
                        sortDirection = sortState.sortDirection
                    )
                }
            }
    }

    private fun getCount() = viewModelScope.launch {
        database.accountQueries.count().asFlow().mapToOne(Dispatchers.IO).collectLatest { count ->
            _state.update {
                it.copy(
                    count = count,
                    countString = numberFormatter.format(count).toString()
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun getSources() = viewModelScope.launch {
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
                .onEach { sortState ->
                    _state.update {
                        it.copy(
                            loading = true
                        )
                    }
                    preferencesDataStore.saveAccountsScreenSortConfig(sortState)
                }
        ) { searchText, sortConfig ->
            Pair(searchText, sortConfig)
        }.flatMapLatest { (searchText, sortConfig) ->
            if (sortConfig.sortField == "balance") {
                database
                    .accountWithTransactionMetadataQueries
                    .getAccountsGroupedByCurrency(
                        searchText = searchText,
                        sortDirection = sortConfig.sortDirection.name
                    )
                    .asFlow()
                    .mapToList(Dispatchers.IO)
            } else {
                database
                    .accountWithTransactionMetadataQueries
                    .getAccounts(
                        searchText = searchText,
                        sortField = sortConfig.sortField,
                        sortDirection = sortConfig.sortDirection.name
                    )
                    .asFlow()
                    .mapToList(Dispatchers.IO)
            }
        }.collectLatest { accounts ->
            _state.update { screenState ->
                screenState.copy(
                    accounts = mapToDTO(accounts, searchState.value),
                    loading = false
                )
            }
        }
    }

    private fun mapToDTO(
        accounts: List<AccountWithTransactionMetadata>,
        searchText: String
    ): List<AccountListingDTO> {
        val hasBeenUsedComparator = Instant.EPOCH.atZone(ZoneId.systemDefault())
        return accounts.map { account ->
            val currencyFormatter = currencyFormatterMap.getValue(account.currency)
            val chips: MutableList<ChipInfo> = mutableListOf()
            chips.add(
                ChipInfo(
                    resId = R.string.frequency_of_use_label,
                    value = numberFormatter.format(account.transactionCount).toString(),
                    icon = TablerIcons.ChartLine
                )
            )
            if (account.lastUsed != null && account.lastUsed > hasBeenUsedComparator) {
                chips.add(
                    ChipInfo(
                        resId = R.string.last_used_datetime_label,
                        value = account.lastUsed
                            .format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")),
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
            AccountListingDTO(
                id = account.accountId,
                annotatedName = getHighlightedString(account.accountName, searchText),
                icon = Constants.DEFAULT_ACCOUNT_ICON,
                currency = account.currency,
                balance = currencyFormatter.format(account.balance).toString(),
                chips = chips
            )
        }
    }

    fun setSearchText(searchText: String) {
        _searchState.update {
            searchText
        }
    }

    fun setSortConfig(field: String, direction: SortDirection) {
        _sortConfigState.update {
            it.copy(
                sortField = field,
                sortDirection = direction
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