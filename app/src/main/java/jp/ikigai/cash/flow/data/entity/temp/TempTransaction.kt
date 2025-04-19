package jp.ikigai.cash.flow.data.entity.temp

import android.icu.util.Currency
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import jp.ikigai.cash.flow.data.enums.TransactionType
import jp.ikigai.cash.flow.utils.toEpochMilli
import java.time.ZoneId
import java.time.ZonedDateTime

class TempTransaction() : RealmObject {
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
    var time: Long = ZonedDateTime.now(ZoneId.of("UTC")).toEpochMilli()
    var category: TempCategory? = null
    var counterParty: TempCounterParty? = null
    var method: TempMethod? = null
    var source: TempSource? = null
}