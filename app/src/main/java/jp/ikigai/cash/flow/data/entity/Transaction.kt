package jp.ikigai.cash.flow.data.entity

import android.icu.util.Currency
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import jp.ikigai.cash.flow.data.enums.TransactionType
import jp.ikigai.cash.flow.utils.toEpochMilli
import java.time.ZoneId
import java.time.ZonedDateTime

class Transaction() : RealmObject {
    @PrimaryKey
    var uuid: String = ""
    var title: String = ""
    var description: String = ""
    var amount: Double = 0.0
    var taxAmount: Double = 0.0

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
    var category: Category? = null
    var counterParty: CounterParty? = null
    var method: Method? = null
    var source: Source? = null
    var items: RealmList<TransactionItem> = realmListOf()
}