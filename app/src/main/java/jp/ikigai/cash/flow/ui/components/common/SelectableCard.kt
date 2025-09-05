package jp.ikigai.cash.flow.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.BuildingBank

@Composable
fun SelectableCard(
    checked: () -> Boolean,
    label: AnnotatedString,
    onClick: () -> Unit,
    modifier: Modifier
) {
    OutlinedCard(
        onClick = onClick,
        colors = CardDefaults.outlinedCardColors(
            containerColor = Color.Transparent
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            RadioButton(selected = checked(), onClick = null)
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun SelectableCard(
    checked: () -> Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier
) {
    OutlinedCard(
        onClick = onClick,
        colors = CardDefaults.outlinedCardColors(
            containerColor = Color.Transparent
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            RadioButton(selected = checked(), onClick = null)
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun SelectableCard(
    checked: () -> Boolean,
    label: AnnotatedString,
    icon: ImageVector,
    iconTint: Color = MaterialTheme.colorScheme.onBackground,
    onClick: () -> Unit,
    modifier: Modifier
) {
    OutlinedCard(
        onClick = onClick,
        colors = CardDefaults.outlinedCardColors(
            containerColor = Color.Transparent
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                RadioButton(selected = checked(), onClick = null)
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(end = 10.dp)
                )
            }
            Icon(
                imageVector = icon,
                contentDescription = icon.name,
                tint = iconTint
            )
        }
    }
}

@Preview
@Composable
fun SelectableCardPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(10.dp)
    ) {
        SelectableCard(
            checked = { true },
            label = AnnotatedString("Test"),
            icon = TablerIcons.BuildingBank,
            onClick = {},
            modifier = Modifier
        )
        SelectableCard(
            checked = { true },
            label = AnnotatedString("Test"),
            onClick = {},
            modifier = Modifier
        )
        SelectableCard(
            checked = { false },
            label = AnnotatedString("Test aklnd enui ioaend uaenf ioawndu aebfg"),
            icon = TablerIcons.BuildingBank,
            onClick = {},
            modifier = Modifier
        )
        SelectableCard(
            checked = { false },
            label = AnnotatedString("Test oiadnu uanei ounbif ounae oinuf jdfih gfouni"),
            onClick = {},
            modifier = Modifier
        )
    }
}