package jp.ikigai.cash.flow.ui.viewmodels.listing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.Sort
import jp.ikigai.cash.flow.data.Database
import jp.ikigai.cash.flow.data.entity.Method
import jp.ikigai.cash.flow.ui.screenStates.listing.MethodScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MethodScreenViewModel(
    private val realm: Realm = Realm.open(Database.config),
) : ViewModel() {

    private val _state = MutableStateFlow(MethodScreenState())
    val state: StateFlow<MethodScreenState> = _state.asStateFlow()

    private val methodQuery = realm.query<Method>().sort("frequency", Sort.DESCENDING)

    init {
        getMethods()
    }

    override fun onCleared() {
        super.onCleared()
        realm.close()
    }

    private fun getMethods() = viewModelScope.launch {
        methodQuery.asFlow().collectLatest { changes ->
            _state.update {
                it.copy(
                    methods = changes.list,
                    loading = false
                )
            }
        }
    }
}