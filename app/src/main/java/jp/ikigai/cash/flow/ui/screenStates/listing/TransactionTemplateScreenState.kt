package jp.ikigai.cash.flow.ui.screenStates.listing

import jp.ikigai.cash.flow.data.dto.TemplateWithChips
import java.util.Locale

data class TransactionTemplateScreenState(
    val templates: List<TemplateWithChips> = emptyList(),
    val count: Long = 0,
    val countString: String = "",
    val loading: Boolean = true,
    val locale: Locale? = null
)
