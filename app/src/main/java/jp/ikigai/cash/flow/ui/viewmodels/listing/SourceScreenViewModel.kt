package jp.ikigai.cash.flow.ui.viewmodels.listing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import compose.icons.TablerIcons
import compose.icons.tablericons.ChartLine
import compose.icons.tablericons.History
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.Sort
import io.realm.kotlin.query.TRUE_PREDICATE
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.Database
import jp.ikigai.cash.flow.data.dto.ChipInfo
import jp.ikigai.cash.flow.data.dto.SourceListingDTO
import jp.ikigai.cash.flow.data.entity.Source
import jp.ikigai.cash.flow.ui.screenStates.common.SortOptionsScreenState
import jp.ikigai.cash.flow.ui.screenStates.listing.SourceScreenState
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
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class SourceScreenViewModel(
    private val realm: Realm = Realm.open(Database.config),
) : ViewModel() {

    private var numberFormatter = getNumberFormatter()
    private var currencyFormatterMap = getCurrencyFormatterMap()

    private val _state = MutableStateFlow(SourceScreenState())
    val state: StateFlow<SourceScreenState> = _state.asStateFlow()

    private val _searchState = MutableStateFlow("")
    val searchState: StateFlow<String> = _searchState.asStateFlow()

    private val _sortOptionsState = MutableStateFlow(SortOptionsScreenState())
    val sortOptionsState: StateFlow<SortOptionsScreenState> = _sortOptionsState.asStateFlow()

    init {
        getSources()
        getCount()
    }

    override fun onCleared() {
        super.onCleared()
        realm.close()
    }

    private fun getCount() = viewModelScope.launch {
        realm.query<Source>().count().asFlow().collectLatest { count ->
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
            val query = realm.query<Source>(
                if (searchText.isBlank()) {
                    TRUE_PREDICATE
                } else {
                    "name CONTAINS[c] '${searchText.trim()}'"
                }
            )
            if (sortOptions.sortField == "balance") {
                query.sort(
                    Pair("currency", Sort.ASCENDING),
                    Pair(sortOptions.sortField, sortOptions.sortDirection)
                ).asFlow()
            } else {
                query.sort(
                    Pair(sortOptions.sortField, sortOptions.sortDirection)
                ).asFlow()
            }
        }.collectLatest { changes ->
            _state.update { screenState ->
                screenState.copy(
                    sources = mapToDTO(changes.list, searchState.value),
                    loading = false
                )
            }
        }
    }

    private fun mapToDTO(
        sources: List<Source>,
        searchText: String
    ): List<SourceListingDTO> {
        val hasBeenUsedComparator = Instant.EPOCH.atZone(ZoneId.systemDefault())
        return sources.map { source ->
            val currencyFormatter = currencyFormatterMap.getValue(source.currency)
            val chips: MutableList<ChipInfo> = mutableListOf()
            chips.add(
                ChipInfo(
                    resId = R.string.frequency_of_use_label,
                    value = numberFormatter.format(source.frequency).toString(),
                    icon = TablerIcons.ChartLine
                )
            )
            if (source.lastUsed > hasBeenUsedComparator) {
                chips.add(
                    ChipInfo(
                        resId = R.string.last_used_datetime_label,
                        value = source.lastUsed
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
            SourceListingDTO(
                uuid = source.uuid,
                annotatedName = getHighlightedString(source.name, searchText),
                icon = Constants.DEFAULT_SOURCE_ICON,
                currency = source.currency,
                balance = currencyFormatter.format(source.balance).toString(),
                chips = chips
            )
        }
    }

    fun setSearchText(searchText: String) {
        _searchState.update {
            searchText
        }
    }

    fun setSortOptions(field: String, direction: Sort) {
        _sortOptionsState.update {
            it.copy(
                sortDirection = direction,
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