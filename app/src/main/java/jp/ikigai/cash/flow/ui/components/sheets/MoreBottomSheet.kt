package jp.ikigai.cash.flow.ui.components.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Archive
import compose.icons.tablericons.BrandGithub
import compose.icons.tablericons.BuildingBank
import compose.icons.tablericons.ClipboardList
import compose.icons.tablericons.CreditCard
import compose.icons.tablericons.TriangleSquareCircle
import compose.icons.tablericons.Users
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreBottomSheet(
    navigateToCategoriesScreen: () -> Unit,
    navigateToCounterPartyScreen: () -> Unit,
    navigateToMethodsScreen: () -> Unit,
    navigateToSourcesScreen: () -> Unit,
    navigateToTemplatesScreen: () -> Unit,
    navigateToItemsScreen: () -> Unit,
    openGithubReleasesPage: () -> Unit,
    dismiss: () -> Unit,
    sheetState: SheetState,
) {
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = {
            scope.launch {
                sheetState.hide()
                dismiss()
            }
        },
        shape = RoundedCornerShape(10)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp, top = 5.dp, bottom = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    scope.launch {
                        sheetState.hide()
                        dismiss()
                        navigateToCategoriesScreen()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 58.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = TablerIcons.Archive,
                    contentDescription = TablerIcons.Archive.name
                )
                Text(text = "Categories", style = MaterialTheme.typography.titleMedium)
            }
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    scope.launch {
                        sheetState.hide()
                        dismiss()
                        navigateToCounterPartyScreen()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 58.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = TablerIcons.Users,
                    contentDescription = TablerIcons.Users.name
                )
                Text(text = "Counter party", style = MaterialTheme.typography.titleMedium)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp, top = 5.dp, bottom = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    scope.launch {
                        sheetState.hide()
                        dismiss()
                        navigateToMethodsScreen()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 58.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = TablerIcons.CreditCard,
                    contentDescription = TablerIcons.CreditCard.name
                )
                Text(text = "Methods", style = MaterialTheme.typography.titleMedium)
            }
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    scope.launch {
                        sheetState.hide()
                        dismiss()
                        navigateToSourcesScreen()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 58.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = TablerIcons.BuildingBank,
                    contentDescription = TablerIcons.BuildingBank.name
                )
                Text(text = "Sources", style = MaterialTheme.typography.titleMedium)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp, top = 5.dp, bottom = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    scope.launch {
                        sheetState.hide()
                        dismiss()
                        navigateToItemsScreen()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 58.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = TablerIcons.TriangleSquareCircle,
                    contentDescription = TablerIcons.TriangleSquareCircle.name
                )
                Text(text = "Items", style = MaterialTheme.typography.titleMedium)
            }
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    scope.launch {
                        sheetState.hide()
                        dismiss()
                        navigateToTemplatesScreen()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 58.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = TablerIcons.ClipboardList,
                    contentDescription = TablerIcons.ClipboardList.name
                )
                Text(text = "Templates", style = MaterialTheme.typography.titleMedium)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp, top = 5.dp, bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    scope.launch {
                        sheetState.hide()
                        dismiss()
                        openGithubReleasesPage()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 58.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = TablerIcons.BrandGithub,
                    contentDescription = TablerIcons.BrandGithub.name
                )
                Text(text = "Updates", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}