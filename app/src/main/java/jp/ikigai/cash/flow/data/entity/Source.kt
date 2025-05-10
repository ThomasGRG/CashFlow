package jp.ikigai.cash.flow.data.entity

import android.icu.util.Currency
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.Ignore
import io.realm.kotlin.types.annotations.PersistedName
import io.realm.kotlin.types.annotations.PrimaryKey
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class Source() : RealmObject {
    @PrimaryKey
    var uuid: String = ""
    var name: String = ""
    var currency: String = Currency.getInstance("INR").currencyCode
    var balance: Double = 0.0

    @Ignore
    var displayBalance: String = ""
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