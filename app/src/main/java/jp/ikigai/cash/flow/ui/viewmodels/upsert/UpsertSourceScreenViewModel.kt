package jp.ikigai.cash.flow.ui.viewmodels.upsert

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Database
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.entity.Source
import jp.ikigai.cash.flow.data.entity.Transaction
import jp.ikigai.cash.flow.ui.screenStates.upsert.UpsertSourceScreenState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
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
            checkTransactionCount()
        } else {
            _state.update {
                it.copy(
                    loading = false,
                    enabled = true
                )
            }
        }
        checkNameAlreadyInUse()
    }

    override fun onCleared() {
        super.onCleared()
        realm.close()
        _event.close()
    }

    private fun checkTransactionCount() = viewModelScope.launch {
        realm.query<Transaction>("source.uuid == $0", sourceUuid).count().asFlow()
            .collectLatest { count ->
                _state.update {
                    it.copy(
                        hasTransactions = count > 0
                    )
                }
            }
    }

    private fun getSource() = viewModelScope.launch {
        realm.query<Source>("uuid == $0", sourceUuid).asFlow().collectLatest { changes ->
            val source = changes.list.first()
            _state.update {
                it.copy(
                    source = source,
                    name = source.name,
                    loading = false,
                    enabled = true
                )
            }
        }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun checkNameAlreadyInUse() = viewModelScope.launch {
        state
            .debounce(250L)
            .flatMapLatest { screenState ->
                val searchName =
                    if (screenState.name.isNotBlank() && screenState.name.trim() != screenState.source.name) {
                        screenState.name.trim()
                    } else {
                        ""
                    }
                realm.query<Source>("name == [c]$0", searchName)
                    .count()
                    .asFlow()
            }.collectLatest { count ->
                _state.update {
                    it.copy(
                        nameValid = if (count > 0) false else it.nameValid,
                        nameErrorStringRes = if (count > 0) R.string.name_in_use_label else R.string.name_empty_error_label,
                        loading = false
                    )
                }
            }
    }

    fun setName(name: String) {
        _state.update {
            it.copy(
                name = name,
                nameValid = name.isNotBlank(),
                nameErrorStringRes = R.string.name_empty_error_label,
                loading = name.isNotBlank()
            )
        }
    }

    fun upsertSource(
        newIcon: ImageVector,
        newName: String,
        newCurrency: String,
        newBalance: Double
    ) = viewModelScope.launch {
        val source = state.value.source
        if (newName.isBlank()) {
            _state.update {
                it.copy(
                    nameValid = false,
                    nameErrorStringRes = R.string.name_empty_error_label,
                )
            }
            return@launch
        }
        if (state.value.nameValid && !state.value.loading) {
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

    fun deleteSource() = viewModelScope.launch {
        if (sourceUuid.isNotBlank()) {
            getSourceJob?.cancelAndJoin()
            _state.update {
                it.copy(
                    loading = true,
                    enabled = false
                )
            }
            val source = state.value.source
            realm.write {
                this.query<Transaction>("source.uuid == $0", sourceUuid).find()
                    .forEach { transaction ->
                        delete(transaction)
                    }
                findLatest(source)?.also {
                    delete(it)
                }
            }
            _state.update {
                it.copy(
                    loading = false
                )
            }
            _event.send(Event.DeleteSuccess)
        }
    }
}