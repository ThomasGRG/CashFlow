package jp.ikigai.cash.flow.ui.viewmodels.listing

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.Sort
import jp.ikigai.cash.flow.data.Database
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.dto.TransactionTemplateWithIcons
import jp.ikigai.cash.flow.data.entity.Category
import jp.ikigai.cash.flow.data.entity.Method
import jp.ikigai.cash.flow.data.entity.Source
import jp.ikigai.cash.flow.data.entity.TransactionTemplate
import jp.ikigai.cash.flow.ui.screenStates.listing.TransactionTemplateScreenState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TransactionTemplateScreenViewModel(
    private val realm: Realm = Realm.open(Database.config),
) : ViewModel() {

    private val _state = MutableStateFlow(TransactionTemplateScreenState())
    val state: StateFlow<TransactionTemplateScreenState> = _state.asStateFlow()

    private val _event: Channel<Event> = Channel(Int.MAX_VALUE)
    val event: Flow<Event> = _event.receiveAsFlow()

    private val templateQuery = realm.query<TransactionTemplate>().sort("frequency", Sort.DESCENDING)

    init {
        getTemplates()
    }

    override fun onCleared() {
        super.onCleared()
        realm.close()
    }

    fun canAddTransaction(): Boolean {
        val sourceEmpty = realm.query<Source>().count().find() == 0L
        val categoryEmpty = realm.query<Category>().count().find() == 0L
        val methodEmpty = realm.query<Method>().count().find() == 0L
        val canAddTransaction = !sourceEmpty && !categoryEmpty && !methodEmpty
        if (!canAddTransaction) {
            showToastBarForRequiredFields(sourceEmpty, methodEmpty, categoryEmpty)
        }
        return canAddTransaction
    }

    private fun showToastBarForRequiredFields(
        sourceEmpty: Boolean,
        methodEmpty: Boolean,
        categoryEmpty: Boolean
    ) = viewModelScope.launch {
        if (sourceEmpty && methodEmpty && categoryEmpty) {
            _event.send(Event.CategoryMethodSourceRequired)
        } else if (sourceEmpty && methodEmpty) {
            _event.send(Event.MethodSourceRequired)
        } else if (categoryEmpty && methodEmpty) {
            _event.send(Event.CategoryMethodRequired)
        } else if (categoryEmpty && sourceEmpty) {
            _event.send(Event.CategorySourceRequired)
        } else if (categoryEmpty) {
            _event.send(Event.CategoryRequired)
        } else if (sourceEmpty) {
            _event.send(Event.SourceRequired)
        } else if (methodEmpty) {
            _event.send(Event.MethodRequired)
        }
    }

    private fun getTemplates() = viewModelScope.launch {
        templateQuery.asFlow().collectLatest { changes ->
            _state.update {
                it.copy(
                    templates = getTemplateWithIcons(changes.list),
                    loading = false
                )
            }
        }
    }

    private fun getTemplateWithIcons(templates: List<TransactionTemplate>): List<TransactionTemplateWithIcons> {
        return templates.map { template ->
            val category = template.category
            val counterParty = template.counterParty
            val method = template.method
            val source = template.source
            val chips: MutableList<Pair<String, ImageVector>> = mutableListOf()
            if (category != null) chips.add(Pair(category.name, category.icon))
            if (counterParty != null) chips.add(Pair(counterParty.name, counterParty.icon))
            if (method != null) chips.add(Pair(method.name, method.icon))
            if (source != null) chips.add(Pair(source.name, source.icon))
            TransactionTemplateWithIcons(
                uuid = template.uuid,
                name = template.name,
                title = template.title,
                description = template.description,
                amount = template.amount,
                typeIcon = template.type.icon,
                typeIconColor = template.type.color,
                frequency = template.frequency,
                currency = source?.currency ?: "",
                itemCount = template.items.size,
                chips = chips
            )
        }
    }
}