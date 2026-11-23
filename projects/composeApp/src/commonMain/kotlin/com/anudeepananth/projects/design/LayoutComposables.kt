package com.anudeepananth.projects.design

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp

/** How wide the browser window is, in three bands that decide which layout a screen uses. */
enum class WidthClass { COMPACT, MEDIUM, EXPANDED }

/** Picks the width band for a window [availableWidth] wide: under 600dp, 600-840dp, or wider. */
fun widthClassForAvailableWidth(availableWidth: Dp): WidthClass = when {
    availableWidth < widthClassCompactUpperBound -> WidthClass.COMPACT
    availableWidth < widthClassMediumUpperBound -> WidthClass.MEDIUM
    else -> WidthClass.EXPANDED
}

/** The current window's width band; the page scaffold sets it, screens read `LocalWidthClass.current`. */
val LocalWidthClass = compositionLocalOf {
    WidthClass.EXPANDED
}

/** Empty vertical gap of [size] between two items in a column. */
@Composable
fun ColumnScope.AddSpace(size: Dp) {
    Spacer(modifier = Modifier.height(size))
}

/** Empty horizontal gap of [size] between two items in a row. */
@Composable
fun RowScope.AddSpace(size: Dp) {
    Spacer(modifier = Modifier.width(size))
}

/** A thin line that separates two blocks of content. */
@Composable
fun ThinDivider(modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.outline) {
    HorizontalDivider(modifier = modifier, thickness = borderWidthThin, color = color)
}

/** A short blue-to-sky line used as an accent under section titles. */
@Composable
fun GradientRule(modifier: Modifier = Modifier, width: Dp = accentRuleWidth) {
    Box(
        modifier = modifier
            .width(width)
            .height(gradientRuleHeight)
            .clip(RoundedCornerShape(cornerRadiusPill))
            .background(LocalSiteColours.current.progressGradientBrush()),
    )
}

/** Inner padding for cards and expandable sections: tighter on phones so the content keeps its room. */
@Composable
fun defaultContainerPadding(): Dp = if (LocalWidthClass.current == WidthClass.COMPACT) paddingLarge else paddingXLarge

/** A white card with a thin lavender border and a very soft shadow that groups related content; becomes clickable when [onClick] is given. */
@Composable
fun ContentCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    contentPadding: Dp = defaultContainerPadding(),
    content: @Composable ColumnScope.() -> Unit,
) {
    val cardShape = RoundedCornerShape(cornerRadiusLarge)
    val siteColours = LocalSiteColours.current
    val clickableModifier =
        if (onClick == null) Modifier else Modifier.clickable(onClick = onClick).showHandCursorOnHover()
    Column(
        modifier = modifier
            .shadow(
                cardShadowElevation,
                cardShape,
                ambientColor = siteColours.cardShadow,
                spotColor = siteColours.cardShadow
            )
            .clip(cardShape)
            .background(containerColor)
            .border(borderWidthThin, borderColor, cardShape)
            .then(clickableModifier)
            .padding(contentPadding),
        content = content,
    )
}

/** The strong blue card, used for at most one piece of content per page, such as the finished-run summary. Text on it uses `onEmphasisBlock`. */
@Composable
fun EmphasisCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val siteColours = LocalSiteColours.current
    val cardShape = RoundedCornerShape(cornerRadiusLarge)
    Column(
        modifier = modifier
            .shadow(
                cardShadowElevation,
                cardShape,
                ambientColor = siteColours.cardShadow,
                spotColor = siteColours.cardShadow
            )
            .clip(cardShape)
            .background(siteColours.emphasisBlockBrush())
            .padding(defaultContainerPadding()),
        content = content,
    )
}

/** The heading of a page section: an optional small label, the title, a short gradient rule and an optional explanation. */
@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier, eyebrow: String? = null, description: String? = null) {
    Column(modifier = modifier.widthIn(max = readableTextMaximumWidth)) {
        if (eyebrow != null) {
            EyebrowText(eyebrow)
            AddSpace(spacingSmall)
        }
        TitleText(title)
        AddSpace(spacingMedium)
        GradientRule()
        if (description != null) {
            AddSpace(spacingDefault)
            BodyText(description, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** How a [NoticeBanner] should feel: a helpful tip, something happening now, or a problem. */
enum class NoticeTone { TIP, IN_PROGRESS, PROBLEM }

/** A tinted strip with a short message and an optional action, used for tips, live notices and errors. */
@Composable
fun NoticeBanner(
    text: String,
    modifier: Modifier = Modifier,
    tone: NoticeTone = NoticeTone.TIP,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val siteColours = LocalSiteColours.current
    val containerColor = when (tone) {
        NoticeTone.TIP -> siteColours.doneContainer
        NoticeTone.IN_PROGRESS -> siteColours.runningContainer
        NoticeTone.PROBLEM -> siteColours.failedContainer
    }
    val contentColor = when (tone) {
        NoticeTone.TIP -> siteColours.doneContent
        NoticeTone.IN_PROGRESS -> siteColours.runningContent
        NoticeTone.PROBLEM -> siteColours.failedContent
    }
    val isCompact = LocalWidthClass.current == WidthClass.COMPACT
    val bannerModifier = modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(cornerRadiusMedium))
        .background(containerColor)
        .padding(horizontal = paddingLarge, vertical = paddingMedium)
    if (isCompact) {
        Column(modifier = bannerModifier) {
            BodyText(text, color = contentColor)
            if (actionLabel != null && onAction != null) {
                TextLinkButton(actionLabel, onAction, color = contentColor)
            }
        }
    } else {
        Row(modifier = bannerModifier, verticalAlignment = Alignment.CenterVertically) {
            BodyText(text, modifier = Modifier.weight(1f), color = contentColor)
            if (actionLabel != null && onAction != null) {
                AddSpace(spacingDefault)
                TextLinkButton(actionLabel, onAction, color = contentColor)
            }
        }
    }
}

/** A small arrow head that points down when [isPointingUp] is false and up when it is true. */
@Composable
private fun ChevronMark(isPointingUp: Boolean, color: Color) {
    Canvas(modifier = Modifier.size(iconSizeSmall)) {
        val strokeWidth = size.minDimension / 8f
        val leftX = size.width * 0.2f
        val rightX = size.width * 0.8f
        val middleX = size.width * 0.5f
        val outerY = if (isPointingUp) size.height * 0.65f else size.height * 0.35f
        val tipY = if (isPointingUp) size.height * 0.35f else size.height * 0.65f
        drawLine(color, Offset(leftX, outerY), Offset(middleX, tipY), strokeWidth, StrokeCap.Round)
        drawLine(color, Offset(middleX, tipY), Offset(rightX, outerY), strokeWidth, StrokeCap.Round)
    }
}

/** A titled block whose content is hidden until the visitor clicks the title; clicking again hides it. */
@Composable
fun ExpandableSection(
    title: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    isInitiallyExpanded: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    var isExpanded by rememberSaveable {
        mutableStateOf(isInitiallyExpanded)
    }
    val sectionShape = RoundedCornerShape(cornerRadiusMedium)
    val borderColor = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(sectionShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(borderWidthThin, borderColor, sectionShape),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    isExpanded = !isExpanded
                }
                .showHandCursorOnHover()
                .heightIn(min = touchTargetMinimum)
                .padding(horizontal = defaultContainerPadding(), vertical = paddingMedium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                SubheadingText(title)
                if (supportingText != null) {
                    SecondaryText(supportingText)
                }
            }
            AddSpace(spacingMedium)
            Box(
                modifier = Modifier
                    .size(touchTargetMinimum / 1.5f)
                    .clip(RoundedCornerShape(cornerRadiusPill))
                    .background(if (isExpanded) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                ChevronMark(
                    isPointingUp = isExpanded,
                    color = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        AnimatedVisibility(visible = isExpanded) {
            Column(modifier = Modifier.fillMaxWidth()) {
                ThinDivider()
                Column(modifier = Modifier.fillMaxWidth().padding(defaultContainerPadding()), content = content)
            }
        }
    }
}

/** A light code box for fixed-width text such as prompts; long text scrolls inside the box instead of stretching the page. */
@Composable
fun MonospaceBlock(text: String, modifier: Modifier = Modifier) {
    val codeBoxShape = RoundedCornerShape(cornerRadiusMedium)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(codeBoxShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(borderWidthThin, MaterialTheme.colorScheme.outlineVariant, codeBoxShape)
            .heightIn(max = monospaceBlockMaximumHeight)
            .verticalScroll(rememberScrollState())
            .padding(paddingDefault),
    ) {
        MonospaceText(text, color = MaterialTheme.colorScheme.onSurface)
    }
}

/** A softly tinted box that sets a quote or a callout apart from the surrounding text. */
@Composable
fun TintedBlock(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cornerRadiusMedium))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(paddingDefault),
        content = content,
    )
}

/** A small spinner with a line of text, shown while something is being fetched. */
@Composable
fun LoadingIndicator(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Row(modifier = modifier.padding(vertical = paddingXLarge), verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator(
            modifier = Modifier.size(progressSpinnerSize),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = progressSpinnerStrokeWidth,
        )
        AddSpace(spacingMedium)
        BodyText(text, color = color)
    }
}

/** A message for a whole page or card: nothing here yet, not found, or something failed. */
@Composable
fun MessageWithAction(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    ContentCard(modifier = modifier.fillMaxWidth()) {
        HeadingText(title)
        AddSpace(spacingSmall)
        BodyText(
            body,
            modifier = Modifier.widthIn(max = readableTextMaximumWidth),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (actionLabel != null && onAction != null) {
            AddSpace(spacingXLarge)
            PrimaryButton(actionLabel, onAction)
        }
    }
}

/** A small label above a value, used in rows of run facts such as "Model" or "Time taken". Set [isOnEmphasisBlock] on the blue card. */
@Composable
fun LabelledValue(label: String, value: String, modifier: Modifier = Modifier, isOnEmphasisBlock: Boolean = false) {
    val siteColours = LocalSiteColours.current
    Column(modifier = modifier) {
        LabelText(
            label,
            color = if (isOnEmphasisBlock) siteColours.onEmphasisBlockMuted else MaterialTheme.colorScheme.onSurfaceVariant
        )
        AddSpace(spacingTiny)
        SubheadingText(
            value,
            color = if (isOnEmphasisBlock) siteColours.onEmphasisBlock else MaterialTheme.colorScheme.onSurface
        )
    }
}

/** A big number with a small label under it, such as "4m 12s" over "Elapsed". Set [isOnEmphasisBlock] on the blue card. */
@Composable
fun StatTile(value: String, label: String, modifier: Modifier = Modifier, isOnEmphasisBlock: Boolean = false) {
    val siteColours = LocalSiteColours.current
    Column(modifier = modifier.widthIn(min = statTileMinimumWidth)) {
        StatNumberText(
            value,
            color = if (isOnEmphasisBlock) siteColours.onEmphasisBlock else MaterialTheme.colorScheme.onSurface
        )
        AddSpace(spacingXSmall)
        LabelText(
            label,
            color = if (isOnEmphasisBlock) siteColours.onEmphasisBlockMuted else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** What colour the dot in a [StatPillChip] is: blue for neutral facts, mint for good news. */
enum class StatPillTone { BLUE, MINT }

/** A floating white pill with a coloured dot, a bold number and a short label, such as "13 pipeline steps". */
@Composable
fun StatPillChip(value: String, label: String, modifier: Modifier = Modifier, tone: StatPillTone = StatPillTone.BLUE) {
    val siteColours = LocalSiteColours.current
    val pillShape = RoundedCornerShape(cornerRadiusPill)
    Row(
        modifier = modifier
            .shadow(
                cardShadowElevation,
                pillShape,
                ambientColor = siteColours.cardShadow,
                spotColor = siteColours.cardShadow
            )
            .clip(pillShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(borderWidthThin, MaterialTheme.colorScheme.outlineVariant, pillShape)
            .padding(horizontal = paddingDefault, vertical = paddingSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(statPillDotSize)
                .clip(RoundedCornerShape(cornerRadiusPill))
                .background(if (tone == StatPillTone.MINT) siteColours.doneContent else MaterialTheme.colorScheme.secondary),
        )
        AddSpace(spacingSmall)
        SubheadingText(value)
        AddSpace(spacingXSmall)
        LabelText(label)
    }
}

/** Lays out several small items in a row that wraps onto new lines when the window is narrow. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WrappingRow(
    modifier: Modifier = Modifier,
    horizontalSpacing: Dp = spacingXLarge,
    verticalSpacing: Dp = spacingMedium,
    content: @Composable () -> Unit,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        content()
    }
}
