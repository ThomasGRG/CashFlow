package jp.ikigai.cash.flow.ui.components.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Typography
import jp.ikigai.cash.flow.ui.components.common.RoundedCornerOutlinedTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpsertItemSheet(
    nameFieldValue: TextFieldValue,
    setName: (TextFieldValue) -> Unit,
    nameValid: Boolean,
    enabled: Boolean,
    save: () -> Unit,
    dismiss: () -> Unit,
    sheetState: SheetState,
    keyboardController: SoftwareKeyboardController?
) {
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = {
            dismiss()
        },
        shape = RoundedCornerShape(10),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RoundedCornerOutlinedTextField(
                enabled = enabled,
                value = nameFieldValue,
                onValueChange = setName,
                label = "Name",
                icon = TablerIcons.Typography,
                iconDescription = "name icon",
                isError = !nameValid,
                errorHint = "Name cannot be empty",
                onDone = {
                    keyboardController?.hide()
                }
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            TextButton(
                onClick = {
                    dismiss()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Cancel")
            }
            TextButton(
                onClick = {
                    dismiss()
                    save()
                },
                modifier = Modifier.weight(1f),
                enabled = nameFieldValue.text.isNotBlank()
            ) {
                Text(text = "Save")
            }
        }
    }
}