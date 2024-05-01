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
import jp.ikigai.cash.flow.data.entity.Method
import jp.ikigai.cash.flow.data.screenStates.upsert.UpsertMethodScreenState
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

class UpsertMethodScreenViewModel(
    savedStateHandle: SavedStateHandle,
    private val realm: Realm = Realm.open(Database.config),
) : ViewModel() {

    private val methodUuid: String = checkNotNull(savedStateHandle["id"])

    private var getMethodJob: Job? = null

    private val _event: Channel<Event> = Channel(Int.MAX_VALUE)
    val event: Flow<Event> = _event.receiveAsFlow()

    private val _state = MutableStateFlow(UpsertMethodScreenState())
    val state: StateFlow<UpsertMethodScreenState> = _state.asStateFlow()

    init {
        if (methodUuid.isNotBlank()) {
            getMethodJob = getMethod()
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

    private fun getMethod() = viewModelScope.launch {
        realm.query<Method>("uuid == $0", methodUuid).asFlow().collectLatest { changes ->
            _state.update {
                it.copy(
                    method = changes.list.first(),
                    loading = false,
                    enabled = true
                )
            }
        }
    }

    fun upsertCategory(newIcon: ImageVector, newName: String) = viewModelScope.launch {
        val method = state.value.method
        if (newName.isNotBlank()) {
            getMethodJob?.cancel()
            _state.update {
                it.copy(
                    loading = true,
                    enabled = false
                )
            }
            val result = realm.write {
                if (method.uuid.isBlank()) {
                    copyToRealm(
                        instance = method.apply {
                            uuid = UUID.randomUUID().toString()
                            icon = newIcon
                            name = newName
                        },
                        updatePolicy = UpdatePolicy.ALL
                    )
                } else {
                    findLatest(method)?.also {
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