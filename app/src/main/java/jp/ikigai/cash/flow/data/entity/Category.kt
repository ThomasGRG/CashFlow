package jp.ikigai.cash.flow.data.entity

import androidx.compose.ui.graphics.vector.ImageVector
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PersistedName
import io.realm.kotlin.types.annotations.PrimaryKey
import jp.ikigai.cash.flow.utils.getIconForCategory
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class Category() : RealmObject {
    @PrimaryKey
    var uuid: String = ""
    var name: String = ""

    private var iconName: String = ""
    var icon: ImageVector
        get() {
            return iconName.getIconForCategory()
        }
        set(value) {
            iconName = value.name
        }

    var frequency: Int = 0

    @PersistedName("lastUsed")
    private var _lastUsed: Long = 0L
    var lastUsed: ZonedDateTime
        get() {
            return Instant.ofEpochMilli(_lastUsed).atZone(ZoneId.systemDefault())
        }
        set(value) {
            _lastUsed = value.toInstant().toEpochMilli()
        }
}