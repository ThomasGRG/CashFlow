package jp.ikigai.cash.flow.ui.screenStates.common

import androidx.compose.ui.graphics.vector.ImageVector

data class ChooseIconScreenState(
    val icons: List<ImageVector> = emptyList(),
    val iconCount: Int = 0,
    val loading: Boolean = true,
    val searching: Boolean = false,
    val searchText: String = ""
)
