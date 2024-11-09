package jp.ikigai.cash.flow.ui.viewmodels.listing

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import compose.icons.TablerIcons
import compose.icons.tablericons.ChartLine
import compose.icons.tablericons.History
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.Sort
import io.realm.kotlin.query.TRUE_PREDICATE
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Database
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.dto.ChipInfo
import jp.ikigai.cash.flow.data.dto.ItemListingDTO
import jp.ikigai.cash.flow.data.entity.Item
import jp.ikigai.cash.flow.ui.screenStates.listing.ItemsScreenState
import jp.ikigai.cash.flow.utils.getNumberFormatter
import jp.ikigai.cash.flow.utils.toZonedDateTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

class ItemsScreenViewModel(
    private val realm: Realm = Realm.open(Database.config),
) : ViewModel() {

    private var formatter = getNumberFormatter()

    private val _state = MutableStateFlow(ItemsScreenState())
    val state: StateFlow<ItemsScreenState> = _state.asStateFlow()

    private val _event: Channel<Event> = Channel(Int.MAX_VALUE)
    val event: Flow<Event> = _event.receiveAsFlow()

    init {
        getItems()
        getCount()
    }

    override fun onCleared() {
        super.onCleared()
        realm.close()
    }

    private fun getCount() = viewModelScope.launch {
        realm.query<Item>().count().asFlow().collectLatest { count ->
            _state.update {
                it.copy(
                    count = count,
                    countString = formatter.format(count).toString()
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getItems() = viewModelScope.launch {
        state.flatMapLatest {
            val query = realm.query<Item>(
                if (it.searchText.isBlank()) {
                    TRUE_PREDICATE
                } else {
                    "name CONTAINS[c] '${it.searchText.trim()}'"
                }
            )
            if (it.sortField == "lastKnownPrice") {
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
                    items = mapToDTO(changes.list, screenState.searchText),
                    itemNames = changes.list.map { it.name },
                    loading = false,
                    countString = formatter.format(screenState.count).toString()
                )
            }
        }
    }

    private fun mapToDTO(items: List<Item>, searchText: String): List<ItemListingDTO> {
        return items.map { item ->
            val annotatedName = buildAnnotatedString {
                val startIndex = item.name.indexOf(
                    searchText,
                    startIndex = 0,
                    ignoreCase = true
                )
                val endIndex = startIndex + searchText.length
                append(item.name)
                addStyle(
                    style = SpanStyle(background = Color.Gray.copy(alpha = 0.7f)),
                    start = startIndex,
                    end = endIndex
                )
            }
            val chips: MutableList<ChipInfo> = mutableListOf()
            chips.add(
                ChipInfo(
                    resId = R.string.frequency_of_use_label,
                    value = formatter.format(item.frequency).toString(),
                    icon = TablerIcons.ChartLine
                )
            )
            if (item.lastUsed > 0) {
                chips.add(
                    ChipInfo(
                        resId = R.string.last_used_datetime_label,
                        value = item.lastUsed.toZonedDateTime()
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
            ItemListingDTO(
                uuid = item.uuid,
                annotatedName = annotatedName,
                price = if (item.lastKnownPrice > 0) {
                    "${formatter.format(item.lastKnownPrice)} ${item.lastUsedCurrency}"
                } else "",
                unit = item.lastUsedUnit,
                chips = chips
            )
        }
    }

    fun editItem(item: ItemListingDTO?) {
        _state.update {
            it.copy(
                selectedItem = item
            )
        }
    }

    fun upsertItem(newName: String, itemDTO: ItemListingDTO?) = viewModelScope.launch {
        if (newName.isNotBlank()) {
            _state.update {
                it.copy(
                    loading = true,
                    enabled = false
                )
            }
            val result = if (itemDTO == null) {
                realm.write {
                    copyToRealm(
                        instance = Item().apply {
                            uuid = UUID.randomUUID().toString()
                            name = newName
                        },
                        updatePolicy = UpdatePolicy.ALL
                    )
                }
            } else {
                val item = realm.query<Item>("uuid == $0", itemDTO.uuid).find().first()
                realm.write {
                    findLatest(item)?.also {
                        it.name = newName
                    }
                }
            }
            if (result != null) {
                _event.send(Event.SaveSuccess)
            } else {
                _event.send(Event.InternalError)
            }
            _state.update {
                it.copy(
                    loading = false,
                    enabled = true
                )
            }
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