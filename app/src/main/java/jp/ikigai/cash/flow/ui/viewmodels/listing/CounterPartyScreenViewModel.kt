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
import jp.ikigai.cash.flow.data.dto.ChipInfo
import jp.ikigai.cash.flow.data.dto.CommonListingDTO
import jp.ikigai.cash.flow.data.store.DataStore
import jp.ikigai.cash.flow.data.store.entity.CounterParty
import jp.ikigai.cash.flow.data.store.entity.CounterParty_
import jp.ikigai.cash.flow.ui.screenStates.common.SortOptionsState
import jp.ikigai.cash.flow.ui.screenStates.listing.CounterPartyScreenState
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

class CounterPartyScreenViewModel(
    store: BoxStore = DataStore.store
) : ViewModel() {

    private var formatter = getNumberFormatter()

    private val _state = MutableStateFlow(CounterPartyScreenState())
    val state: StateFlow<CounterPartyScreenState> = _state.asStateFlow()

    private val _searchState = MutableStateFlow("")
    val searchState: StateFlow<String> = _searchState.asStateFlow()

    private val _sortOptionsState = MutableStateFlow(
        SortOptionsState<CounterParty>(
            sortField = CounterParty_.lastUsed
        )
    )
    val sortOptionsState: StateFlow<SortOptionsState<CounterParty>> =
        _sortOptionsState.asStateFlow()

    private val counterPartyBox: Box<CounterParty> = store.boxFor()

    private val counterPartyCountQuery = counterPartyBox.query().build()

    init {
        getCounterParties()
        getCount()
    }

    override fun onCleared() {
        super.onCleared()
        counterPartyCountQuery.close()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getCount() = viewModelScope.launch {
        counterPartyCountQuery.flow().collectLatest { counterParties ->
            _state.update {
                it.copy(
                    count = counterParties.size,
                    countString = formatter.format(counterParties.size).toString()
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun getCounterParties() = viewModelScope.launch {
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
            val counterPartyQueryBuilder = if (searchText.isBlank()) {
                counterPartyBox.query()
            } else {
                counterPartyBox
                    .query(
                        CounterParty_.name.contains(
                            searchText,
                            QueryBuilder.StringOrder.CASE_INSENSITIVE
                        )
                    )
            }

            val query = counterPartyQueryBuilder
                .order(sortOptions.sortField, sortOptions.sortFlags)
                .build()

            query.flow().onCompletion {
                query.close()
            }
        }.collectLatest { counterParties ->
            _state.update { screenState ->
                screenState.copy(
                    counterParties = mapToDTO(counterParties, searchState.value),
                    loading = false
                )
            }
        }
    }

    private fun mapToDTO(
        counterParties: List<CounterParty>,
        searchText: String
    ): List<CommonListingDTO> {
        val hasBeenUsedComparator = Instant.EPOCH.atZone(ZoneId.systemDefault())
        return counterParties.map { counterParty ->
            val chips: MutableList<ChipInfo> = mutableListOf()
            chips.add(
                ChipInfo(
                    resId = R.string.frequency_of_use_label,
                    value = formatter.format(counterParty.frequency).toString(),
                    icon = TablerIcons.ChartLine
                )
            )
            if (counterParty.lastUsed > hasBeenUsedComparator) {
                chips.add(
                    ChipInfo(
                        resId = R.string.last_used_datetime_label,
                        value = counterParty.lastUsed
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
            CommonListingDTO(
                id = counterParty.id,
                annotatedName = getHighlightedString(counterParty.name, searchText),
                icon = Constants.DEFAULT_COUNTERPARTY_ICON,
                chips = chips
            )
        }
    }

    fun setSearchText(searchText: String) {
        _searchState.update {
            searchText
        }
    }

    fun setSortOptions(field: Property<CounterParty>, flags: Int) {
        _sortOptionsState.update {
            it.copy(
                sortFlags = flags,
                sortField = field
            )
        }
    }

    fun setLocale(locale: Locale?) {
        formatter = getNumberFormatter(locale)
        _state.update {
            it.copy(
                locale = locale
            )
        }
    }
}