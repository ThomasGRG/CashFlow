package jp.ikigai.cash.flow.data.entity

import android.icu.util.Currency
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PersistedName
import io.realm.kotlin.types.annotations.PrimaryKey
import jp.ikigai.cash.flow.data.enums.TransactionType
import jp.ikigai.cash.flow.utils.toEpochMilli
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class Transaction() : RealmObject {
    @PrimaryKey
    var uuid: String = ""
    var title: String = ""
    var description: String = ""
    var amount: Double = 0.0

    private var typeId: Int = TransactionType.DEBIT.id
    var type: TransactionType
        get() {
            for (type in TransactionType.values()) {
                if (type.id == typeId) return type
            }
            return TransactionType.DEBIT
        }
        set(value) {
            typeId = value.id
        }

    var currency: String = Currency.getInstance("INR").currencyCode

    @PersistedName("time")
    private var _time: Long = ZonedDateTime.now(ZoneId.systemDefault()).toEpochMilli()
    var time: ZonedDateTime
        get() {
            return Instant.ofEpochMilli(_time).atZone(ZoneId.systemDefault())
        }
        set(value) {
            _time = value.toInstant().toEpochMilli()
        }

    var category: Category? = null
    var counterParty: CounterParty? = null
    var method: Method? = null
    var source: Source? = null
    
    constructor(title: String, description: String): this() {
        this.title = title
        this.description = description
    }
}