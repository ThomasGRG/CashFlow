package jp.ikigai.cash.flow.data.entity

import android.icu.util.Currency
import androidx.compose.ui.graphics.vector.ImageVector
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import jp.ikigai.cash.flow.utils.getIconForSource

class Source() : RealmObject {
    @PrimaryKey
    var uuid: String = ""
    var name: String = ""
    private var iconName: String = ""
    var icon: ImageVector
        get() {
            return iconName.getIconForSource()
        }
        set(value) {
            iconName = value.name
        }
    var currency: String = Currency.getInstance("INR").currencyCode
    var balance: Double = 0.0
    var frequency: Int = 0
    var lastUsed: Long = 0L
}