package jp.ikigai.cash.flow.ui.screenStates.listing

import io.realm.kotlin.query.Sort
import jp.ikigai.cash.flow.data.dto.ItemListingDTO
import java.util.Locale

data class ItemsScreenState(
    val items: List<ItemListingDTO> = emptyList(),
    val itemNames: List<String> = emptyList(),
    val count: Long = 0,
    val countString: String = "",
    val loading: Boolean = true,
    val enabled: Boolean = true,
    val selectedItem: ItemListingDTO? = null,
    val searchText: String = "",
    val sortDirection: Sort = Sort.DESCENDING,
    val sortField: String = "frequency",
    val locale: Locale? = null
)
