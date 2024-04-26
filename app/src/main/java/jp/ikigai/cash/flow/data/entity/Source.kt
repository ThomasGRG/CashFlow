package jp.ikigai.cash.flow.data.entity

import android.icu.util.Currency
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class Source : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var name: String = ""
    var iconName: String = ""
    var currency: String = Currency.getInstance("INR").currencyCode
    var balance: Double = 0.0
    var frequency: Int = 0
    var lastUsed: Long = 0L
}