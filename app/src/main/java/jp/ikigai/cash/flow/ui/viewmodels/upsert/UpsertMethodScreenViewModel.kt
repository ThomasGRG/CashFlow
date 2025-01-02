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
import jp.ikigai.cash.flow.data.entity.Method
import jp.ikigai.cash.flow.data.entity.Transaction
import jp.ikigai.cash.flow.data.enums.TransactionType
import jp.ikigai.cash.flow.ui.screenStates.upsert.UpsertMethodScreenState
import jp.ikigai.cash.flow.utils.getNumberFormatter
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
import java.util.Locale
import java.util.UUID

class UpsertMethodScreenViewModel(
    savedStateHandle: SavedStateHandle,
    private val realm: Realm = Realm.open(Database.config),
) : ViewModel() {

    private var numberFormatter = getNumberFormatter()

    private val methodUuid: String = checkNotNull(savedStateHandle["id"])

    private var getMethodJob: Job? = null

    private val _event: Channel<Event> = Channel(Int.MAX_VALUE)
    val event: Flow<Event> = _event.receiveAsFlow()

    private val _state = MutableStateFlow(UpsertMethodScreenState())
    val state: StateFlow<UpsertMethodScreenState> = _state.asStateFlow()

    init {
        if (methodUuid.isNotBlank()) {
            getMethodJob = getMethod()
            getTransactionCount()
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
    }

    private fun getTransactionCount() = viewModelScope.launch {
        realm.query<Transaction>("method.uuid == $0", methodUuid).count().asFlow()
            .collectLatest { count ->
                _state.update {
                    it.copy(
                        transactionCount = if (count > 0) numberFormatter.format(count)
                            .toString() else ""
                    )
                }
            }
    }

    private fun getMethod() = viewModelScope.launch {
        realm.query<Method>("uuid == $0", methodUuid).asFlow().collectLatest { changes ->
            val method = changes.list.first()
            _state.update {
                it.copy(
                    method = method,
                    name = method.name,
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
                    if (screenState.name.isNotBlank() && screenState.name.trim() != screenState.method.name) {
                        screenState.name.trim()
                    } else {
                        ""
                    }
                realm.query<Method>("name == [c]$0", searchName)
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

    fun setLocale(locale: Locale?) {
        numberFormatter = getNumberFormatter(locale)
        _state.update {
            it.copy(
                locale = locale
            )
        }
    }

    fun upsertMethod(newIcon: ImageVector, newName: String) = viewModelScope.launch {
        val method = state.value.method
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

    fun deleteMethod() = viewModelScope.launch {
        if (methodUuid.isNotBlank()) {
            getMethodJob?.cancelAndJoin()
            _state.update {
                it.copy(
                    loading = true,
                    enabled = false
                )
            }
            val method = state.value.method
            realm.write {
                this.query<Transaction>("method.uuid == $0", methodUuid).find()
                    .groupBy { transaction -> transaction.source!! }
                    .forEach { (source, transactions) ->
                        var newBalance = source.balance
                        transactions.forEach { transaction ->
                            if (transaction.type == TransactionType.CREDIT) {
                                newBalance -= transaction.amount
                            } else {
                                newBalance += transaction.amount
                            }
                            delete(transaction)
                        }
                        source.balance = newBalance
                    }
                findLatest(method)?.also {
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