package jp.ikigai.cash.flow.ui.screenStates.upsert

import androidx.annotation.StringRes
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.entity.Category
import jp.ikigai.cash.flow.data.entity.CounterParty
import jp.ikigai.cash.flow.data.entity.Method
import jp.ikigai.cash.flow.data.entity.Source
import jp.ikigai.cash.flow.data.entity.TransactionTemplate
import jp.ikigai.cash.flow.data.enums.TransactionType
import java.util.Locale

data class UpsertTransactionTemplateScreenState(
    val transactionTemplate: TransactionTemplate = TransactionTemplate(),
    val name: String = "",
    val nameValid: Boolean = true,
    @StringRes val nameErrorStringRes: Int = R.string.name_empty_error_label,
    val amount: Double = 0.0,
    val displayAmount: String = "",
    val type: TransactionType = TransactionType.DEBIT,
    val categories: List<Category> = emptyList(),
    val selectedCategory: Category = Category(),
    val counterParties: List<CounterParty> = emptyList(),
    val selectedCounterParty: CounterParty = CounterParty(),
    val methods: List<Method> = emptyList(),
    val selectedMethod: Method = Method(),
    val sources: List<Source> = emptyList(),
    val selectedSource: Source = Source(),
    val loading: Boolean = true,
    val enabled: Boolean = false,
    val locale: Locale? = null
)
