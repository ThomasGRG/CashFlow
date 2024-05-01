package jp.ikigai.cash.flow.data.entity

import androidx.compose.ui.graphics.vector.ImageVector
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import jp.ikigai.cash.flow.utils.getIconForCategory

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
    var lastUsed: Long = 0L
}