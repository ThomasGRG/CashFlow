package jp.ikigai.cash.flow.data.dto

import jp.ikigai.cash.flow.data.entity.Category
import jp.ikigai.cash.flow.data.entity.CounterParty
import jp.ikigai.cash.flow.data.entity.Item
import jp.ikigai.cash.flow.data.entity.Method
import jp.ikigai.cash.flow.data.entity.Source
import jp.ikigai.cash.flow.data.entity.TransactionTemplate

data class UpsertTransactionTemplateFlows(
    val categories: List<Category> = emptyList(),
    val counterParties: List<CounterParty> = emptyList(),
    val methods: List<Method> = emptyList(),
    val sources: List<Source> = emptyList(),
    val items: List<Item> = emptyList(),
    val transactionTemplate: TransactionTemplate? = null,
)
