package jp.ikigai.cash.flow.data.store.entity.temp

import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToOne
import jp.ikigai.cash.flow.data.converters.TransactionTypeConverter
import jp.ikigai.cash.flow.data.converters.ZonedDateTimeConverter
import jp.ikigai.cash.flow.data.enums.TransactionType
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

@Entity
data class TempTransactionTemplate(
    @Id
    var id: Long = 0L,

    var name: String = "",
    var title: String = "",
    var description: String = "",
    var amount: Double = 0.0,

    @Convert(converter = TransactionTypeConverter::class, dbType = Int::class)
    var type: TransactionType = TransactionType.DEBIT,

    var frequency: Int = 0,

    @Convert(converter = ZonedDateTimeConverter::class, dbType = Long::class)
    var lastUsed: ZonedDateTime = Instant.EPOCH.atZone(ZoneId.systemDefault())
) {
    lateinit var account: ToOne<TempAccount>
    lateinit var category: ToOne<TempCategory>
    lateinit var counterParty: ToOne<TempCounterParty>
    lateinit var method: ToOne<TempMethod>
}
