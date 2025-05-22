package jp.ikigai.cash.flow.data.store.entity.temp

import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToOne
import jp.ikigai.cash.flow.data.converters.TransactionTypeConverter
import jp.ikigai.cash.flow.data.converters.ZonedDateTimeConverter
import jp.ikigai.cash.flow.data.enums.TransactionType
import jp.ikigai.cash.flow.data.store.entity.Account
import jp.ikigai.cash.flow.data.store.entity.Category
import jp.ikigai.cash.flow.data.store.entity.CounterParty
import jp.ikigai.cash.flow.data.store.entity.Method
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
    lateinit var account: ToOne<Account>
    lateinit var category: ToOne<Category>
    lateinit var counterParty: ToOne<CounterParty>
    lateinit var method: ToOne<Method>
}
