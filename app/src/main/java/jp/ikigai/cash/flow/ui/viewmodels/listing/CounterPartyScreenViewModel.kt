package jp.ikigai.cash.flow.ui.viewmodels.listing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.Sort
import jp.ikigai.cash.flow.data.Database
import jp.ikigai.cash.flow.data.entity.CounterParty
import jp.ikigai.cash.flow.data.screenStates.listing.CounterPartyScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CounterPartyScreenViewModel(
    private val realm: Realm = Realm.open(Database.config),
) : ViewModel() {

    private val _state = MutableStateFlow(CounterPartyScreenState())
    val state: StateFlow<CounterPartyScreenState> = _state.asStateFlow()

    private val counterPartyQuery = realm.query<CounterParty>().sort("frequency", Sort.DESCENDING)

    init {
        getCounterParties()
    }

    override fun onCleared() {
        super.onCleared()
        realm.close()
    }

    private fun getCounterParties() = viewModelScope.launch {
        counterPartyQuery.asFlow().collectLatest { changes ->
            _state.update {
                it.copy(
                    counterParties = changes.list,
                    loading = false
                )
            }
        }
    }
}