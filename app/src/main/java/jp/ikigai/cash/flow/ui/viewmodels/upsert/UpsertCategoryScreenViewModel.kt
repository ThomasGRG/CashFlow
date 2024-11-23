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
import jp.ikigai.cash.flow.data.entity.Category
import jp.ikigai.cash.flow.ui.screenStates.upsert.UpsertCategoryScreenState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
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

class UpsertCategoryScreenViewModel(
    savedStateHandle: SavedStateHandle,
    private val realm: Realm = Realm.open(Database.config),
) : ViewModel() {

    private val categoryUuid: String = checkNotNull(savedStateHandle["id"])

    private var getCategoryJob: Job? = null

    private val _event: Channel<Event> = Channel(Int.MAX_VALUE)
    val event: Flow<Event> = _event.receiveAsFlow()

    private val _state = MutableStateFlow(UpsertCategoryScreenState())
    val state: StateFlow<UpsertCategoryScreenState> = _state.asStateFlow()

    init {
        if (categoryUuid.isNotBlank()) {
            getCategoryJob = getCategory()
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

    private fun getCategory() = viewModelScope.launch {
        realm.query<Category>("uuid == $0", categoryUuid).asFlow().collectLatest { changes ->
            _state.update {
                val category = changes.list.first()
                it.copy(
                    category = category,
                    name = category.name,
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
                    if (screenState.name.isNotBlank() && screenState.name.trim() != screenState.category.name) {
                        screenState.name.trim()
                    } else {
                        ""
                    }
                realm.query<Category>("name == [c]$0", searchName)
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

    fun upsertCategory(newIcon: ImageVector, newName: String) = viewModelScope.launch {
        val category = state.value.category
        if (state.value.nameValid && !state.value.loading) {
            getCategoryJob?.cancel() // otherwise enabled will be set to true after saving and the flow updates
            _state.update {
                it.copy(
                    loading = true,
                    enabled = false
                )
            }
            val result = realm.write {
                if (category.uuid.isBlank()) {
                    copyToRealm(
                        instance = category.apply {
                            uuid = UUID.randomUUID().toString()
                            icon = newIcon
                            name = newName
                        },
                        updatePolicy = UpdatePolicy.ALL
                    )
                } else {
                    findLatest(category)?.also {
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