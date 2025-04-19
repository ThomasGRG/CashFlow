package jp.ikigai.cash.flow.ui.viewmodels.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import jp.ikigai.cash.flow.data.Database
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.entity.Category
import jp.ikigai.cash.flow.data.entity.CounterParty
import jp.ikigai.cash.flow.data.entity.Method
import jp.ikigai.cash.flow.data.entity.Source
import jp.ikigai.cash.flow.data.entity.Transaction
import jp.ikigai.cash.flow.data.entity.TransactionTitle
import jp.ikigai.cash.flow.ui.screenStates.common.SettingsScreenState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsScreenViewModel(
    private val realm: Realm = Realm.open(Database.config),
) : ViewModel() {

    private val _event: Channel<Event> = Channel(Int.MAX_VALUE)
    val event: Flow<Event> = _event.receiveAsFlow()

    private val _state = MutableStateFlow(SettingsScreenState())
    val state: StateFlow<SettingsScreenState> = _state.asStateFlow()

    override fun onCleared() {
        super.onCleared()
        realm.close()
        _event.close()
    }

    fun reCount() = viewModelScope.launch {
        _state.update {
            it.copy(
                loading = true
            )
        }
        try {
            realm.write {
                val categories = this.query<Category>().find()
                val counterParties = this.query<CounterParty>().find()
                val methods = this.query<Method>().find()
                val sources = this.query<Source>().find()
                val transactionTitles = this.query<TransactionTitle>().find()

                transactionTitles.forEach { transactionTitle ->
                    transactionTitle.frequency =
                        this.query<Transaction>("title == $0", transactionTitle.title).count()
                            .find().toInt()
                }

                categories.forEach { category ->
                    category.frequency =
                        this.query<Transaction>("category.uuid==$0", category.uuid).count().find()
                            .toInt()
                }
                counterParties.forEach { counterParty ->
                    counterParty.frequency =
                        this.query<Transaction>("counterParty.uuid==$0", counterParty.uuid).count()
                            .find().toInt()
                }
                methods.forEach { method ->
                    method.frequency =
                        this.query<Transaction>("method.uuid==$0", method.uuid).count().find()
                            .toInt()
                }
                sources.forEach { source ->
                    source.frequency =
                        this.query<Transaction>("source.uuid==$0", source.uuid).count().find()
                            .toInt()
                }
            }
            _event.send(Event.ReCountSuccess)
        } catch (exception: Exception) {
            _event.send(Event.InternalError)
        }
        _state.update {
            it.copy(
                loading = false
            )
        }
    }
}