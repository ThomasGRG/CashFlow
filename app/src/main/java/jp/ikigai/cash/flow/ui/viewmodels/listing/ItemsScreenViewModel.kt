package jp.ikigai.cash.flow.ui.viewmodels.listing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.Sort
import jp.ikigai.cash.flow.data.Database
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.entity.Item
import jp.ikigai.cash.flow.ui.screenStates.listing.ItemsScreenState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class ItemsScreenViewModel(
    private val realm: Realm = Realm.open(Database.config),
) : ViewModel() {

    private val _state = MutableStateFlow(ItemsScreenState())
    val state: StateFlow<ItemsScreenState> = _state.asStateFlow()

    private val _event: Channel<Event> = Channel(Int.MAX_VALUE)
    val event: Flow<Event> = _event.receiveAsFlow()

    private val itemQuery = realm.query<Item>().sort("frequency", Sort.DESCENDING)

    init {
        getItems()
    }

    override fun onCleared() {
        super.onCleared()
        realm.close()
    }

    private fun getItems() = viewModelScope.launch {
        itemQuery.asFlow().collectLatest { changes ->
            _state.update {
                it.copy(
                    items = changes.list,
                    loading = false
                )
            }
        }
    }

    fun editItem(item: Item) {
        _state.update {
            it.copy(
                item = item
            )
        }
    }

    fun upsertItem(newName: String) = viewModelScope.launch {
        val item = state.value.item
        if (newName.isNotBlank()) {
            _state.update {
                it.copy(
                    loading = true,
                    enabled = false
                )
            }
            val result = realm.write {
                if (item.uuid.isBlank()) {
                    copyToRealm(
                        instance = item.apply {
                            uuid = UUID.randomUUID().toString()
                            name = newName
                        },
                        updatePolicy = UpdatePolicy.ALL
                    )
                } else {
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
}