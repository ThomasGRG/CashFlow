package jp.ikigai.cash.flow.data.converters

import io.objectbox.converter.PropertyConverter
import jp.ikigai.cash.flow.data.enums.TransactionType

class TransactionTypeConverter : PropertyConverter<TransactionType?, Int?> {

    override fun convertToEntityProperty(databaseValue: Int?): TransactionType? {
        return TransactionType.values().find { it.id == databaseValue }
    }

    override fun convertToDatabaseValue(entityProperty: TransactionType?): Int? {
        return entityProperty?.id
    }
}