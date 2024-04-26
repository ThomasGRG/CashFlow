package jp.ikigai.cash.flow.data.entity

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import jp.ikigai.cash.flow.data.enums.ItemUnit
import org.mongodb.kbson.ObjectId

class Item : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var name: String = ""
    var lastUsedUnit: Int = ItemUnit.PIECE.id
    var lastUsedCurrency: String = ""
    var lastKnownPrice: Double = 0.0
    var frequency: Int = 0
    var lastUsed: Long = 0L
}