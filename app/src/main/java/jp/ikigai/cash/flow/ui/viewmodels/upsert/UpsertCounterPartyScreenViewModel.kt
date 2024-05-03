package jp.ikigai.cash.flow.ui.viewmodels.upsert

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import jp.ikigai.cash.flow.data.Database
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.entity.CounterParty
import jp.ikigai.cash.flow.ui.screenStates.upsert.UpsertCounterPartyScreenState
import kotlinx.coroutines.Job
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

class UpsertCounterPartyScreenViewModel(
    savedStateHandle: SavedStateHandle,
    private val realm: Realm = Realm.open(Database.config),
) : ViewModel() {

    private val counterPartyUuid: String = checkNotNull(savedStateHandle["id"])

    private var getCounterPartyJob: Job? = null

    private val _event: Channel<Event> = Channel(Int.MAX_VALUE)
    val event: Flow<Event> = _event.receiveAsFlow()

    private val _state = MutableStateFlow(UpsertCounterPartyScreenState())
    val state: StateFlow<UpsertCounterPartyScreenState> = _state.asStateFlow()

    init {
        if (counterPartyUuid.isNotBlank()) {
            getCounterPartyJob = getCounterParty()
        } else {
            _state.update {
                it.copy(
                    loading = false,
                    enabled = true
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        realm.close()
    }

    private fun getCounterParty() = viewModelScope.launch {
        realm.query<CounterParty>("uuid == $0", counterPartyUuid).asFlow()
            .collectLatest { changes ->
                _state.update {
                    it.copy(
                        counterParty = changes.list.first(),
                        loading = false,
                        enabled = true
                    )
                }
            }
    }

    fun upsertCounterParty(newIcon: ImageVector, newName: String) = viewModelScope.launch {
        val counterParty = state.value.counterParty
        if (newName.isNotBlank()) {
            getCounterPartyJob?.cancel()
            _state.update {
                it.copy(
                    loading = true,
                    enabled = false
                )
            }
            val result = realm.write {
                if (counterParty.uuid.isBlank()) {
                    copyToRealm(
                        instance = counterParty.apply {
                            uuid = UUID.randomUUID().toString()
                            icon = newIcon
                            name = newName
                        },
                        updatePolicy = UpdatePolicy.ALL
                    )
                } else {
                    findLatest(counterParty)?.also {
                        it.icon = newIcon
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
                    loading = false
                )
            }
        }
    }
}