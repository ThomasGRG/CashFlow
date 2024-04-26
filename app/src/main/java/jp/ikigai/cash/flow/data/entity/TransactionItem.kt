package jp.ikigai.cash.flow.data.entity

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import jp.ikigai.cash.flow.data.enums.ItemUnit
import org.mongodb.kbson.ObjectId

class TransactionItem : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var unit: Int = ItemUnit.PIECE.id
    var price: Double = 0.0
    var quantity: Double = 0.0
    var item: Item? = null
}