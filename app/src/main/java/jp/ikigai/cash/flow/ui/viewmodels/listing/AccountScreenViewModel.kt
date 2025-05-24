package jp.ikigai.cash.flow.ui.viewmodels.listing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import compose.icons.TablerIcons
import compose.icons.tablericons.ChartLine
import compose.icons.tablericons.History
import io.objectbox.Box
import io.objectbox.BoxStore
import io.objectbox.Property
import io.objectbox.kotlin.boxFor
import io.objectbox.kotlin.flow
import io.objectbox.query.QueryBuilder
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.dto.AccountListingDTO
import jp.ikigai.cash.flow.data.dto.ChipInfo
import jp.ikigai.cash.flow.data.store.DataStore
import jp.ikigai.cash.flow.data.store.entity.Account
import jp.ikigai.cash.flow.data.store.entity.Account_
import jp.ikigai.cash.flow.ui.screenStates.common.SortOptionsState
import jp.ikigai.cash.flow.ui.screenStates.listing.AccountScreenState
import jp.ikigai.cash.flow.utils.getCurrencyFormatterMap
import jp.ikigai.cash.flow.utils.getHighlightedString
import jp.ikigai.cash.flow.utils.getNumberFormatter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class AccountScreenViewModel(
    store: BoxStore = DataStore.store
) : ViewModel() {

    private var numberFormatter = getNumberFormatter()
    private var currencyFormatterMap = getCurrencyFormatterMap()

    private val _state = MutableStateFlow(AccountScreenState())
    val state: StateFlow<AccountScreenState> = _state.asStateFlow()

    private val _searchState = MutableStateFlow("")
    val searchState: StateFlow<String> = _searchState.asStateFlow()

    private val _sortOptionsState = MutableStateFlow(
        SortOptionsState<Account>(
            sortField = Account_.lastUsed
        )
    )
    val sortOptionsState: StateFlow<SortOptionsState<Account>> = _sortOptionsState.asStateFlow()

    private val accountBox: Box<Account> = store.boxFor()

    private val accountCountQuery = accountBox.query().build()

    init {
        getSources()
        getCount()
    }

    override fun onCleared() {
        super.onCleared()
        accountCountQuery.close()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getCount() = viewModelScope.launch {
        accountCountQuery.flow().collectLatest { accounts ->
            _state.update {
                it.copy(
                    count = accounts.size,
                    countString = numberFormatter.format(accounts.size).toString()
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
            _sortOptionsState
                .onEach {
                    _state.update {
                        it.copy(
                            loading = true
                        )
                    }
                }
        ) { searchText, sortOptions ->
            Pair(searchText, sortOptions)
        }.flatMapLatest { (searchText, sortOptions) ->
            val accountQueryBuilder = if (searchText.isBlank()) {
                accountBox.query()
            } else {
                accountBox
                    .query(
                        Account_.name.contains(
                            searchText,
                            QueryBuilder.StringOrder.CASE_INSENSITIVE
                        )
                    )
            }
            if (sortOptions.sortField == Account_.balance) {
                accountQueryBuilder
                    .order(Account_.currency)
                    .order(sortOptions.sortField, sortOptions.sortFlags)
            } else {
                accountQueryBuilder.order(sortOptions.sortField, sortOptions.sortFlags)
            }
            val query = accountQueryBuilder.build()
            query.flow().onCompletion {
                query.close()
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
        accounts: List<Account>,
        searchText: String
    ): List<AccountListingDTO> {
        val hasBeenUsedComparator = Instant.EPOCH.atZone(ZoneId.systemDefault())
        return accounts.map { account ->
            val currencyFormatter = currencyFormatterMap.getValue(account.currency)
            val chips: MutableList<ChipInfo> = mutableListOf()
            chips.add(
                ChipInfo(
                    resId = R.string.frequency_of_use_label,
                    value = numberFormatter.format(account.frequency).toString(),
                    icon = TablerIcons.ChartLine
                )
            )
            if (account.lastUsed > hasBeenUsedComparator) {
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
                id = account.id,
                annotatedName = getHighlightedString(account.name, searchText),
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

    fun setSortOptions(field: Property<Account>, flags: Int) {
        _sortOptionsState.update {
            it.copy(
                sortFlags = flags,
                sortField = field
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