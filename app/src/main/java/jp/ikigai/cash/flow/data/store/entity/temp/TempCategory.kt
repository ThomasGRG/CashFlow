package jp.ikigai.cash.flow.data.store.entity.temp

import androidx.compose.ui.graphics.vector.ImageVector
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Unique
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.converters.ImageVectorConverter
import jp.ikigai.cash.flow.data.converters.ZonedDateTimeConverter
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

@Entity
data class TempCategory(
    @Id
    var id: Long = 0L,

    @Unique
    var name: String = "",

    @Convert(converter = ImageVectorConverter::class, dbType = String::class)
    var icon: ImageVector = Constants.DEFAULT_CATEGORY_ICON,

    var frequency: Int = 0,

    @Convert(converter = ZonedDateTimeConverter::class, dbType = Long::class)
    var lastUsed: ZonedDateTime = Instant.EPOCH.atZone(ZoneId.systemDefault())
)
