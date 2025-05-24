package jp.ikigai.cash.flow.data.dto

import jp.ikigai.cash.flow.data.store.entity.Account
import jp.ikigai.cash.flow.data.store.entity.Category
import jp.ikigai.cash.flow.data.store.entity.CounterParty
import jp.ikigai.cash.flow.data.store.entity.Method
import jp.ikigai.cash.flow.data.store.entity.Transaction
import jp.ikigai.cash.flow.data.store.entity.TransactionTemplate
import jp.ikigai.cash.flow.data.store.entity.TransactionTitle

data class UpsertTransactionFlows(
    val accounts: List<Account> = emptyList(),
    val categories: List<Category> = emptyList(),
    val counterParties: List<CounterParty> = emptyList(),
    val methods: List<Method> = emptyList(),
    val transactionTitles: List<TransactionTitle> = emptyList(),
    val transaction: Transaction? = null,
    val transactionTemplate: TransactionTemplate? = null,
)
