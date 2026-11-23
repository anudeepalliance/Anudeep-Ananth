package com.anudeepananth.projects.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

/**
 * One bar in a [HorizontalBarComparison].
 *
 * @property valueLabel the number as it should be shown beside the bar
 * @property isHighlighted true for the winning bar, which is drawn in the accent colour
 */
data class BarComparisonItem(val label: String, val value: Double, val valueLabel: String, val isHighlighted: Boolean)

/** Compares a few numbers as horizontal bars; the longest bar fills the row and the others scale to it. */
@Composable
fun HorizontalBarComparison(items: List<BarComparisonItem>, modifier: Modifier = Modifier) {
    val largestValue = items.maxOfOrNull {
        it.value
    } ?: 0.0
    val isCompact = LocalWidthClass.current == WidthClass.COMPACT
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(spacingMedium)) {
        items.forEach { item ->
            val filledFraction = if (largestValue > 0.0) (item.value / largestValue).toFloat().coerceIn(0f, 1f) else 0f
            val labelColor =
                if (item.isHighlighted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            if (isCompact) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SubheadingText(item.label, modifier = Modifier.weight(1f), color = labelColor)
                        MonospaceText(item.valueLabel, color = labelColor)
                    }
                    AddSpace(spacingXSmall)
                    ComparisonBar(filledFraction, item.isHighlighted, Modifier.fillMaxWidth())
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SubheadingText(item.label, modifier = Modifier.width(barComparisonLabelWidth), color = labelColor)
                    ComparisonBar(filledFraction, item.isHighlighted, Modifier.weight(1f))
                    AddSpace(spacingMedium)
                    MonospaceText(
                        item.valueLabel,
                        modifier = Modifier.width(barComparisonValueWidth),
                        color = labelColor
                    )
                }
            }
        }
    }
}

/** A single track with the left [filledFraction] of it coloured in. */
@Composable
private fun ComparisonBar(filledFraction: Float, isHighlighted: Boolean, modifier: Modifier) {
    val barShape = RoundedCornerShape(cornerRadiusPill)
    val filledModifier = Modifier.fillMaxWidth(filledFraction).height(barComparisonBarHeight).clip(barShape)
    Box(
        modifier = modifier.height(barComparisonBarHeight).clip(barShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        if (isHighlighted) {
            Box(modifier = filledModifier.background(LocalSiteColours.current.progressGradientBrush()))
        } else {
            Box(modifier = filledModifier.background(LocalSiteColours.current.otherBar))
        }
    }
}
