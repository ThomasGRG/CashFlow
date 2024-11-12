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
import jp.ikigai.cash.flow.data.entity.Method
import jp.ikigai.cash.flow.ui.screenStates.listing.MethodScreenState
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

class MethodScreenViewModel(
    private val realm: Realm = Realm.open(Database.config),
) : ViewModel() {

    private var formatter = getNumberFormatter()

    private val _state = MutableStateFlow(MethodScreenState())
    val state: StateFlow<MethodScreenState> = _state.asStateFlow()

    init {
        getMethods()
        getCount()
    }

    override fun onCleared() {
        super.onCleared()
        realm.close()
    }

    private fun getCount() = viewModelScope.launch {
        realm.query<Method>().count().asFlow().collectLatest { count ->
            _state.update {
                it.copy(
                    count = count,
                    countString = formatter.format(count).toString()
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getMethods() = viewModelScope.launch {
        state.flatMapLatest {
            realm.query<Method>(
                if (it.searchText.isBlank()) {
                    TRUE_PREDICATE
                } else {
                    "name CONTAINS[c] '${it.searchText.trim()}'"
                }
            )
                .sort(it.sortField, it.sortDirection)
                .asFlow()
        }.collectLatest { changes ->
            _state.update { screenState ->
                screenState.copy(
                    methods = mapToDTO(changes.list, screenState.searchText),
                    loading = false,
                    countString = formatter.format(screenState.count).toString()
                )
            }
        }
    }

    private fun mapToDTO(methods: List<Method>, searchText: String): List<CommonListingDTO> {
        return methods.map { method ->
            val chips: MutableList<ChipInfo> = mutableListOf()
            chips.add(
                ChipInfo(
                    resId = R.string.frequency_of_use_label,
                    value = formatter.format(method.frequency).toString(),
                    icon = TablerIcons.ChartLine
                )
            )
            if (method.lastUsed > 0) {
                chips.add(
                    ChipInfo(
                        resId = R.string.last_used_datetime_label,
                        value = method.lastUsed.toZonedDateTime()
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
                uuid = method.uuid,
                annotatedName = getHighlightedString(method.name, searchText),
                icon = method.icon,
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