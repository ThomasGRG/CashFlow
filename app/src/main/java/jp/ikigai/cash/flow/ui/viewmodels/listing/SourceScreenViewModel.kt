package jp.ikigai.cash.flow.ui.viewmodels.listing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.Sort
import jp.ikigai.cash.flow.data.Database
import jp.ikigai.cash.flow.data.entity.Source
import jp.ikigai.cash.flow.ui.screenStates.listing.SourceScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SourceScreenViewModel(
    private val realm: Realm = Realm.open(Database.config),
) : ViewModel() {

    private val _state = MutableStateFlow(SourceScreenState())
    val state: StateFlow<SourceScreenState> = _state.asStateFlow()

    private val sourceQuery = realm.query<Source>().sort("frequency", Sort.DESCENDING)

    init {
        getSources()
    }

    override fun onCleared() {
        super.onCleared()
        realm.close()
    }

    private fun getSources() = viewModelScope.launch {
        sourceQuery.asFlow().collectLatest { changes ->
            _state.update {
                it.copy(
                    sources = changes.list,
                    loading = false
                )
            }
        }
    }
}