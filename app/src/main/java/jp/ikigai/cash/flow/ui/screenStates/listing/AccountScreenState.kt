package jp.ikigai.cash.flow.ui.screenStates.listing

import jp.ikigai.cash.flow.data.dto.AccountListingDTO
import java.util.Locale

data class AccountScreenState(
    val accounts: List<AccountListingDTO> = emptyList(),
    val count: Int = 0,
    val countString: String = "",
    val loading: Boolean = true,
    val locale: Locale? = null
)
