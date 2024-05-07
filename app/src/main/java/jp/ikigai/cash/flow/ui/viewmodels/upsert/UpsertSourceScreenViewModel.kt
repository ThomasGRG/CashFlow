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
import jp.ikigai.cash.flow.data.entity.Source
import jp.ikigai.cash.flow.ui.screenStates.upsert.UpsertSourceScreenState
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

class UpsertSourceScreenViewModel(
    savedStateHandle: SavedStateHandle,
    private val realm: Realm = Realm.open(Database.config),
) : ViewModel() {

    private val sourceUuid: String = checkNotNull(savedStateHandle["id"])

    private var getSourceJob: Job? = null

    private val _event: Channel<Event> = Channel(Int.MAX_VALUE)
    val event: Flow<Event> = _event.receiveAsFlow()

    private val _state = MutableStateFlow(UpsertSourceScreenState())
    val state: StateFlow<UpsertSourceScreenState> = _state.asStateFlow()

    init {
        if (sourceUuid.isNotBlank()) {
            getSourceJob = getSource()
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

    private fun getSource() = viewModelScope.launch {
        realm.query<Source>("uuid == $0", sourceUuid).asFlow().collectLatest { changes ->
            val source = changes.list.first()
            _state.update {
                it.copy(
                    source = source,
                    loading = false,
                    enabled = true
                )
            }
        }
    }

    fun upsertSource(
        newIcon: ImageVector,
        newName: String,
        newCurrency: String,
        newBalance: Double
    ) = viewModelScope.launch {
        val source = state.value.source
        if (newName.isNotBlank()) {
            if (newName != source.name && realm.query<Source>("name == [c]$0", newName).count().find() > 0) {
                _event.send(Event.NameAlreadyTaken)
                return@launch
            }
            getSourceJob?.cancel()
            _state.update {
                it.copy(
                    loading = true,
                    enabled = false
                )
            }
            val result = realm.write {
                if (source.uuid.isBlank()) {
                    copyToRealm(
                        instance = source.apply {
                            uuid = UUID.randomUUID().toString()
                            icon = newIcon
                            name = newName
                            currency = newCurrency
                            balance = newBalance
                        },
                        updatePolicy = UpdatePolicy.ALL
                    )
                } else {
                    findLatest(source)?.also {
                        it.icon = newIcon
                        it.name = newName
                        it.currency = newCurrency
                        it.balance = newBalance
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