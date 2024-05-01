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
import jp.ikigai.cash.flow.data.entity.Category
import jp.ikigai.cash.flow.data.screenStates.upsert.UpsertCategoryScreenState
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
    }

    override fun onCleared() {
        super.onCleared()
        realm.close()
    }

    private fun getCategory() = viewModelScope.launch {
        realm.query<Category>("uuid == $0", categoryUuid).asFlow().collectLatest { changes ->
            _state.update {
                it.copy(
                    category = changes.list.first(),
                    loading = false,
                    enabled = true
                )
            }
        }
    }

    fun upsertCategory(newIcon: ImageVector, newName: String) = viewModelScope.launch {
        val category = state.value.category
        if (newName.isNotBlank()) {
            // otherwise enabled will be set to true after saving and the flow updates
            getCategoryJob?.cancel()
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