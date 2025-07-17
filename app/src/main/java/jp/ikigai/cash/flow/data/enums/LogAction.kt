package jp.ikigai.cash.flow.data.enums

import androidx.annotation.StringRes
import jp.ikigai.cash.flow.R

enum class LogAction(
    @StringRes val label: Int
) {
    CREATE(R.string.create_label),
    UPDATE(R.string.update_label),
    DELETE(R.string.delete_label),
    BALANCE_UPDATE(R.string.balance_updated_label)
}