package jp.ikigai.cash.flow.ui.screenStates.upsert

import androidx.annotation.StringRes
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.enums.TransactionType
import jp.ikigai.cash.flow.data.store.entity.Account
import jp.ikigai.cash.flow.data.store.entity.Category
import jp.ikigai.cash.flow.data.store.entity.CounterParty
import jp.ikigai.cash.flow.data.store.entity.Method
import jp.ikigai.cash.flow.data.store.entity.TransactionTemplate
import java.util.Locale

data class UpsertTransactionTemplateScreenState(
    val transactionTemplate: TransactionTemplate = TransactionTemplate(),
    val name: String = "",
    val nameValid: Boolean = true,
    @StringRes val nameErrorStringRes: Int = R.string.name_empty_error_label,
    val amount: Double = 0.0,
    val displayAmount: String = "",
    val type: TransactionType = TransactionType.DEBIT,
    val accounts: List<Account> = emptyList(),
    val selectedAccount: Account = Account(currency = "INR"),
    val categories: List<Category> = emptyList(),
    val selectedCategory: Category = Category(),
    val counterParties: List<CounterParty> = emptyList(),
    val selectedCounterParty: CounterParty = CounterParty(),
    val methods: List<Method> = emptyList(),
    val selectedMethod: Method = Method(),
    val loading: Boolean = true,
    val enabled: Boolean = false,
    val locale: Locale? = null
)
