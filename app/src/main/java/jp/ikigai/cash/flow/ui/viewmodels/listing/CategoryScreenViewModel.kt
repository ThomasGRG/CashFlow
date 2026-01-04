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

package jp.ikigai.cash.flow.ui.viewmodels.listing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import compose.icons.TablerIcons
import compose.icons.tablericons.ChartLine
import compose.icons.tablericons.History
import jp.ikigai.cash.flow.CashFlowDatabase
import jp.ikigai.cash.flow.CategoryWithTransactionMetadata
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Database
import jp.ikigai.cash.flow.data.dto.ChipInfo
import jp.ikigai.cash.flow.data.dto.CommonListingDTO
import jp.ikigai.cash.flow.data.enums.SortDirection
import jp.ikigai.cash.flow.data.preferences.CashFlowPreferencesDataStore
import jp.ikigai.cash.flow.ui.screenStates.common.SortConfigState
import jp.ikigai.cash.flow.ui.screenStates.listing.CategoryScreenState
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

class CategoryScreenViewModel(
    private val preferencesDataStore: CashFlowPreferencesDataStore,
    private val database: CashFlowDatabase = Database.database
) : ViewModel() {

    private var formatter = getNumberFormatter()

    private val _state = MutableStateFlow(CategoryScreenState())
    val state: StateFlow<CategoryScreenState> = _state.asStateFlow()

    private val _searchState = MutableStateFlow("")
    val searchState: StateFlow<String> = _searchState.asStateFlow()

    private val _sortConfigState = MutableStateFlow(
        SortConfigState(sortField = "transactionCount")
    )
    val sortConfigState: StateFlow<SortConfigState> = _sortConfigState.asStateFlow()

    init {
        getSortPreferences()
        getCategories()
        getCount()
    }

    private fun getSortPreferences() = viewModelScope.launch {
        preferencesDataStore
            .getCategoriesScreenSortConfig()
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
        database.categoryQueries.count().asFlow().mapToOne(Dispatchers.IO).collectLatest { count ->
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
            _sortConfigState
                .onEach { sortState ->
                    _state.update {
                        it.copy(
                            loading = true
                        )
                    }
                    preferencesDataStore.saveCategoriesScreenSortConfig(sortState)
                }
        ) { searchText, sortConfig ->
            Pair(searchText, sortConfig)
        }.flatMapLatest { (searchText, sortConfig) ->
            database.categoryWithTransactionMetadataQueries.getCategories(
                searchText = searchText,
                sortField = sortConfig.sortField,
                sortDirection = sortConfig.sortDirection.name
            )
                .asFlow()
                .mapToList(Dispatchers.IO)
        }.collectLatest { categories ->
            _state.update { screenState ->
                screenState.copy(
                    categories = mapToDTO(categories, searchState.value),
                    loading = false
                )
            }
        }
    }

    private fun mapToDTO(
        categories: List<CategoryWithTransactionMetadata>,
        searchText: String
    ): List<CommonListingDTO> {
        val hasBeenUsedComparator = Instant.EPOCH.atZone(ZoneId.systemDefault())
        return categories.map { category ->
            val chips: MutableList<ChipInfo> = mutableListOf()
            chips.add(
                ChipInfo(
                    resId = R.string.frequency_of_use_label,
                    value = formatter.format(category.transactionCount).toString(),
                    icon = TablerIcons.ChartLine
                )
            )
            if (category.lastUsed != null && category.lastUsed > hasBeenUsedComparator) {
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
                id = category.categoryId,
                annotatedName = getHighlightedString(category.categoryName, searchText),
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

    fun setSortConfig(field: String, direction: SortDirection) {
        _sortConfigState.update {
            it.copy(
                sortField = field,
                sortDirection = direction
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