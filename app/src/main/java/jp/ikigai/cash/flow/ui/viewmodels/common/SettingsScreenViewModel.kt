package jp.ikigai.cash.flow.ui.viewmodels.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.objectbox.Box
import io.objectbox.BoxStore
import io.objectbox.kotlin.boxFor
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.Database
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.entity.Category
import jp.ikigai.cash.flow.data.entity.CounterParty
import jp.ikigai.cash.flow.data.entity.Method
import jp.ikigai.cash.flow.data.entity.Source
import jp.ikigai.cash.flow.data.entity.Transaction
import jp.ikigai.cash.flow.data.entity.TransactionTemplate
import jp.ikigai.cash.flow.data.entity.TransactionTitle
import jp.ikigai.cash.flow.data.store.DataStore
import jp.ikigai.cash.flow.data.store.entity.Account
import jp.ikigai.cash.flow.data.store.entity.Account_
import jp.ikigai.cash.flow.data.store.entity.Category_
import jp.ikigai.cash.flow.data.store.entity.CounterParty_
import jp.ikigai.cash.flow.data.store.entity.Method_
import jp.ikigai.cash.flow.data.store.entity.TransactionTitle_
import jp.ikigai.cash.flow.data.store.entity.Transaction_
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
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class SettingsScreenViewModel(
    private val realm: Realm = Realm.open(Database.config),
    private val store: BoxStore = DataStore.store
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

    fun importFromRealm() = viewModelScope.launch {
        _state.update {
            it.copy(
                showWaitDialog = true
            )
        }

        val accountBox: Box<Account> = store.boxFor()
        val categoryBox: Box<jp.ikigai.cash.flow.data.store.entity.Category> = store.boxFor()
        val counterPartyBox: Box<jp.ikigai.cash.flow.data.store.entity.CounterParty> =
            store.boxFor()
        val methodBox: Box<jp.ikigai.cash.flow.data.store.entity.Method> = store.boxFor()
        val transactionBox: Box<jp.ikigai.cash.flow.data.store.entity.Transaction> = store.boxFor()
        val templateBox: Box<jp.ikigai.cash.flow.data.store.entity.TransactionTemplate> =
            store.boxFor()
        val titleBox: Box<jp.ikigai.cash.flow.data.store.entity.TransactionTitle> = store.boxFor()

        val realmTransactions = realm.query<Transaction>().find()
        val realmTransactionTitles = realm.query<TransactionTitle>().find()
        val realmTemplates = realm.query<TransactionTemplate>().find()
        val realmCategories = realm.query<Category>().find()
        val realmCounterParties = realm.query<CounterParty>().find()
        val realmMethods = realm.query<Method>().find()
        val realmSources = realm.query<Source>().find()

        val realmAccountToBoxMap = mutableMapOf<String, Long>()
        val realmCategoryToBoxMap = mutableMapOf<String, Long>()
        val realmCounterPartyToBoxMap = mutableMapOf<String, Long>()
        val realmMethodToBoxMap = mutableMapOf<String, Long>()

        realmSources.forEach { realmSource ->
            val account = Account(
                name = realmSource.name,
                currency = realmSource.currency,
                balance = realmSource.balance,
                frequency = realmSource.frequency,
                lastUsed = realmSource.lastUsed
            )
            realmAccountToBoxMap[realmSource.uuid] = accountBox.put(account)
        }
        realmCategories.forEach { realmCategory ->
            val category = jp.ikigai.cash.flow.data.store.entity.Category(
                name = realmCategory.name,
                icon = realmCategory.icon,
                frequency = realmCategory.frequency,
                lastUsed = realmCategory.lastUsed
            )
            realmCategoryToBoxMap[realmCategory.uuid] = categoryBox.put(category)
        }
        realmCounterParties.map { realmCounterParty ->
            val counterParty = jp.ikigai.cash.flow.data.store.entity.CounterParty(
                name = realmCounterParty.name,
                frequency = realmCounterParty.frequency,
                lastUsed = realmCounterParty.lastUsed
            )
            realmCounterPartyToBoxMap[realmCounterParty.uuid] = counterPartyBox.put(counterParty)
        }
        realmMethods.map { realmMethod ->
            val method = jp.ikigai.cash.flow.data.store.entity.Method(
                name = realmMethod.name,
                frequency = realmMethod.frequency,
                lastUsed = realmMethod.lastUsed
            )
            realmMethodToBoxMap[realmMethod.uuid] = methodBox.put(method)
        }
        titleBox.put(
            realmTransactionTitles.map { realmTitle ->
                jp.ikigai.cash.flow.data.store.entity.TransactionTitle(
                    title = realmTitle.title,
                    frequency = realmTitle.frequency,
                    lastUsed = realmTitle.lastUsed
                )
            }
        )

        realmTemplates.forEach { realmTemplate ->
            val template = jp.ikigai.cash.flow.data.store.entity.TransactionTemplate(
                name = realmTemplate.name,
                title = realmTemplate.title,
                description = realmTemplate.description,
                amount = realmTemplate.amount,
                type = realmTemplate.type,
                frequency = realmTemplate.frequency,
                lastUsed = realmTemplate.lastUsed
            )
            template.account.targetId = realmAccountToBoxMap[realmTemplate.source?.uuid] ?: 0L
            template.category.targetId = realmCategoryToBoxMap[realmTemplate.category?.uuid] ?: 0L
            template.counterParty.targetId =
                realmCounterPartyToBoxMap[realmTemplate.counterParty?.uuid] ?: 0L
            template.method.targetId = realmMethodToBoxMap[realmTemplate.method?.uuid] ?: 0L
            templateBox.put(template)
        }

        realmTransactions.forEach { realmTransaction ->
            val transaction = jp.ikigai.cash.flow.data.store.entity.Transaction(
                title = realmTransaction.title,
                description = realmTransaction.description,
                amount = realmTransaction.amount,
                type = realmTransaction.type,
                currency = realmTransaction.currency,
                time = realmTransaction.time
            )
            transaction.account.targetId = realmAccountToBoxMap[realmTransaction.source?.uuid] ?: 0L
            transaction.category.targetId =
                realmCategoryToBoxMap[realmTransaction.category?.uuid] ?: 0L
            transaction.counterParty.targetId =
                realmCounterPartyToBoxMap[realmTransaction.counterParty?.uuid] ?: 0L
            transaction.method.targetId = realmMethodToBoxMap[realmTransaction.method?.uuid] ?: 0L
            transactionBox.put(transaction)
        }

        _state.update {
            it.copy(
                showWaitDialog = false
            )
        }
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
            val accountBox: Box<Account> = store.boxFor()
            val categoryBox: Box<jp.ikigai.cash.flow.data.store.entity.Category> = store.boxFor()
            val counterPartyBox: Box<jp.ikigai.cash.flow.data.store.entity.CounterParty> =
                store.boxFor()
            val methodBox: Box<jp.ikigai.cash.flow.data.store.entity.Method> = store.boxFor()
            val transactionBox: Box<jp.ikigai.cash.flow.data.store.entity.Transaction> =
                store.boxFor()
            val titleBox: Box<jp.ikigai.cash.flow.data.store.entity.TransactionTitle> =
                store.boxFor()

            val accountQuery = accountBox.query().build()
            val categoryQuery = categoryBox.query().build()
            val counterPartyQuery = counterPartyBox.query().build()
            val methodQuery = methodBox.query().build()
            val titleQuery = titleBox.query().build()

            val accounts = accountQuery.find()
            val categories = categoryQuery.find()
            val counterParties = counterPartyQuery.find()
            val methods = methodQuery.find()
            val transactionTitles = titleQuery.find()

            accountQuery.close()
            categoryQuery.close()
            counterPartyQuery.close()
            methodQuery.close()
            titleQuery.close()

            val epoch = ZonedDateTime.ofInstant(Instant.EPOCH, ZoneId.systemDefault())

            val titleCountQuery = titleBox.query(TransactionTitle_.id.equal(0L)).build()
            val titleLastUsedQuery = transactionBox
                .query(Transaction_.title.equal(""))
                .orderDesc(Transaction_.time)
                .build()

            transactionTitles.forEach { transactionTitle ->
                titleBox.put(
                    transactionTitle.copy(
                        frequency = titleCountQuery
                            .setParameter(TransactionTitle_.id, transactionTitle.id)
                            .count()
                            .toInt(),
                        lastUsed = titleLastUsedQuery
                            .setParameter(Transaction_.title, transactionTitle.title)
                            .findFirst()
                            ?.time ?: epoch
                    )
                )
            }

            titleCountQuery.close()
            titleLastUsedQuery.close()

            val categoryCountQuery = categoryBox.query(Category_.id.equal(0L)).build()
            val categoryLastUsedQuery = transactionBox
                .query(Transaction_.categoryId.equal(0L))
                .orderDesc(Transaction_.time)
                .build()

            categories.forEach { category ->
                categoryBox.put(
                    category.copy(
                        frequency = categoryCountQuery
                            .setParameter(Category_.id, category.id)
                            .count()
                            .toInt(),
                        lastUsed = categoryLastUsedQuery
                            .setParameter(Transaction_.categoryId, category.id)
                            .findFirst()
                            ?.time ?: epoch
                    )
                )
            }

            categoryCountQuery.close()
            categoryLastUsedQuery.close()

            val counterPartyCountQuery = counterPartyBox.query(CounterParty_.id.equal(0L)).build()
            val counterPartyLastUsedQuery = transactionBox
                .query(Transaction_.counterPartyId.equal(0L))
                .orderDesc(Transaction_.time)
                .build()

            counterParties.forEach { counterParty ->
                counterPartyBox.put(
                    counterParty.copy(
                        frequency = counterPartyCountQuery
                            .setParameter(CounterParty_.id, counterParty.id)
                            .count()
                            .toInt(),
                        lastUsed = counterPartyLastUsedQuery
                            .setParameter(Transaction_.counterPartyId, counterParty.id)
                            .findFirst()
                            ?.time ?: epoch
                    )
                )
            }

            counterPartyCountQuery.close()
            counterPartyLastUsedQuery.close()

            val methodCountQuery = methodBox.query(Method_.id.equal(0L)).build()
            val methodLastUsedQuery = transactionBox
                .query(Transaction_.methodId.equal(0L))
                .orderDesc(Transaction_.time)
                .build()

            methods.forEach { method ->
                methodBox.put(
                    method.copy(
                        frequency = methodCountQuery
                            .setParameter(Method_.id, method.id)
                            .count()
                            .toInt(),
                        lastUsed = methodLastUsedQuery
                            .setParameter(Transaction_.methodId, method.id)
                            .findFirst()
                            ?.time ?: epoch
                    )
                )
            }

            methodCountQuery.close()
            methodLastUsedQuery.close()

            val accountCountQuery = accountBox.query(Account_.id.equal(0L)).build()
            val accountLastUsedQuery = transactionBox
                .query(Transaction_.accountId.equal(0L))
                .orderDesc(Transaction_.time)
                .build()

            accounts.forEach { account ->
                accountBox.put(
                    account.copy(
                        frequency = accountCountQuery
                            .setParameter(Account_.id, account.id)
                            .count()
                            .toInt(),
                        lastUsed = accountLastUsedQuery
                            .setParameter(Transaction_.accountId, account.id)
                            .findFirst()
                            ?.time ?: epoch
                    )
                )
            }

            accountCountQuery.close()
            accountLastUsedQuery.close()

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