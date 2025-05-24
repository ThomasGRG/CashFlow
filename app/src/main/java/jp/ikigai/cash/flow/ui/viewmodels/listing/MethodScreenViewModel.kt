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
import jp.ikigai.cash.flow.data.store.entity.Method
import jp.ikigai.cash.flow.data.store.entity.Method_
import jp.ikigai.cash.flow.ui.screenStates.common.SortOptionsState
import jp.ikigai.cash.flow.ui.screenStates.listing.MethodScreenState
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

class MethodScreenViewModel(
    store: BoxStore = DataStore.store
) : ViewModel() {

    private var formatter = getNumberFormatter()

    private val _state = MutableStateFlow(MethodScreenState())
    val state: StateFlow<MethodScreenState> = _state.asStateFlow()

    private val _searchState = MutableStateFlow("")
    val searchState: StateFlow<String> = _searchState.asStateFlow()

    private val _sortOptionsState = MutableStateFlow(
        SortOptionsState<Method>(
            sortField = Method_.lastUsed
        )
    )
    val sortOptionsState: StateFlow<SortOptionsState<Method>> = _sortOptionsState.asStateFlow()

    private val methodBox: Box<Method> = store.boxFor()

    private val methodCountQuery = methodBox.query().build()

    init {
        getMethods()
        getCount()
    }

    override fun onCleared() {
        super.onCleared()
        methodCountQuery.close()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getCount() = viewModelScope.launch {
        methodCountQuery.flow().collectLatest { methods ->
            _state.update {
                it.copy(
                    count = methods.size,
                    countString = formatter.format(methods.size).toString()
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun getMethods() = viewModelScope.launch {
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
            val methodQueryBuilder = if (searchText.isBlank()) {
                methodBox.query()
            } else {
                methodBox
                    .query(
                        Method_.name.contains(
                            searchText,
                            QueryBuilder.StringOrder.CASE_INSENSITIVE
                        )
                    )
            }

            val query = methodQueryBuilder
                .order(sortOptions.sortField, sortOptions.sortFlags)
                .build()

            query.flow().onCompletion {
                query.close()
            }
        }.collectLatest { methods ->
            _state.update { screenState ->
                screenState.copy(
                    methods = mapToDTO(methods, searchState.value),
                    loading = false,
                )
            }
        }
    }

    private fun mapToDTO(methods: List<Method>, searchText: String): List<CommonListingDTO> {
        val hasBeenUsedComparator = Instant.EPOCH.atZone(ZoneId.systemDefault())
        return methods.map { method ->
            val chips: MutableList<ChipInfo> = mutableListOf()
            chips.add(
                ChipInfo(
                    resId = R.string.frequency_of_use_label,
                    value = formatter.format(method.frequency).toString(),
                    icon = TablerIcons.ChartLine
                )
            )
            if (method.lastUsed > hasBeenUsedComparator) {
                chips.add(
                    ChipInfo(
                        resId = R.string.last_used_datetime_label,
                        value = method.lastUsed
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
                id = method.id,
                annotatedName = getHighlightedString(method.name, searchText),
                icon = Constants.DEFAULT_METHOD_ICON,
                chips = chips
            )
        }
    }

    fun setSearchText(searchText: String) {
        _searchState.update {
            searchText
        }
    }

    fun setSortOptions(field: Property<Method>, flags: Int) {
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