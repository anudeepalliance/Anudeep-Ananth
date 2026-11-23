package com.anudeepananth.projects.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * One column of a [DataTable].
 *
 * @property header the column's name, shown in the top row
 * @property width the narrowest the column may be; columns grow to share any spare room
 * @property cellContent draws this column's cell for one row
 */
class DataTableColumn<RowItem>(
    val header: String,
    val width: Dp,
    val cellContent: @Composable (RowItem) -> Unit,
)

/**
 * A bordered table. When the window is narrower than the columns need, the table scrolls sideways
 * instead of squashing; when there is spare room, the columns stretch to fill it.
 */
@Composable
fun <RowItem> DataTable(
    columns: List<DataTableColumn<RowItem>>,
    rows: List<RowItem>,
    modifier: Modifier = Modifier,
    onRowClick: ((RowItem) -> Unit)? = null,
) {
    val tableShape = RoundedCornerShape(cornerRadiusLarge)
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                cardShadowElevation,
                tableShape,
                ambientColor = LocalSiteColours.current.cardShadow,
                spotColor = LocalSiteColours.current.cardShadow
            )
            .clip(tableShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(borderWidthThin, MaterialTheme.colorScheme.outlineVariant, tableShape),
    ) {
        val minimumTableWidth = columns.fold(0.dp) { widthSoFar, column ->
            widthSoFar + column.width
        }
        val stretchFactor = if (maxWidth > minimumTableWidth) maxWidth / minimumTableWidth else 1f
        Column(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            Row(modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
                columns.forEach { column ->
                    Box(
                        modifier = Modifier
                            .width(column.width * stretchFactor)
                            .padding(horizontal = tableCellHorizontalPadding, vertical = tableCellVerticalPadding),
                    ) {
                        EyebrowText(column.header, color = MaterialTheme.colorScheme.onSurfaceVariant, maximumLines = 3)
                    }
                }
            }
            rows.forEach { rowItem ->
                ThinDivider(
                    modifier = Modifier.width(minimumTableWidth * stretchFactor),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                val rowClickModifier = if (onRowClick == null) {
                    Modifier
                } else {
                    Modifier
                        .clickable {
                            onRowClick(rowItem)
                        }
                        .showHandCursorOnHover()
                }
                Row(modifier = rowClickModifier, verticalAlignment = Alignment.CenterVertically) {
                    columns.forEach { column ->
                        Box(
                            modifier = Modifier
                                .width(column.width * stretchFactor)
                                .padding(horizontal = tableCellHorizontalPadding, vertical = tableCellVerticalPadding),
                        ) {
                            column.cellContent(rowItem)
                        }
                    }
                }
            }
        }
    }
}
