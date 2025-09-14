package jp.ikigai.cash.flow.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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
fun MultiSelectCard(
    checked: () -> Boolean,
    label: AnnotatedString,
    onClick: (Boolean) -> Unit,
    modifier: Modifier
) {
    OutlinedCard(
        onClick = {
            onClick(!checked())
        },
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
            Checkbox(checked = checked(), onCheckedChange = null)
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun MultiSelectCard(
    checked: () -> Boolean,
    label: String,
    onClick: (Boolean) -> Unit,
    modifier: Modifier
) {
    OutlinedCard(
        onClick = {
            onClick(!checked())
        },
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
            Checkbox(checked = checked(), onCheckedChange = null)
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun MultiSelectCard(
    checked: () -> Boolean,
    label: AnnotatedString,
    icon: ImageVector,
    iconTint: Color = MaterialTheme.colorScheme.onBackground,
    onClick: (Boolean) -> Unit,
    modifier: Modifier
) {
    OutlinedCard(
        onClick = {
            onClick(!checked())
        },
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
                Checkbox(checked = checked(), onCheckedChange = null)
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
fun MultiSelectCardPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(10.dp)
    ) {
        MultiSelectCard(
            checked = { true },
            label = AnnotatedString("Test"),
            onClick = {},
            modifier = Modifier
        )
        MultiSelectCard(
            checked = { true },
            label = AnnotatedString("Test"),
            icon = TablerIcons.BuildingBank,
            onClick = {},
            modifier = Modifier
        )
        MultiSelectCard(
            checked = { false },
            label = AnnotatedString("Test aklnd enui ioaend uaenf ioawndu aebfg"),
            onClick = {},
            modifier = Modifier
        )
        MultiSelectCard(
            checked = { false },
            label = AnnotatedString("Test oiadnu uanei ounbif ounae oinuf jdfih gfouni"),
            icon = TablerIcons.BuildingBank,
            onClick = {},
            modifier = Modifier
        )
    }
}