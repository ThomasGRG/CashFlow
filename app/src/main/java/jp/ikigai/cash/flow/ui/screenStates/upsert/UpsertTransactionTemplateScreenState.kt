package jp.ikigai.cash.flow.ui.screenStates.upsert

import jp.ikigai.cash.flow.data.entity.Category
import jp.ikigai.cash.flow.data.entity.CounterParty
import jp.ikigai.cash.flow.data.entity.Item
import jp.ikigai.cash.flow.data.entity.Method
import jp.ikigai.cash.flow.data.entity.Source
import jp.ikigai.cash.flow.data.entity.TransactionItem
import jp.ikigai.cash.flow.data.entity.TransactionTemplate
import jp.ikigai.cash.flow.data.enums.TransactionType

data class UpsertTransactionTemplateScreenState(
    val transactionTemplate: TransactionTemplate = TransactionTemplate(),
    val amount: Double = 0.0,
    val displayAmount: String = "",
    val taxAmount: Double = 0.0,
    val displayTaxAmount: String = "",
    val type: TransactionType = TransactionType.DEBIT,
    val categories: List<Category> = emptyList(),
    val selectedCategory: Category = Category(),
    val counterParties: List<CounterParty> = emptyList(),
    val selectedCounterParty: CounterParty = CounterParty(),
    val methods: List<Method> = emptyList(),
    val selectedMethod: Method = Method(),
    val sources: List<Source> = emptyList(),
    val selectedSource: Source = Source(),
    val items: List<Item> = emptyList(),
    val transactionItems: Map<Item, TransactionItem> = emptyMap(),
    val loading: Boolean = true,
    val enabled: Boolean = false,
)
