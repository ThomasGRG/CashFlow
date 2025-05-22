package jp.ikigai.cash.flow.data.converters

import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.AllIcons
import compose.icons.TablerIcons
import io.objectbox.converter.PropertyConverter

class ImageVectorConverter : PropertyConverter<ImageVector?, String?> {

    override fun convertToEntityProperty(databaseValue: String?): ImageVector? {
        return TablerIcons.AllIcons.find { it.name == databaseValue }
    }

    override fun convertToDatabaseValue(entityProperty: ImageVector?): String? {
        return entityProperty?.name
    }
}