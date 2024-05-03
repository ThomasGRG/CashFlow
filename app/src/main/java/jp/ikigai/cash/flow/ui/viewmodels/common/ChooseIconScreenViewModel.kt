package jp.ikigai.cash.flow.ui.viewmodels.common

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import compose.icons.AllIcons
import compose.icons.TablerIcons
import jp.ikigai.cash.flow.ui.screenStates.common.ChooseIconScreenState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class ChooseIconScreenViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val defaultIcon: String = checkNotNull(savedStateHandle["defaultIcon"])

    private val icons: MutableList<ImageVector> = mutableListOf()

    private val _state = MutableStateFlow(ChooseIconScreenState())
    val state: StateFlow<ChooseIconScreenState> = _state.asStateFlow()

    private val _searchTextMutableStateFlow = MutableStateFlow("")
    private val searchTextStateFlow = _searchTextMutableStateFlow.asStateFlow()

    init {
        getIcons()
        filterIcons()
    }

    private fun filterIcons() = viewModelScope.launch {
        searchTextStateFlow
            .debounce(500L)
            .collectLatest { searchText ->
                val filteredIcons = if (searchText.isBlank()) {
                    icons
                } else {
                    icons.filter { it.name.startsWith(searchText, ignoreCase = true) }
                }
                _state.update {
                    it.copy(
                        icons = filteredIcons,
                        searching = false
                    )
                }
            }
    }

    private fun getIcons() {
        icons.addAll(TablerIcons.AllIcons.sortedBy { icon -> icon.name })
        _state.update {
            it.copy(
                icons = icons,
                iconCount = icons.size,
                loading = false
            )
        }
    }

    fun setSearchText(searchText: String) {
        _state.update {
            it.copy(
                searching = true,
                searchText = searchText,
            )
        }
        _searchTextMutableStateFlow.update {
            searchText
        }
    }
}