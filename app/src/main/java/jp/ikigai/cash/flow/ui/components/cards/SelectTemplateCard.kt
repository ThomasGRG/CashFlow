package jp.ikigai.cash.flow.ui.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jp.ikigai.cash.flow.data.dto.SelectTemplateInfoDTO

@Composable
fun SelectTemplateCard(
    data: SelectTemplateInfoDTO,
    onClick: (String) -> Unit,
    modifier: Modifier
) {
    OutlinedCard(
        onClick = {
            onClick(data.uuid)
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
            Text(
                text = data.annotatedName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .padding(end = 10.dp)
            )
            Text(
                text = data.frequency,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

@Preview
@Composable
fun SelectTemplateCardPreview() {
    Column {
        SelectTemplateCard(
            data = SelectTemplateInfoDTO(
                uuid = "",
                annotatedName = AnnotatedString("Test"),
                frequency = "3,246"
            ),
            onClick = {},
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(10.dp)
        )
        SelectTemplateCard(
            data = SelectTemplateInfoDTO(
                uuid = "",
                annotatedName = AnnotatedString("Test oinaduoebnou andioa enoifnda oie niaeoaoie"),
                frequency = "3.2k"
            ),
            onClick = {},
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(10.dp)
        )
        SelectTemplateCard(
            data = SelectTemplateInfoDTO(
                uuid = "",
                annotatedName = AnnotatedString("Test"),
                frequency = "0"
            ),
            onClick = {},
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(10.dp)
        )
        SelectTemplateCard(
            data = SelectTemplateInfoDTO(
                uuid = "",
                annotatedName = AnnotatedString("Test ajnduane uaoi noien doai naoi adaw wadwa"),
                frequency = "34"
            ),
            onClick = {},
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(10.dp)
        )
    }
}
