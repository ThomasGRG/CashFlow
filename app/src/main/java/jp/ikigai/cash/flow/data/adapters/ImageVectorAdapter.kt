package jp.ikigai.cash.flow.data.adapters

import androidx.compose.ui.graphics.vector.ImageVector
import app.cash.sqldelight.ColumnAdapter
import compose.icons.AllIcons
import compose.icons.TablerIcons
import compose.icons.tablericons.Archive

val ImageVectorAdapter = object : ColumnAdapter<ImageVector, String> {
    override fun decode(databaseValue: String): ImageVector {
        return TablerIcons.AllIcons.find { it.name == databaseValue } ?: TablerIcons.Archive
    }

    override fun encode(value: ImageVector): String {
        return value.name
    }
}