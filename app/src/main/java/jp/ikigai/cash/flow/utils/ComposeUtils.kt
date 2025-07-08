package jp.ikigai.cash.flow.utils

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

val TextFieldValueSaver = Saver<MutableState<TextFieldValue>, String>(
    save = { it.component1().text },
    restore = { mutableStateOf(TextFieldValue(it, TextRange(it.length))) }
)