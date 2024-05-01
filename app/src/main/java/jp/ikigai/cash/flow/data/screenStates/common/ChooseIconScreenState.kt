package jp.ikigai.cash.flow.data.screenStates.common

import androidx.compose.ui.graphics.vector.ImageVector

data class ChooseIconScreenState(
    val icons: List<ImageVector> = emptyList(),
    val iconCount: String = "0",
    val loading: Boolean = true,
    val searchText: String = ""
)
