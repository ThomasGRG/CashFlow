package jp.ikigai.cash.flow.data.entity

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PersistedName
import io.realm.kotlin.types.annotations.PrimaryKey
import jp.ikigai.cash.flow.data.enums.TransactionType
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class TransactionTemplate() : RealmObject {
    @PrimaryKey
    var uuid: String = ""
    var name: String = ""
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

    var category: Category? = null
    var counterParty: CounterParty? = null
    var method: Method? = null
    var source: Source? = null
    var frequency: Int = 0

    @PersistedName("lastUsed")
    private var _lastUsed: Long = 0L
    var lastUsed: ZonedDateTime
        get() {
            return Instant.ofEpochMilli(_lastUsed).atZone(ZoneId.systemDefault())
        }
        set(value) {
            _lastUsed = value.toInstant().toEpochMilli()
        }
}