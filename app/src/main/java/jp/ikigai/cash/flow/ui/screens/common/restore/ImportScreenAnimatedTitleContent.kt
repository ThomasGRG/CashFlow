/*
CashFlow - Expense Tracker
Copyright (C) 2025 ThomasGRG

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 3 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package jp.ikigai.cash.flow.ui.screens.common.restore

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import jp.ikigai.cash.flow.R

@Composable
fun ImportScreenAnimatedTitleContent(
    currentPage: Int,
    headers: List<Int>,
    subHeaders: List<String>,
    dateRangeStringRes: Int,
    startDateString: String,
    endDateString: String,
    titleStyle: TextStyle? = null,
) {
    AnimatedContent(
        targetState = currentPage,
        label = "import_header_animated_content",
        transitionSpec = {
            if (targetState > initialState) {
                // If the target page is larger, it slides from end and fades in
                // while the initial (smaller) number slides out and fades out.
                slideInHorizontally { width -> width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> -width } + fadeOut()
            } else {
                // If the target number is smaller, it slides from start and fades in
                // while the initial number slides out and fades out.
                slideInHorizontally { width -> -width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> width } + fadeOut()
            }.using(
                // Disable clipping since the faded slide-in/out should
                // be displayed out of bounds.
                SizeTransform(clip = false)
            )
        }
    ) {
        Column {
            Text(
                text = stringResource(id = headers[it]),
                style = titleStyle ?: LocalTextStyle.current,
            )
            Text(
                text = when (it) {
                    5 -> {
                        stringResource(
                            id = dateRangeStringRes,
                            startDateString,
                            endDateString
                        )
                    }

                    else -> {
                        stringResource(
                            id = R.string.selected_count_label,
                            subHeaders[it],
                        )
                    }
                },
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.alpha(0.8f),
            )
        }
    }
}