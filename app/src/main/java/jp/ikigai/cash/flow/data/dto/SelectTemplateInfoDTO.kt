package jp.ikigai.cash.flow.data.dto

import androidx.compose.ui.text.AnnotatedString

data class SelectTemplateInfoDTO(
    val uuid: String,
    val annotatedName: AnnotatedString,
    val frequency: String
)
