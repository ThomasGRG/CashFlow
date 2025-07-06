package jp.ikigai.cash.flow.data.dto

import jp.ikigai.cash.flow.AccountWithTransactionMetadata
import jp.ikigai.cash.flow.CategoryWithTransactionMetadata
import jp.ikigai.cash.flow.CounterPartyWithTransactionMetadata
import jp.ikigai.cash.flow.MethodWithTransactionMetadata
import jp.ikigai.cash.flow.TransactionTemplate

data class UpsertTransactionTemplateFlows(
    val accounts: List<AccountWithTransactionMetadata> = emptyList(),
    val categories: List<CategoryWithTransactionMetadata> = emptyList(),
    val counterParties: List<CounterPartyWithTransactionMetadata> = emptyList(),
    val methods: List<MethodWithTransactionMetadata> = emptyList(),
    val transactionTemplate: TransactionTemplate? = null,
)
