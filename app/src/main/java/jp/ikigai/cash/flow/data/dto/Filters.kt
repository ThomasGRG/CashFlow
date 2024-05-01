package jp.ikigai.cash.flow.data.dto

import jp.ikigai.cash.flow.data.entity.Category
import jp.ikigai.cash.flow.data.entity.CounterParty
import jp.ikigai.cash.flow.data.entity.Method
import jp.ikigai.cash.flow.data.entity.Source
import jp.ikigai.cash.flow.data.enums.TransactionType

data class Filters(
    val categories: List<Category> = emptyList(),
    val selectedCategories: List<String> = emptyList(),
    val counterParties: List<CounterParty> = emptyList(),
    val selectedCounterParties: List<String> = emptyList(),
    val methods: List<Method> = emptyList(),
    val selectedMethods: List<String> = emptyList(),
    val sources: List<Source> = emptyList(),
    val selectedSources: List<String> = emptyList(),
    val transactionTypes: List<TransactionType> = TransactionType.values().toList(),
    val selectedTransactionTypes: List<Int> = listOf(1, 2),
    val filterAmountMin: Double = 0.0,
    val filterAmountMax: Double = 0.0,
)
