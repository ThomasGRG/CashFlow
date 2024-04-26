package jp.ikigai.cash.flow.data.enums

enum class ItemUnit(val id: Int, val code: String) {
    GRAM(1, "g"),
    KILOGRAM(2, "kg"),
    LITER(3, "L"),
    MILLILITER(4, "mL"),
    POUND(5, "lb"),
    OUNCE(6, "oz"),
    FLUID_OUNCE(7, "fl oz"),
    PIECE(8, ""),
}