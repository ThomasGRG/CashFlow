package jp.ikigai.cash.flow.ui.components.popups

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Archive
import compose.icons.tablericons.BrandGithub
import compose.icons.tablericons.BuildingBank
import compose.icons.tablericons.ClipboardList
import compose.icons.tablericons.CreditCard
import compose.icons.tablericons.TriangleSquareCircle
import compose.icons.tablericons.Users
import jp.ikigai.cash.flow.R

@Composable
fun MoreOptionsPopup(
    navigateToCategoriesScreen: () -> Unit,
    navigateToCounterPartyScreen: () -> Unit,
    navigateToMethodsScreen: () -> Unit,
    navigateToSourcesScreen: () -> Unit,
    navigateToTemplatesScreen: () -> Unit,
    navigateToItemsScreen: () -> Unit,
    openGithubReleasesPage: () -> Unit,
    dismiss: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
            .padding(top = 20.dp, start = 5.dp, end = 5.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
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
                    dismiss()
                    navigateToCategoriesScreen()
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
                Text(
                    text = stringResource(id = R.string.categories_label),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    dismiss()
                    navigateToMethodsScreen()
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
                Text(
                    text = stringResource(id = R.string.methods_label),
                    style = MaterialTheme.typography.titleMedium
                )
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
                    dismiss()
                    navigateToCounterPartyScreen()
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
                Text(
                    text = stringResource(id = R.string.counter_parties_label),
                    style = MaterialTheme.typography.titleMedium
                )
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
                    dismiss()
                    navigateToSourcesScreen()
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
                Text(
                    text = stringResource(id = R.string.sources_label),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    dismiss()
                    navigateToItemsScreen()
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
                Text(
                    text = stringResource(id = R.string.items_label),
                    style = MaterialTheme.typography.titleMedium
                )
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
                    dismiss()
                    navigateToTemplatesScreen()
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
                Text(
                    text = stringResource(id = R.string.templates_label),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    dismiss()
                    openGithubReleasesPage()
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
                Text(
                    text = stringResource(id = R.string.updates_label),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Preview
@Composable
fun MoreOptionsPopupPreview() {
    MoreOptionsPopup(
        navigateToCategoriesScreen = {},
        navigateToCounterPartyScreen = {},
        navigateToMethodsScreen = {},
        navigateToSourcesScreen = {},
        navigateToTemplatesScreen = {},
        navigateToItemsScreen = {},
        openGithubReleasesPage = {},
        dismiss = {}
    )
}