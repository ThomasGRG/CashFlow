package jp.ikigai.cash.flow.ui.screenStates.listing

import jp.ikigai.cash.flow.data.dto.SourceListingDTO
import java.util.Locale

data class SourceScreenState(
    val sources: List<SourceListingDTO> = emptyList(),
    val count: Long = 0,
    val countString: String = "",
    val loading: Boolean = true,
    val locale: Locale? = null
)
