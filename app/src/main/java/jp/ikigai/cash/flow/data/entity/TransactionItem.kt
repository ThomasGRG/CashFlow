package jp.ikigai.cash.flow.data.entity

import io.realm.kotlin.types.EmbeddedRealmObject
import jp.ikigai.cash.flow.data.enums.ItemUnit

class TransactionItem() : EmbeddedRealmObject {

    constructor(item: Item?, unit: ItemUnit, price: Double, quantity: Double) : this() {
        this.item = item
        this.unit = unit
        this.price = price
        this.quantity = quantity
    }

    var item: Item? = null

    private var unitId: Int = ItemUnit.PIECE.id
    var unit: ItemUnit
        get() {
            for (unit in ItemUnit.values()) {
                if (unit.id == unitId) return unit
            }
            return ItemUnit.PIECE
        }
        set(value) {
            unitId = value.id
        }

    var price: Double = 0.0
    var quantity: Double = 0.0
}