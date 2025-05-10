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
import jp.ikigai.cash.flow.data.dto.CommonListingDTO
import jp.ikigai.cash.flow.data.entity.Category
import jp.ikigai.cash.flow.ui.screenStates.common.SortOptionsScreenState
import jp.ikigai.cash.flow.ui.screenStates.listing.CategoryScreenState
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

class CategoryScreenViewModel(
    private val realm: Realm = Realm.open(Database.config),
) : ViewModel() {

    private var formatter = getNumberFormatter()

    private val _state = MutableStateFlow(CategoryScreenState())
    val state: StateFlow<CategoryScreenState> = _state.asStateFlow()

    private val _searchState = MutableStateFlow("")
    val searchState: StateFlow<String> = _searchState.asStateFlow()

    private val _sortOptionsState = MutableStateFlow(SortOptionsScreenState())
    val sortOptionsState: StateFlow<SortOptionsScreenState> = _sortOptionsState.asStateFlow()

    init {
        getCategories()
        getCategoryCount()
    }

    override fun onCleared() {
        super.onCleared()
        realm.close()
    }

    private fun getCategoryCount() = viewModelScope.launch {
        realm.query<Category>().count().asFlow().collectLatest { count ->
            _state.update {
                it.copy(
                    count = count,
                    countString = formatter.format(count).toString()
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun getCategories() = viewModelScope.launch {
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
            realm.query<Category>(
                if (searchText.isBlank()) {
                    TRUE_PREDICATE
                } else {
                    "name CONTAINS[c] '${searchText.trim()}'"
                }
            )
                .sort(sortOptions.sortField, sortOptions.sortDirection)
                .asFlow()
        }.collectLatest { changes ->
            _state.update { screenState ->
                screenState.copy(
                    categories = mapToDTO(changes.list, searchState.value),
                    loading = false
                )
            }
        }
    }

    private fun mapToDTO(categories: List<Category>, searchText: String): List<CommonListingDTO> {
        val hasBeenUsedComparator = Instant.EPOCH.atZone(ZoneId.systemDefault())
        return categories.map { category ->
            val chips: MutableList<ChipInfo> = mutableListOf()
            chips.add(
                ChipInfo(
                    resId = R.string.frequency_of_use_label,
                    value = formatter.format(category.frequency).toString(),
                    icon = TablerIcons.ChartLine
                )
            )
            if (category.lastUsed > hasBeenUsedComparator) {
                chips.add(
                    ChipInfo(
                        resId = R.string.last_used_datetime_label,
                        value = category.lastUsed
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
                uuid = category.uuid,
                annotatedName = getHighlightedString(category.name, searchText),
                icon = category.icon,
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
        formatter = getNumberFormatter(locale)
        _state.update {
            it.copy(
                locale = locale
            )
        }
    }
}