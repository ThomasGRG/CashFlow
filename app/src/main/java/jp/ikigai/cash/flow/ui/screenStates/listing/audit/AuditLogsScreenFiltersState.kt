package jp.ikigai.cash.flow.ui.screenStates.listing.audit

import androidx.annotation.StringRes
import jp.ikigai.cash.flow.R
import java.time.ZonedDateTime

data class AuditLogsScreenFiltersState(
    val startDate: ZonedDateTime? = null,
    val endDate: ZonedDateTime? = null,
    val startDateString: String = "",
    val endDateString: String = "",
    @StringRes val dateRangeStringRes: Int = R.string.all_time_date_range_label
)
