package jp.ikigai.cash.flow.data.enums

import androidx.annotation.StringRes
import jp.ikigai.cash.flow.R

enum class ItemUnit(val id: Int, @StringRes val label: Int, @StringRes val code: Int) {
    GRAM(1, R.string.gram_label, R.string.gram_code),
    KILOGRAM(2, R.string.kilogram_label, R.string.kilogram_code),
    LITER(3, R.string.liter_label, R.string.liter_code),
    MILLILITER(4, R.string.milliLiter_label, R.string.milliLiter_code),
    POUND(5, R.string.pound_label, R.string.pound_code),
    OUNCE(6, R.string.ounce_label, R.string.ounce_code),
    FLUID_OUNCE(7, R.string.fluid_ounce_label, R.string.fluid_ounce_code),
    PIECE(8, R.string.piece_label, R.string.piece_label),
}