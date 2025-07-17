package jp.ikigai.cash.flow.data.enums

import androidx.annotation.StringRes
import jp.ikigai.cash.flow.R

enum class EntityType(
    @StringRes val label: Int
) {
    TRANSACTION(R.string.transaction_label),
    ACCOUNT(R.string.account_field_label)
}