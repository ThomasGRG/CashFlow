package jp.ikigai.cash.flow.data.converters

import io.objectbox.converter.PropertyConverter
import jp.ikigai.cash.flow.utils.toEpochMilli
import jp.ikigai.cash.flow.utils.toZonedDateTime
import java.time.ZonedDateTime

class ZonedDateTimeConverter : PropertyConverter<ZonedDateTime?, Long?> {

    override fun convertToEntityProperty(databaseValue: Long?): ZonedDateTime? {
        if (databaseValue !== null) {
            return databaseValue.toZonedDateTime()
        }
        return null
    }

    override fun convertToDatabaseValue(entityProperty: ZonedDateTime?): Long? {
        return entityProperty?.toEpochMilli()
    }
}