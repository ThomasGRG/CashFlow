package jp.ikigai.cash.flow.ui.viewmodels.listing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import compose.icons.TablerIcons
import compose.icons.tablericons.Archive
import compose.icons.tablericons.ChartLine
import compose.icons.tablericons.FileText
import compose.icons.tablericons.History
import compose.icons.tablericons.Typography
import jp.ikigai.cash.flow.CashFlowDatabase
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.TemplateWithTransactionMetadata
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.Database
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.dto.ChipInfo
import jp.ikigai.cash.flow.data.dto.TemplateWithChips
import jp.ikigai.cash.flow.data.enums.SortDirection
import jp.ikigai.cash.flow.ui.screenStates.common.SortConfigState
import jp.ikigai.cash.flow.ui.screenStates.listing.TransactionTemplateScreenState
import jp.ikigai.cash.flow.utils.getCurrencyFormatterMap
import jp.ikigai.cash.flow.utils.getHighlightedString
import jp.ikigai.cash.flow.utils.getNumberFormatter
import kotlinx.coroutines.Dispatchers
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
    private val database: CashFlowDatabase = Database.database
) : ViewModel() {

    private var numberFormatter = getNumberFormatter()
    private var currencyFormatterMap = getCurrencyFormatterMap()

    private val _state = MutableStateFlow(TransactionTemplateScreenState())
    val state: StateFlow<TransactionTemplateScreenState> = _state.asStateFlow()

    private val _searchState = MutableStateFlow("")
    val searchState: StateFlow<String> = _searchState.asStateFlow()

    private val _sortConfigState = MutableStateFlow(
        SortConfigState(sortField = "transactionCount")
    )
    val sortConfigState: StateFlow<SortConfigState> = _sortConfigState.asStateFlow()

    private val _event: Channel<Event> = Channel(Int.MAX_VALUE)
    val event: Flow<Event> = _event.receiveAsFlow()

    init {
        getTemplates()
        getCount()
    }

    override fun onCleared() {
        super.onCleared()
        _event.close()
    }

    private fun getCount() = viewModelScope.launch {
        database.transactionTemplateQueries.count().asFlow().mapToOne(Dispatchers.IO)
            .collectLatest { count ->
                _state.update {
                    it.copy(
                        count = count,
                        countString = numberFormatter.format(count).toString()
                    )
                }
            }
    }

    fun canAddTransaction(): Boolean {
        val accountEmpty = database.accountQueries.count().executeAsOne() == 0L
        val categoryEmpty = database.categoryQueries.count().executeAsOne() == 0L
        val methodEmpty = database.methodQueries.count().executeAsOne() == 0L
        val canAddTransaction = !accountEmpty && !categoryEmpty && !methodEmpty
        if (!canAddTransaction) {
            showToastBarForRequiredFields(accountEmpty, methodEmpty, categoryEmpty)
        }
        return canAddTransaction
    }

    private fun showToastBarForRequiredFields(
        accountEmpty: Boolean,
        methodEmpty: Boolean,
        categoryEmpty: Boolean
    ) = viewModelScope.launch {
        if (accountEmpty && methodEmpty && categoryEmpty) {
            _event.send(Event.AccountCategoryMethodRequired)
        } else if (accountEmpty && methodEmpty) {
            _event.send(Event.AccountMethodRequired)
        } else if (categoryEmpty && methodEmpty) {
            _event.send(Event.CategoryMethodRequired)
        } else if (categoryEmpty && accountEmpty) {
            _event.send(Event.AccountCategoryRequired)
        } else if (categoryEmpty) {
            _event.send(Event.CategoryRequired)
        } else if (accountEmpty) {
            _event.send(Event.AccountRequired)
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
            _sortConfigState
                .onEach {
                    _state.update {
                        it.copy(
                            loading = true
                        )
                    }
                }
        ) { searchText, sortConfig ->
            Pair(searchText, sortConfig)
        }.flatMapLatest { (searchText, sortConfig) ->
            database
                .templateWithTransactionMetadataQueries
                .getTemplates(
                    searchText = searchText,
                    sortField = sortConfig.sortField,
                    sortDirection = sortConfig.sortDirection.name
                )
                .asFlow()
                .mapToList(Dispatchers.IO)
        }.collectLatest { templates ->
            _state.update { screenState ->
                screenState.copy(
                    templates = getTemplateWithIcons(templates, searchState.value),
                    loading = false
                )
            }
        }
    }

    private fun getTemplateWithIcons(
        templates: List<TemplateWithTransactionMetadata>,
        searchText: String
    ): List<TemplateWithChips> {
        val hasBeenUsedComparator = Instant.EPOCH.atZone(ZoneId.systemDefault())
        return templates.map { template ->
            val chips: MutableList<ChipInfo> = mutableListOf()

            if (template.templateTitle.isNotEmpty()) {
                chips.add(
                    ChipInfo(
                        resId = R.string.placeholder,
                        value = template.templateTitle,
                        icon = TablerIcons.Typography
                    )
                )
            }
            if (template.templateDescription.isNotEmpty()) {
                chips.add(
                    ChipInfo(
                        resId = R.string.placeholder,
                        value = template.templateDescription,
                        icon = TablerIcons.FileText
                    )
                )
            }
            if (template.templateCategoryName != null) {
                chips.add(
                    ChipInfo(
                        resId = R.string.placeholder,
                        value = template.templateCategoryName,
                        icon = template.templateCategoryIcon ?: TablerIcons.Archive
                    )
                )
            }
            if (template.templateCounterPartyName != null) {
                chips.add(
                    ChipInfo(
                        resId = R.string.placeholder,
                        value = template.templateCounterPartyName,
                        icon = Constants.DEFAULT_COUNTERPARTY_ICON
                    )
                )
            }
            if (template.templateMethodName != null) {
                chips.add(
                    ChipInfo(
                        resId = R.string.placeholder,
                        value = template.templateMethodName,
                        icon = Constants.DEFAULT_METHOD_ICON
                    )
                )
            }
            if (template.templateAccountName != null) {
                chips.add(
                    ChipInfo(
                        resId = R.string.placeholder,
                        value = template.templateAccountName,
                        icon = Constants.DEFAULT_ACCOUNT_ICON
                    )
                )
            }
            chips.add(
                ChipInfo(
                    resId = R.string.frequency_of_use_label,
                    value = numberFormatter.format(template.transactionCount).toString(),
                    icon = TablerIcons.ChartLine
                )
            )
            if (template.lastUsed != null && template.lastUsed > hasBeenUsedComparator) {
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
            val formattedAmount = if (template.templateAmount > 0) {
                val currencyFormatter = currencyFormatterMap[template.templateCurrency]
                currencyFormatter?.format(template.templateAmount)?.toString()
                    ?: numberFormatter.format(template.templateAmount).toString()
            } else {
                ""
            }
            TemplateWithChips(
                id = template.templateId,
                annotatedName = getHighlightedString(template.templateName, searchText),
                amount = formattedAmount,
                typeIcon = template.templateType.icon,
                typeIconColor = template.templateType.color,
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
        numberFormatter = getNumberFormatter(locale)
        currencyFormatterMap = getCurrencyFormatterMap(locale)
        _state.update {
            it.copy(
                locale = locale
            )
        }
    }
}