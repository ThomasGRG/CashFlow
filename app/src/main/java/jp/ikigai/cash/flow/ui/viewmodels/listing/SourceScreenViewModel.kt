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
import jp.ikigai.cash.flow.data.Database
import jp.ikigai.cash.flow.data.dto.ChipInfo
import jp.ikigai.cash.flow.data.dto.SourceListingDTO
import jp.ikigai.cash.flow.data.entity.Source
import jp.ikigai.cash.flow.ui.screenStates.listing.SourceScreenState
import jp.ikigai.cash.flow.utils.getCurrencyFormatter
import jp.ikigai.cash.flow.utils.getHighlightedString
import jp.ikigai.cash.flow.utils.getNumberFormatter
import jp.ikigai.cash.flow.utils.toZonedDateTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.util.Locale

class SourceScreenViewModel(
    private val realm: Realm = Realm.open(Database.config),
) : ViewModel() {

    private var formatter = getNumberFormatter()

    private val _state = MutableStateFlow(SourceScreenState())
    val state: StateFlow<SourceScreenState> = _state.asStateFlow()

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
                    countString = formatter.format(count).toString()
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getSources() = viewModelScope.launch {
        state.flatMapLatest {
            val query = realm.query<Source>(
                if (it.searchText.isBlank()) {
                    TRUE_PREDICATE
                } else {
                    "name CONTAINS[c] '${it.searchText.trim()}'"
                }
            )
            if (it.sortField == "balance") {
                query.sort(
                    Pair("currency", Sort.ASCENDING),
                    Pair(it.sortField, it.sortDirection)
                ).asFlow()
            } else {
                query.sort(
                    Pair(it.sortField, it.sortDirection)
                ).asFlow()
            }
        }.collectLatest { changes ->
            _state.update { screenState ->
                screenState.copy(
                    sources = mapToDTO(changes.list, screenState.searchText, screenState.locale),
                    loading = false,
                    countString = formatter.format(screenState.count).toString()
                )
            }
        }
    }

    private fun mapToDTO(
        sources: List<Source>,
        searchText: String,
        locale: Locale?
    ): List<SourceListingDTO> {
        return sources.map { source ->
            val currencyFormatter = getCurrencyFormatter(locale, source.currency)
            val chips: MutableList<ChipInfo> = mutableListOf()
            chips.add(
                ChipInfo(
                    resId = R.string.frequency_of_use_label,
                    value = formatter.format(source.frequency).toString(),
                    icon = TablerIcons.ChartLine
                )
            )
            if (source.lastUsed > 0) {
                chips.add(
                    ChipInfo(
                        resId = R.string.last_used_datetime_label,
                        value = source.lastUsed.toZonedDateTime()
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
                icon = source.icon,
                currency = source.currency,
                balance = currencyFormatter.format(source.balance).toString(),
                chips = chips
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

    fun setSortInfo(field: String, direction: Sort) {
        _state.update {
            it.copy(
                sortDirection = direction,
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