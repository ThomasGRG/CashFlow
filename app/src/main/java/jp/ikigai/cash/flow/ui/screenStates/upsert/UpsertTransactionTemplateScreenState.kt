package jp.ikigai.cash.flow.ui.screenStates.upsert

import androidx.annotation.StringRes
import compose.icons.TablerIcons
import compose.icons.tablericons.Archive
import jp.ikigai.cash.flow.AccountWithTransactionMetadata
import jp.ikigai.cash.flow.CategoryWithTransactionMetadata
import jp.ikigai.cash.flow.CounterPartyWithTransactionMetadata
import jp.ikigai.cash.flow.MethodWithTransactionMetadata
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.TransactionTemplate
import jp.ikigai.cash.flow.data.enums.TransactionType
import java.util.Locale

data class UpsertTransactionTemplateScreenState(
    val transactionTemplate: TransactionTemplate = TransactionTemplate(
        templateId = 0L,
        templateName = "",
        templateTitle = "",
        templateDescription = "",
        templateAmount = 0.0,
        templateType = TransactionType.DEBIT,
        templateAccountId = 0L,
        templateCategoryId = 0L,
        templateCounterPartyId = 0L,
        templateMethodId = 0L
    ),
    val name: String = "",
    val nameValid: Boolean = true,
    @StringRes val nameErrorStringRes: Int = R.string.name_empty_error_label,
    val amount: Double = 0.0,
    val displayAmount: String = "",
    val type: TransactionType = TransactionType.DEBIT,
    val accounts: List<AccountWithTransactionMetadata> = emptyList(),
    val selectedAccount: AccountWithTransactionMetadata = AccountWithTransactionMetadata(
        accountId = 0L,
        accountName = "",
        currency = "INR",
        balance = 0.0,
        formattedBalance = "",
        transactionCount = 0L,
        lastUsed = null
    ),
    val categories: List<CategoryWithTransactionMetadata> = emptyList(),
    val selectedCategory: CategoryWithTransactionMetadata = CategoryWithTransactionMetadata(
        categoryId = 0L,
        categoryName = "",
        icon = TablerIcons.Archive,
        transactionCount = 0L,
        lastUsed = null
    ),
    val counterParties: List<CounterPartyWithTransactionMetadata> = emptyList(),
    val selectedCounterParty: CounterPartyWithTransactionMetadata = CounterPartyWithTransactionMetadata(
        counterPartyId = 0L,
        counterPartyName = "",
        transactionCount = 0L,
        lastUsed = null
    ),
    val methods: List<MethodWithTransactionMetadata> = emptyList(),
    val selectedMethod: MethodWithTransactionMetadata = MethodWithTransactionMetadata(
        methodId = 0L,
        methodName = "",
        transactionCount = 0L,
        lastUsed = null
    ),
    val loading: Boolean = true,
    val enabled: Boolean = false,
    val locale: Locale? = null
)
