package jp.ikigai.cash.flow.data.dto

import jp.ikigai.cash.flow.data.entity.Category
import jp.ikigai.cash.flow.data.entity.CounterParty
import jp.ikigai.cash.flow.data.entity.Item
import jp.ikigai.cash.flow.data.entity.Method
import jp.ikigai.cash.flow.data.entity.Source
import jp.ikigai.cash.flow.data.enums.TransactionType

data class Filters(
    val categories: List<Category> = emptyList(),
    val selectedCategories: Map<String, Boolean> = emptyMap(),
    val counterParties: List<CounterParty> = emptyList(),
    val selectedCounterParties: Map<String, Boolean> = emptyMap(),
    val includeNoCounterPartyTransactions: Boolean = true,
    val methods: List<Method> = emptyList(),
    val selectedMethods: Map<String, Boolean> = emptyMap(),
    val sources: List<Source> = emptyList(),
    val selectedSources: Map<String, Boolean> = emptyMap(),
    val items: List<Item> = emptyList(),
    val selectedItems: Map<String, Boolean> = emptyMap(),
    val includeNoItemTransactions: Boolean = true,
    val transactionTypes: List<TransactionType> = TransactionType.values().toList(),
    val selectedTransactionTypes: List<Int> = listOf(1, 2),
    val filterAmountMin: Double = 0.0,
    val filterAmountMax: Double = 0.0,
)
