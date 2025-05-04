package jp.ikigai.cash.flow.data.entity

import android.icu.util.Currency
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Ignore
import io.realm.kotlin.types.annotations.PrimaryKey

class Source() : RealmObject {
    @PrimaryKey
    var uuid: String = ""
    var name: String = ""
    var currency: String = Currency.getInstance("INR").currencyCode
    var balance: Double = 0.0

    @Ignore
    var displayBalance: String = ""
    var frequency: Int = 0
    var lastUsed: Long = 0L
}