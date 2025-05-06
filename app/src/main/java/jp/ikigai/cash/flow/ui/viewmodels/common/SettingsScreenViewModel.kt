package jp.ikigai.cash.flow.ui.viewmodels.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.max
import jp.ikigai.cash.flow.data.Constants
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
import kotlinx.coroutines.delay
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

    fun fixBrokenMetadata() = viewModelScope.launch {
        _state.update {
            it.copy(
                showWaitDialog = true
            )
        }
        val startTime = System.currentTimeMillis()
        var result: Event
        try {
            realm.write {
                val categories = this.query<Category>().find()
                val counterParties = this.query<CounterParty>().find()
                val methods = this.query<Method>().find()
                val sources = this.query<Source>().find()
                val transactionTitles = this.query<TransactionTitle>().find()

                transactionTitles.forEach { transactionTitle ->
                    transactionTitle.frequency = this
                        .query<Transaction>("title == $0", transactionTitle.title)
                        .count()
                        .find()
                        .toInt()
                    transactionTitle.lastUsed = this
                        .query<Transaction>("title == $0", transactionTitle.title)
                        .max<Long>("time")
                        .find() ?: 0L
                }

                categories.forEach { category ->
                    category.frequency = this
                        .query<Transaction>("category.uuid==$0", category.uuid)
                        .count()
                        .find()
                        .toInt()
                    category.lastUsed = this
                        .query<Transaction>("category.uuid==$0", category.uuid)
                        .max<Long>("time")
                        .find() ?: 0L
                }
                counterParties.forEach { counterParty ->
                    counterParty.frequency = this
                        .query<Transaction>("counterParty.uuid==$0", counterParty.uuid)
                        .count()
                        .find()
                        .toInt()
                    counterParty.lastUsed = this
                        .query<Transaction>("counterParty.uuid==$0", counterParty.uuid)
                        .max<Long>("time")
                        .find() ?: 0L
                }
                methods.forEach { method ->
                    method.frequency = this
                        .query<Transaction>("method.uuid==$0", method.uuid)
                        .count()
                        .find()
                        .toInt()
                    method.lastUsed = this
                        .query<Transaction>("method.uuid==$0", method.uuid)
                        .max<Long>("time")
                        .find() ?: 0L
                }
                sources.forEach { source ->
                    source.frequency = this
                        .query<Transaction>("source.uuid==$0", source.uuid)
                        .count()
                        .find()
                        .toInt()
                    source.lastUsed = this
                        .query<Transaction>("source.uuid==$0", source.uuid)
                        .max<Long>("time")
                        .find() ?: 0L
                }
            }
            result = Event.MetadataFixSuccess
        } catch (exception: Exception) {
            result = Event.InternalError
        }
        val duration = System.currentTimeMillis() - startTime
        if (duration < Constants.WAIT_DIALOG_MINIMUM_SCREEN_TIME) {
            delay(Constants.WAIT_DIALOG_MINIMUM_SCREEN_TIME - duration)
        }
        _event.send(result)
        _state.update {
            it.copy(
                showWaitDialog = false
            )
        }
    }
}