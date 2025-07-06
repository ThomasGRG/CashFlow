package jp.ikigai.cash.flow.ui.screenStates.listing

import jp.ikigai.cash.flow.data.dto.CommonListingDTO
import java.util.Locale

data class CategoryScreenState(
    val categories: List<CommonListingDTO> = emptyList(),
    val count: Long = 0,
    val countString: String = "",
    val loading: Boolean = true,
    val locale: Locale? = null
)
