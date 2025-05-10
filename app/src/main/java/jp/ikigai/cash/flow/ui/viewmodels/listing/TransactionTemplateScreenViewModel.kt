package jp.ikigai.cash.flow.ui.viewmodels.listing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import compose.icons.TablerIcons
import compose.icons.tablericons.ChartLine
import compose.icons.tablericons.FileText
import compose.icons.tablericons.History
import compose.icons.tablericons.Typography
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.Sort
import io.realm.kotlin.query.TRUE_PREDICATE
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.Database
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.dto.ChipInfo
import jp.ikigai.cash.flow.data.dto.TransactionTemplateWithIcons
import jp.ikigai.cash.flow.data.entity.Category
import jp.ikigai.cash.flow.data.entity.Method
import jp.ikigai.cash.flow.data.entity.Source
import jp.ikigai.cash.flow.data.entity.TransactionTemplate
import jp.ikigai.cash.flow.ui.screenStates.common.SortOptionsScreenState
import jp.ikigai.cash.flow.ui.screenStates.listing.TransactionTemplateScreenState
import jp.ikigai.cash.flow.utils.getCurrencyFormatterMap
import jp.ikigai.cash.flow.utils.getHighlightedString
import jp.ikigai.cash.flow.utils.getNumberFormatter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class TransactionTemplateScreenViewModel(
    private val realm: Realm = Realm.open(Database.config),
) : ViewModel() {

    private var numberFormatter = getNumberFormatter()
    private var currencyFormatterMap = getCurrencyFormatterMap()

    private val _state = MutableStateFlow(TransactionTemplateScreenState())
    val state: StateFlow<TransactionTemplateScreenState> = _state.asStateFlow()

    private val _searchState = MutableStateFlow("")
    val searchState: StateFlow<String> = _searchState.asStateFlow()

    private val _sortOptionsState = MutableStateFlow(SortOptionsScreenState())
    val sortOptionsState: StateFlow<SortOptionsScreenState> = _sortOptionsState.asStateFlow()

    private val _event: Channel<Event> = Channel(Int.MAX_VALUE)
    val event: Flow<Event> = _event.receiveAsFlow()

    init {
        getTemplates()
        getCount()
    }

    override fun onCleared() {
        super.onCleared()
        realm.close()
        _event.close()
    }

    private fun getCount() = viewModelScope.launch {
        realm.query<TransactionTemplate>().count().asFlow().collectLatest { count ->
            _state.update {
                it.copy(
                    count = count,
                    countString = numberFormatter.format(count).toString()
                )
            }
        }
    }

    fun canAddTransaction(): Boolean {
        val sourceEmpty = realm.query<Source>().count().find() == 0L
        val categoryEmpty = realm.query<Category>().count().find() == 0L
        val methodEmpty = realm.query<Method>().count().find() == 0L
        val canAddTransaction = !sourceEmpty && !categoryEmpty && !methodEmpty
        if (!canAddTransaction) {
            showToastBarForRequiredFields(sourceEmpty, methodEmpty, categoryEmpty)
        }
        return canAddTransaction
    }

    private fun showToastBarForRequiredFields(
        sourceEmpty: Boolean,
        methodEmpty: Boolean,
        categoryEmpty: Boolean
    ) = viewModelScope.launch {
        if (sourceEmpty && methodEmpty && categoryEmpty) {
            _event.send(Event.CategoryMethodSourceRequired)
        } else if (sourceEmpty && methodEmpty) {
            _event.send(Event.MethodSourceRequired)
        } else if (categoryEmpty && methodEmpty) {
            _event.send(Event.CategoryMethodRequired)
        } else if (categoryEmpty && sourceEmpty) {
            _event.send(Event.CategorySourceRequired)
        } else if (categoryEmpty) {
            _event.send(Event.CategoryRequired)
        } else if (sourceEmpty) {
            _event.send(Event.SourceRequired)
        } else if (methodEmpty) {
            _event.send(Event.MethodRequired)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun getTemplates() = viewModelScope.launch {
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
            realm.query<TransactionTemplate>(
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
                    templates = getTemplateWithIcons(changes.list, searchState.value),
                    loading = false
                )
            }
        }
    }

    private fun getTemplateWithIcons(
        templates: List<TransactionTemplate>,
        searchText: String
    ): List<TransactionTemplateWithIcons> {
        val hasBeenUsedComparator = Instant.EPOCH.atZone(ZoneId.systemDefault())
        return templates.map { template ->
            val category = template.category
            val counterParty = template.counterParty
            val method = template.method
            val source = template.source
            val chips: MutableList<ChipInfo> = mutableListOf()
            if (template.title.isNotEmpty()) {
                chips.add(
                    ChipInfo(
                        resId = R.string.placeholder,
                        value = template.title,
                        icon = TablerIcons.Typography
                    )
                )
            }
            if (template.description.isNotEmpty()) {
                chips.add(
                    ChipInfo(
                        resId = R.string.placeholder,
                        value = template.description,
                        icon = TablerIcons.FileText
                    )
                )
            }
            if (category != null) {
                chips.add(
                    ChipInfo(
                        resId = R.string.placeholder,
                        value = category.name,
                        icon = category.icon
                    )
                )
            }
            if (counterParty != null) {
                chips.add(
                    ChipInfo(
                        resId = R.string.placeholder,
                        value = counterParty.name,
                        icon = Constants.DEFAULT_COUNTERPARTY_ICON
                    )
                )
            }
            if (method != null) {
                chips.add(
                    ChipInfo(
                        resId = R.string.placeholder,
                        value = method.name,
                        icon = Constants.DEFAULT_METHOD_ICON
                    )
                )
            }
            if (source != null) {
                chips.add(
                    ChipInfo(
                        resId = R.string.placeholder,
                        value = source.name,
                        icon = Constants.DEFAULT_SOURCE_ICON
                    )
                )
            }
            chips.add(
                ChipInfo(
                    resId = R.string.frequency_of_use_label,
                    value = numberFormatter.format(template.frequency).toString(),
                    icon = TablerIcons.ChartLine
                )
            )
            if (template.lastUsed > hasBeenUsedComparator) {
                chips.add(
                    ChipInfo(
                        resId = R.string.last_used_datetime_label,
                        value = template.lastUsed
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
            val formattedAmount = if (template.amount > 0) {
                if (source != null) {
                    currencyFormatterMap.getValue(source.currency).format(template.amount)
                        .toString()
                } else {
                    numberFormatter.format(template.amount).toString()
                }
            } else {
                ""
            }
            TransactionTemplateWithIcons(
                uuid = template.uuid,
                annotatedName = getHighlightedString(template.name, searchText),
                amount = formattedAmount,
                typeIcon = template.type.icon,
                typeIconColor = template.type.color,
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