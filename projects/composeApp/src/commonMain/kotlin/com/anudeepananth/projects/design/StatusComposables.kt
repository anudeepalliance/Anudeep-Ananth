package com.anudeepananth.projects.design

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap

private const val PULSE_DURATION_MILLISECONDS = 900
private const val PULSE_SMALLEST_SCALE = 0.7f
private const val PULSE_LARGEST_SCALE = 1.25f
private const val PULSE_FAINTEST_ALPHA = 0.45f

/** The four states a run or a step can be in, as far as colours and markers are concerned. */
enum class ProgressStatus { PENDING, RUNNING, DONE, FAILED }

@Composable
private fun containerColourFor(status: ProgressStatus): Color = when (status) {
    ProgressStatus.PENDING -> LocalSiteColours.current.pendingContainer
    ProgressStatus.RUNNING -> LocalSiteColours.current.runningContainer
    ProgressStatus.DONE -> LocalSiteColours.current.doneContainer
    ProgressStatus.FAILED -> LocalSiteColours.current.failedContainer
}

@Composable
private fun contentColourFor(status: ProgressStatus): Color = when (status) {
    ProgressStatus.PENDING -> LocalSiteColours.current.pendingContent
    ProgressStatus.RUNNING -> LocalSiteColours.current.runningContent
    ProgressStatus.DONE -> LocalSiteColours.current.doneContent
    ProgressStatus.FAILED -> LocalSiteColours.current.failedContent
}

/** A dot that gently grows and fades in a loop, so a running step or run looks alive. */
@Composable
fun PulsingDot(color: Color, modifier: Modifier = Modifier) {
    val pulseTransition = rememberInfiniteTransition()
    val pulseScale by pulseTransition.animateFloat(
        initialValue = PULSE_SMALLEST_SCALE,
        targetValue = PULSE_LARGEST_SCALE,
        animationSpec = infiniteRepeatable(
            tween(PULSE_DURATION_MILLISECONDS, easing = LinearEasing),
            RepeatMode.Reverse
        ),
    )
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = PULSE_FAINTEST_ALPHA,
        animationSpec = infiniteRepeatable(
            tween(PULSE_DURATION_MILLISECONDS, easing = LinearEasing),
            RepeatMode.Reverse
        ),
    )
    Box(
        modifier = modifier
            .size(pulsingDotSize)
            .scale(pulseScale)
            .alpha(pulseAlpha)
            .clip(CircleShape)
            .background(color),
    )
}

/** A small rounded pill with a coloured dot that says where a run or step stands, e.g. "Running". The dot pulses while running. */
@Composable
fun StatusChip(status: ProgressStatus, label: String, modifier: Modifier = Modifier) {
    val contentColor = contentColourFor(status)
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadiusPill))
            .background(containerColourFor(status))
            .padding(horizontal = paddingMedium, vertical = paddingTiny),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (status == ProgressStatus.RUNNING) {
            PulsingDot(LocalSiteColours.current.livePulse)
        } else {
            Box(modifier = Modifier.size(statusDotSize).clip(CircleShape).background(contentColor))
        }
        AddSpace(spacingSmall)
        LabelText(label, color = contentColor)
    }
}

/** What a [TagChip] marks: an AI step, a code step, a highlight, a quiet label, or a warning. */
enum class TagTone { AI, CODE, ACCENT, NEUTRAL, WARNING }

/** A small rounded tag for a category or badge, such as "AI", "Code" or "Featured". */
@Composable
fun TagChip(text: String, modifier: Modifier = Modifier, tone: TagTone = TagTone.NEUTRAL) {
    val siteColours = LocalSiteColours.current
    val isAccent = tone == TagTone.ACCENT
    val backgroundModifier = when (tone) {
        TagTone.AI -> Modifier.background(siteColours.aiTagContainer)
        TagTone.CODE -> Modifier.background(siteColours.codeTagContainer)
            .border(borderWidthThin, siteColours.codeTagBorder, RoundedCornerShape(cornerRadiusPill))

        TagTone.ACCENT -> Modifier.background(MaterialTheme.colorScheme.primary)
        TagTone.NEUTRAL -> Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
        TagTone.WARNING -> Modifier.background(siteColours.warningContainer)
    }
    val contentColor = when (tone) {
        TagTone.AI -> siteColours.aiTagContent
        TagTone.CODE -> siteColours.codeTagContent
        TagTone.ACCENT -> MaterialTheme.colorScheme.onPrimary
        TagTone.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant
        TagTone.WARNING -> siteColours.warningContent
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadiusPill))
            .then(backgroundModifier)
            .padding(horizontal = if (isAccent) paddingMedium else paddingSmall, vertical = spacingTiny),
    ) {
        LabelText(text, color = contentColor)
    }
}

/**
 * A thin bar split into one segment per pipeline step: finished steps fill with the blue-to-sky gradient,
 * the running step pulses, failed steps turn coral, and pending steps stay pale blue.
 */
@Composable
fun StepProgressBar(stepStatuses: List<ProgressStatus>, modifier: Modifier = Modifier) {
    val siteColours = LocalSiteColours.current
    val pendingSegmentColor = siteColours.heroDecoration
    val segmentShape = RoundedCornerShape(cornerRadiusPill)
    Row(
        modifier = modifier.fillMaxWidth().height(stepProgressBarHeight),
        horizontalArrangement = Arrangement.spacedBy(stepProgressSegmentGap)
    ) {
        stepStatuses.forEach { stepStatus ->
            val segmentModifier = Modifier.weight(1f).fillMaxHeight().clip(segmentShape)
            when (stepStatus) {
                ProgressStatus.DONE -> Box(segmentModifier.background(siteColours.progressGradientBrush()))
                ProgressStatus.FAILED -> Box(segmentModifier.background(MaterialTheme.colorScheme.error))
                ProgressStatus.PENDING -> Box(segmentModifier.background(pendingSegmentColor))
                ProgressStatus.RUNNING -> Box(segmentModifier.background(pendingSegmentColor)) {
                    PulsingSegmentFill(siteColours.progressGradientStart)
                }
            }
        }
    }
}

/** Fills its parent with [color] at a slowly breathing opacity. */
@Composable
private fun PulsingSegmentFill(color: Color) {
    val pulseTransition = rememberInfiniteTransition()
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = PULSE_FAINTEST_ALPHA,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(PULSE_DURATION_MILLISECONDS, easing = LinearEasing),
            RepeatMode.Reverse
        ),
    )
    Box(modifier = Modifier.fillMaxWidth().fillMaxHeight().alpha(pulseAlpha).background(color))
}

/** The round marker at the left of a timeline step: hollow, a spinning ring with a pulsing dot, ticked or crossed. */
@Composable
private fun TimelineMarker(status: ProgressStatus) {
    val siteColours = LocalSiteColours.current
    Box(modifier = Modifier.size(timelineMarkerSize), contentAlignment = Alignment.Center) {
        when (status) {
            ProgressStatus.PENDING -> Box(
                modifier = Modifier
                    .size(timelineMarkerSize)
                    .border(borderWidthSelected, MaterialTheme.colorScheme.outline, CircleShape),
            )

            ProgressStatus.RUNNING -> Box(
                modifier = Modifier
                    .size(timelineMarkerSize)
                    .clip(CircleShape)
                    .background(siteColours.runningContainer),
                contentAlignment = Alignment.Center,
            ) {
                SpinningGradientRing(modifier = Modifier.size(timelineMarkerSize))
                PulsingDot(siteColours.livePulse)
            }

            ProgressStatus.DONE -> {
                val tickColor = MaterialTheme.colorScheme.onPrimary
                Canvas(
                    modifier = Modifier.size(timelineMarkerSize).clip(CircleShape)
                        .background(siteColours.progressGradientBrush())
                ) {
                    val strokeWidth = size.minDimension / 9f
                    drawLine(
                        tickColor,
                        Offset(size.width * 0.28f, size.height * 0.52f),
                        Offset(size.width * 0.44f, size.height * 0.68f),
                        strokeWidth,
                        StrokeCap.Round
                    )
                    drawLine(
                        tickColor,
                        Offset(size.width * 0.44f, size.height * 0.68f),
                        Offset(size.width * 0.73f, size.height * 0.35f),
                        strokeWidth,
                        StrokeCap.Round
                    )
                }
            }

            ProgressStatus.FAILED -> {
                val crossColor = MaterialTheme.colorScheme.onError
                Canvas(
                    modifier = Modifier.size(timelineMarkerSize).clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error)
                ) {
                    val strokeWidth = size.minDimension / 9f
                    drawLine(
                        crossColor,
                        Offset(size.width * 0.33f, size.height * 0.33f),
                        Offset(size.width * 0.67f, size.height * 0.67f),
                        strokeWidth,
                        StrokeCap.Round
                    )
                    drawLine(
                        crossColor,
                        Offset(size.width * 0.67f, size.height * 0.33f),
                        Offset(size.width * 0.33f, size.height * 0.67f),
                        strokeWidth,
                        StrokeCap.Round
                    )
                }
            }
        }
    }
}

/**
 * One step in a vertical timeline: a status marker on the left joined to the next step by a line,
 * and the step's own [content] on the right. Set [isLastStep] so the final step draws no line below it.
 * Set [isHighlighted] for the step running now: the row gets a pale blue background and a blue left edge.
 * Set [isConnectorFlowing] when the next step is the one running now: its line shows dashes moving down.
 */
@Composable
fun TimelineStep(
    status: ProgressStatus,
    isLastStep: Boolean,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false,
    isConnectorFlowing: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val siteColours = LocalSiteColours.current
    val highlightAccentColor = MaterialTheme.colorScheme.primary
    val highlightModifier = if (isHighlighted) {
        Modifier
            .clip(RoundedCornerShape(cornerRadiusMedium))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .drawBehind {
                drawRect(color = highlightAccentColor, size = Size(liveStepAccentWidth.toPx(), size.height))
            }
            .padding(start = paddingMedium, end = paddingMedium, top = paddingMedium)
    } else {
        Modifier
    }
    Row(modifier = modifier.fillMaxWidth().then(highlightModifier).height(IntrinsicSize.Min)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            TimelineMarker(status)
            if (!isLastStep) {
                val connectorModifier = Modifier
                    .padding(vertical = spacingXSmall)
                    .width(timelineConnectorWidth)
                    .weight(1f)
                    .heightIn(min = timelineConnectorMinimumHeight)
                    .clip(RoundedCornerShape(cornerRadiusPill))
                when {
                    isConnectorFlowing -> FlowingDashedConnector(connectorModifier)
                    status == ProgressStatus.DONE -> Box(modifier = connectorModifier.background(siteColours.progressGradientBrush()))
                    else -> Box(modifier = connectorModifier.background(MaterialTheme.colorScheme.outline))
                }
            }
        }
        AddSpace(spacingMedium)
        Column(
            modifier = Modifier.weight(1f)
                .padding(bottom = if (isLastStep && !isHighlighted) spacingNone else spacingLarge), content = content
        )
    }
}
