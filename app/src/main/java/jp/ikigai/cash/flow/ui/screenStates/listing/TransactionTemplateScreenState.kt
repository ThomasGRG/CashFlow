package jp.ikigai.cash.flow.ui.screenStates.listing

import jp.ikigai.cash.flow.data.dto.TransactionTemplateWithIcons

data class TransactionTemplateScreenState(
    val templates: List<TransactionTemplateWithIcons> = emptyList(),
    val loading: Boolean = true,
)
