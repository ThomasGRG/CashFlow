package jp.ikigai.cash.flow.data.adapters

import app.cash.sqldelight.ColumnAdapter
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

val ZonedDateTimeAdapter = object : ColumnAdapter<ZonedDateTime, Long> {
    override fun decode(databaseValue: Long): ZonedDateTime {
        return Instant.ofEpochMilli(databaseValue).atZone(ZoneId.systemDefault())
    }

    override fun encode(value: ZonedDateTime): Long {
        return value.toInstant().toEpochMilli()
    }
}