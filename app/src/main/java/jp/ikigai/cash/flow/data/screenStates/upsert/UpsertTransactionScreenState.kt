package jp.ikigai.cash.flow.data.screenStates.upsert

import jp.ikigai.cash.flow.data.entity.Category
import jp.ikigai.cash.flow.data.entity.CounterParty
import jp.ikigai.cash.flow.data.entity.Item
import jp.ikigai.cash.flow.data.entity.Method
import jp.ikigai.cash.flow.data.entity.Source
import jp.ikigai.cash.flow.data.entity.Transaction
import jp.ikigai.cash.flow.data.entity.TransactionItem
import java.time.ZoneId
import java.time.ZonedDateTime

data class UpsertTransactionScreenState(
    val transaction: Transaction = Transaction(),
    val amount: Double = 0.0,
    val displayAmount: String = "",
    val amountValid: Boolean = true,
    val taxAmount: Double = 0.0,
    val displayTaxAmount: String = "",
    val dateTime: ZonedDateTime = ZonedDateTime.now(ZoneId.systemDefault()),
    val dateString: String = "",
    val timeString: String = "",
    val selectedCategory: Category = Category(),
    val selectedCounterParty: CounterParty = CounterParty(),
    val selectedMethod: Method = Method(),
    val selectedSource: Source = Source(),
    val categories: List<Category> = emptyList(),
    val counterParties: List<CounterParty> = emptyList(),
    val methods: List<Method> = emptyList(),
    val sources: List<Source> = emptyList(),
    val items: List<Item> = emptyList(),
    val transactionItems: Map<Item, TransactionItem> = emptyMap(),
    val loading: Boolean = true,
    val enabled: Boolean = false,
)
