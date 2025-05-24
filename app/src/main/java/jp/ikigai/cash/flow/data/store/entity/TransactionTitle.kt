package jp.ikigai.cash.flow.data.store.entity

import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Unique
import jp.ikigai.cash.flow.data.converters.ZonedDateTimeConverter
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

@Entity
data class TransactionTitle(
    @Id
    var id: Long = 0L,

    @Unique
    var title: String = "",
    var frequency: Int = 0,

    @Convert(converter = ZonedDateTimeConverter::class, dbType = Long::class)
    var lastUsed: ZonedDateTime = Instant.EPOCH.atZone(ZoneId.systemDefault())
)
