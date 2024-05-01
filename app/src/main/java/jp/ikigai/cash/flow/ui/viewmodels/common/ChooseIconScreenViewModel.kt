package jp.ikigai.cash.flow.ui.viewmodels.common

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import compose.icons.AllIcons
import compose.icons.TablerIcons
import jp.ikigai.cash.flow.data.screenStates.common.ChooseIconScreenState
import jp.ikigai.cash.flow.utils.getNumberFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ChooseIconScreenViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val defaultIcon : String = checkNotNull(savedStateHandle["defaultIcon"])

    private val icons: MutableList<ImageVector> = mutableListOf()

    private val formatter = getNumberFormatter()

    private val _state = MutableStateFlow(ChooseIconScreenState())
    val state: StateFlow<ChooseIconScreenState> = _state.asStateFlow()

    init {
        getIcons()
    }

    private fun getIcons() {
        icons.addAll(TablerIcons.AllIcons.sortedBy { icon ->  icon.name })
        _state.update {
            it.copy(
                icons = icons,
                iconCount = formatter.format(icons.size).toString(),
                loading = false
            )
        }
    }

    fun setSearchText(searchText: String) {
        _state.update {
            it.copy(loading = true)
        }
        if (searchText.isBlank()) {
            _state.update {
                it.copy(
                    icons = icons,
                    searchText = "",
                    loading = false
                )
            }
        } else {
            val filteredIcons = icons.filter { it.name.startsWith(searchText, ignoreCase = true) }
            _state.update {
                it.copy(
                    icons = filteredIcons,
                    searchText = searchText,
                    loading = false
                )
            }
        }
    }
}