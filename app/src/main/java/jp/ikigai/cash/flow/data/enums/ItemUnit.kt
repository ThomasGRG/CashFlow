package jp.ikigai.cash.flow.data.enums

import androidx.annotation.StringRes
import jp.ikigai.cash.flow.R

enum class ItemUnit(val id: Int, @StringRes val code: Int) {
    GRAM(1, R.string.gram_label),
    KILOGRAM(2, R.string.kilogram_label),
    LITER(3, R.string.Liter_label),
    MILLILITER(4, R.string.milliLiter_label),
    POUND(5, R.string.pound_label),
    OUNCE(6, R.string.ounce_label),
    FLUID_OUNCE(7, R.string.fluid_ounce_label),
    PIECE(8, R.string.piece_label),
}