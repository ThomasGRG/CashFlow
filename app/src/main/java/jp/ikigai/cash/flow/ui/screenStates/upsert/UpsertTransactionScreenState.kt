package jp.ikigai.cash.flow.ui.screenStates.upsert

import jp.ikigai.cash.flow.data.entity.Category
import jp.ikigai.cash.flow.data.entity.CounterParty
import jp.ikigai.cash.flow.data.entity.Item
import jp.ikigai.cash.flow.data.entity.Method
import jp.ikigai.cash.flow.data.entity.Source
import jp.ikigai.cash.flow.data.entity.Transaction
import jp.ikigai.cash.flow.data.entity.TransactionItem
import jp.ikigai.cash.flow.data.entity.TransactionTitle
import jp.ikigai.cash.flow.data.enums.TransactionType
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
    val type: TransactionType = TransactionType.DEBIT,
    val categories: List<Category> = emptyList(),
    val selectedCategory: Category = Category(),
    val categoryValid: Boolean = true,
    val counterParties: List<CounterParty> = emptyList(),
    val selectedCounterParty: CounterParty = CounterParty(),
    val methods: List<Method> = emptyList(),
    val selectedMethod: Method = Method(),
    val methodValid: Boolean = true,
    val sources: List<Source> = emptyList(),
    val selectedSource: Source = Source(),
    val sourceValid: Boolean = true,
    val items: List<Item> = emptyList(),
    val transactionTitles: List<TransactionTitle> = emptyList(),
    val transactionItems: Map<Item, TransactionItem> = emptyMap(),
    val loading: Boolean = true,
    val enabled: Boolean = false,
)
