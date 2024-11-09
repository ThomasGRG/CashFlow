package jp.ikigai.cash.flow.ui.screenStates.listing

import io.realm.kotlin.query.Sort
import jp.ikigai.cash.flow.data.dto.TransactionTemplateWithIcons
import java.util.Locale

data class TransactionTemplateScreenState(
    val templates: List<TransactionTemplateWithIcons> = emptyList(),
    val count: Long = 0,
    val countString: String = "",
    val loading: Boolean = true,
    val searchText: String = "",
    val sortDirection: Sort = Sort.DESCENDING,
    val sortField: String = "frequency",
    val locale: Locale? = null
)
