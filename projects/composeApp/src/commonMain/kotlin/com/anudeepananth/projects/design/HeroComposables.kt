package com.anudeepananth.projects.design

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity

/**
 * The soft pastel panel at the top of every page, inset from the window edges with rounded corners.
 * It holds an optional [topContent] such as a back link, a small [eyebrow], the large [title] (its first
 * [gradientWordCount] words in the blue gradient), an optional [lead] paragraph, and [bottomContent] for
 * stat pills or actions. A faint dashed curve decorates the right-hand side.
 */
@Composable
fun HeroBand(
    title: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    lead: String? = null,
    gradientWordCount: Int = 0,
    topContent: (@Composable ColumnScope.() -> Unit)? = null,
    bottomContent: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val siteColours = LocalSiteColours.current
    val widthClass = LocalWidthClass.current
    val isCompact = widthClass == WidthClass.COMPACT
    val panelShape = RoundedCornerShape(cornerRadiusHeroPanel)
    Box(modifier = modifier.fillMaxWidth().padding(top = heroPanelTopMargin), contentAlignment = Alignment.TopCenter) {
        Box(
            modifier = Modifier
                .widthIn(max = heroPanelMaximumWidth)
                .fillMaxWidth()
                .padding(horizontal = pageHorizontalPaddingFor(widthClass))
                .clip(panelShape)
                .background(siteColours.heroPanelBrush())
                .border(borderWidthThin, MaterialTheme.colorScheme.outlineVariant, panelShape),
        ) {
            HeroDecoration(modifier = Modifier.matchParentSize())
            Column(
                modifier = Modifier.fillMaxWidth()
                    .padding(if (isCompact) heroPanelInnerPaddingCompact else heroPanelInnerPaddingExpanded)
            ) {
                if (topContent != null) {
                    topContent()
                    AddSpace(if (isCompact) spacingDefault else spacingXLarge)
                }
                if (eyebrow != null) {
                    EyebrowText(eyebrow, color = MaterialTheme.colorScheme.secondary)
                    AddSpace(spacingMedium)
                }
                HeroTitleText(title, gradientWordCount = gradientWordCount)
                if (lead != null) {
                    AddSpace(spacingXLarge)
                    LeadBodyText(
                        lead,
                        modifier = Modifier.widthIn(max = heroLeadMaximumWidth),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (bottomContent != null) {
                    AddSpace(if (isCompact) spacingXLarge else spacingXXLarge)
                    bottomContent()
                }
            }
        }
    }
}

/** A faint dashed curve and two soft circles drawn on the right of the hero panel, in pale blue. */
@Composable
private fun HeroDecoration(modifier: Modifier) {
    val siteColours = LocalSiteColours.current
    val strokeWidthInPixels = with(LocalDensity.current) {
        heroDecorationStrokeWidth.toPx()
    }
    val dashLengthInPixels = with(LocalDensity.current) {
        heroDecorationDashLength.toPx()
    }
    Canvas(modifier = modifier.clipToBounds()) {
        val decorativeCurve = Path().apply {
            moveTo(size.width * 0.55f, size.height * 1.05f)
            cubicTo(
                size.width * 0.7f, size.height * 0.55f,
                size.width * 0.8f, size.height * 0.95f,
                size.width * 1.02f, size.height * 0.15f,
            )
        }
        drawPath(
            path = decorativeCurve,
            color = siteColours.heroDecoration,
            style = Stroke(
                width = strokeWidthInPixels,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLengthInPixels, dashLengthInPixels))
            ),
        )
        drawCircle(
            color = siteColours.heroDecoration,
            radius = size.minDimension * 0.22f,
            center = Offset(size.width * 0.95f, size.height * 0.05f)
        )
        drawCircle(
            color = siteColours.heroPanelTop,
            radius = size.minDimension * 0.08f,
            center = Offset(size.width * 0.78f, size.height * 0.78f)
        )
    }
}

/**
 * A full-width row that centres its content in a column no wider than the page's maximum width,
 * with the page's side padding. Use it for everything below the hero band.
 */
@Composable
fun PageWidthColumn(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = modifier
                .widthIn(max = pageMaximumContentWidth)
                .fillMaxWidth()
                .padding(horizontal = pageHorizontalPaddingFor(LocalWidthClass.current)),
            content = content,
        )
    }
}

/** The light body of a page below the hero band, centred and padded, with room above and below. */
@Composable
fun PageBody(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val isCompact = LocalWidthClass.current == WidthClass.COMPACT
    PageWidthColumn(
        modifier = modifier.padding(top = if (isCompact) spacingXXLarge else spacingSection),
        content = content
    )
}
