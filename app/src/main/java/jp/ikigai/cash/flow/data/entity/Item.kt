package jp.ikigai.cash.flow.data.entity

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import jp.ikigai.cash.flow.data.enums.ItemUnit

class Item() : RealmObject {
    @PrimaryKey
    var uuid: String = ""
    var name: String = ""
    private var lastUsedUnitId: Int = ItemUnit.PIECE.id
    var lastUsedUnit: ItemUnit
        get() {
            for (unit in ItemUnit.values()) {
                if (unit.id == lastUsedUnitId) return unit
            }
            return ItemUnit.PIECE
        }
        set(value) {
            lastUsedUnitId = value.id
        }
    var lastUsedCurrency: String = ""
    var lastKnownPrice: Double = 0.0
    var frequency: Int = 0
    var lastUsed: Long = 0L
}