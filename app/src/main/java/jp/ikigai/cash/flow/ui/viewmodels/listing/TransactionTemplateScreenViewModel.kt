package jp.ikigai.cash.flow.ui.viewmodels.listing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import compose.icons.TablerIcons
import compose.icons.tablericons.ChartLine
import compose.icons.tablericons.FileText
import compose.icons.tablericons.History
import compose.icons.tablericons.Typography
import io.objectbox.Box
import io.objectbox.BoxStore
import io.objectbox.Property
import io.objectbox.kotlin.boxFor
import io.objectbox.kotlin.flow
import io.objectbox.query.QueryBuilder
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.dto.ChipInfo
import jp.ikigai.cash.flow.data.dto.TemplateWithChips
import jp.ikigai.cash.flow.data.store.DataStore
import jp.ikigai.cash.flow.data.store.entity.Account
import jp.ikigai.cash.flow.data.store.entity.Category
import jp.ikigai.cash.flow.data.store.entity.Method
import jp.ikigai.cash.flow.data.store.entity.TransactionTemplate
import jp.ikigai.cash.flow.data.store.entity.TransactionTemplate_
import jp.ikigai.cash.flow.ui.screenStates.common.SortOptionsState
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
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class TransactionTemplateScreenViewModel(
    store: BoxStore = DataStore.store
) : ViewModel() {

    private var numberFormatter = getNumberFormatter()
    private var currencyFormatterMap = getCurrencyFormatterMap()

    private val _state = MutableStateFlow(TransactionTemplateScreenState())
    val state: StateFlow<TransactionTemplateScreenState> = _state.asStateFlow()

    private val _searchState = MutableStateFlow("")
    val searchState: StateFlow<String> = _searchState.asStateFlow()

    private val _sortOptionsState = MutableStateFlow(
        SortOptionsState<TransactionTemplate>(
            sortField = TransactionTemplate_.lastUsed
        )
    )
    val sortOptionsState: StateFlow<SortOptionsState<TransactionTemplate>> =
        _sortOptionsState.asStateFlow()

    private val accountBox: Box<Account> = store.boxFor()
    private val categoryBox: Box<Category> = store.boxFor()
    private val methodBox: Box<Method> = store.boxFor()
    private val templateBox: Box<TransactionTemplate> = store.boxFor()

    private val _event: Channel<Event> = Channel(Int.MAX_VALUE)
    val event: Flow<Event> = _event.receiveAsFlow()

    private val templateCountQuery = templateBox.query().build()

    init {
        getTemplates()
        getCount()
    }

    override fun onCleared() {
        super.onCleared()
        _event.close()
        templateCountQuery.close()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getCount() = viewModelScope.launch {
        templateCountQuery.flow().collectLatest { templates ->
            _state.update {
                it.copy(
                    count = templates.size,
                    countString = numberFormatter.format(templates.size).toString()
                )
            }
        }
    }

    fun canAddTransaction(): Boolean {
        val accountEmpty = accountBox.isEmpty
        val categoryEmpty = categoryBox.isEmpty
        val methodEmpty = methodBox.isEmpty
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
            val templateQueryBuilder = if (searchText.isBlank()) {
                templateBox.query()
            } else {
                templateBox
                    .query(
                        TransactionTemplate_.name.contains(
                            searchText,
                            QueryBuilder.StringOrder.CASE_INSENSITIVE
                        )
                    )
            }

            val query = templateQueryBuilder
                .order(sortOptions.sortField, sortOptions.sortFlags)
                .build()

            query.flow().onCompletion {
                query.close()
            }
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
        templates: List<TransactionTemplate>,
        searchText: String
    ): List<TemplateWithChips> {
        val hasBeenUsedComparator = Instant.EPOCH.atZone(ZoneId.systemDefault())
        return templates.map { template ->
            val account = template.account.target
            val category = template.category.target
            val counterParty = template.counterParty.target
            val method = template.method.target

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
            if (account != null) {
                chips.add(
                    ChipInfo(
                        resId = R.string.placeholder,
                        value = account.name,
                        icon = Constants.DEFAULT_ACCOUNT_ICON
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
                val currencyFormatter = currencyFormatterMap[account?.currency]
                currencyFormatter?.format(template.amount)?.toString()
                    ?: numberFormatter.format(template.amount).toString()
            } else {
                ""
            }
            TemplateWithChips(
                id = template.id,
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

    fun setSortOptions(field: Property<TransactionTemplate>, flags: Int) {
        _sortOptionsState.update {
            it.copy(
                sortFlags = flags,
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