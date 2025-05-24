package jp.ikigai.cash.flow.data.store.entity.temp

import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToOne
import jp.ikigai.cash.flow.data.converters.TransactionTypeConverter
import jp.ikigai.cash.flow.data.converters.ZonedDateTimeConverter
import jp.ikigai.cash.flow.data.enums.TransactionType
import java.time.ZoneId
import java.time.ZonedDateTime

@Entity
data class TempTransaction(
    @Id
    var id: Long = 0L,

    var title: String = "",
    var description: String = "",
    var amount: Double = 0.0,
    var currency: String = "",

    @Convert(converter = TransactionTypeConverter::class, dbType = Int::class)
    var type: TransactionType = TransactionType.DEBIT,

    @Convert(converter = ZonedDateTimeConverter::class, dbType = Long::class)
    var time: ZonedDateTime = ZonedDateTime.now(ZoneId.systemDefault())
) {
    lateinit var account: ToOne<TempAccount>
    lateinit var category: ToOne<TempCategory>
    lateinit var counterParty: ToOne<TempCounterParty>
    lateinit var method: ToOne<TempMethod>
}
