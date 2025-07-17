package jp.ikigai.cash.flow.data.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class ZonedDateTimeJSONAdapter {

    @ToJson
    fun toJson(zonedDateTime: ZonedDateTime): Long {
        return zonedDateTime.toInstant().toEpochMilli()
    }

    @FromJson
    fun fromJson(epochMilli: Long): ZonedDateTime {
        return Instant.ofEpochMilli(epochMilli).atZone(ZoneId.systemDefault())
    }
}