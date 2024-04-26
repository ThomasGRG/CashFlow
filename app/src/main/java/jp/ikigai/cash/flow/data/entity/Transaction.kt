package jp.ikigai.cash.flow.data.entity

import android.icu.util.Currency
import io.realm.kotlin.ext.realmSetOf
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmSet
import io.realm.kotlin.types.annotations.PrimaryKey
import jp.ikigai.cash.flow.data.enums.TransactionType
import jp.ikigai.cash.flow.utils.toEpochMilli
import org.mongodb.kbson.ObjectId
import java.time.ZoneId
import java.time.ZonedDateTime

class Transaction : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var title: String = ""
    var description: String = ""
    var amount: Double = 0.0
    var taxAmount: Double = 0.0
    var type: Int = TransactionType.DEBIT.id
    var currency: String = Currency.getInstance("INR").currencyCode
    var time: Long = ZonedDateTime.now(ZoneId.of("UTC")).toEpochMilli()
    var category: Category? = null
    var counterParty: CounterParty? = null
    var method: Method? = null
    var source: Source? = null
    var items: RealmSet<TransactionItem> = realmSetOf()
}