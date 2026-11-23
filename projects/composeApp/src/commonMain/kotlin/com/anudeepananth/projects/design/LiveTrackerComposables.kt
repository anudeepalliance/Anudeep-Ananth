package com.anudeepananth.projects.design

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

private const val RING_ROTATION_DURATION_MILLISECONDS = 1100
private const val RING_SWEEP_DEGREES = 270f
private const val DASH_FLOW_DURATION_MILLISECONDS = 700
private const val FEED_ITEM_ENTER_DURATION_MILLISECONDS = 350

/** A ring that spins steadily, drawn as a blue-to-sky arc; it sits around the marker of the step running now. */
@Composable
fun SpinningGradientRing(modifier: Modifier = Modifier) {
    val siteColours = LocalSiteColours.current
    val rotationTransition = rememberInfiniteTransition()
    val rotationDegrees by rotationTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(RING_ROTATION_DURATION_MILLISECONDS, easing = LinearEasing)),
    )
    Canvas(modifier = modifier.rotate(rotationDegrees)) {
        val strokeWidth = spinningRingStrokeWidth.toPx()
        drawArc(
            brush = Brush.sweepGradient(
                listOf(
                    Color.Transparent,
                    siteColours.progressGradientStart,
                    siteColours.progressGradientEnd
                )
            ),
            startAngle = 0f,
            sweepAngle = RING_SWEEP_DEGREES,
            useCenter = false,
            topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
            size = androidx.compose.ui.geometry.Size(size.width - strokeWidth, size.height - strokeWidth),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
    }
}

/** A timeline connector whose dashes keep moving downwards, showing that work is flowing into the next step. */
@Composable
fun FlowingDashedConnector(modifier: Modifier = Modifier) {
    val siteColours = LocalSiteColours.current
    val flowTransition = rememberInfiniteTransition()
    val dashFlowFraction by flowTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(DASH_FLOW_DURATION_MILLISECONDS, easing = LinearEasing)),
    )
    Canvas(modifier = modifier) {
        val dashLength = flowingDashLength.toPx()
        drawLine(
            brush = Brush.verticalGradient(listOf(siteColours.progressGradientStart, siteColours.progressGradientEnd)),
            start = Offset(size.width / 2, 0f),
            end = Offset(size.width / 2, size.height),
            strokeWidth = size.width,
            pathEffect = PathEffect.dashPathEffect(
                floatArrayOf(dashLength, dashLength),
                phase = -dashFlowFraction * dashLength * 2
            ),
        )
    }
}

/** A small "Live" pill with a pulsing mint dot, shown while a run is in progress. */
@Composable
fun LivePill(modifier: Modifier = Modifier, label: String = "Live") {
    val siteColours = LocalSiteColours.current
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadiusPill))
            .background(siteColours.runningContainer)
            .padding(horizontal = paddingSmall, vertical = spacingTiny),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(pulsingDotSize), contentAlignment = Alignment.Center) {
            PulsingDot(siteColours.doneContent)
        }
        AddSpace(spacingXSmall)
        LabelText(label, color = siteColours.runningContent)
    }
}

/** What kind of line an [ActivityFeedLine] is: ordinary progress, the system retrying after a problem, or a switch to another model. */
enum class ActivityTone { PROGRESS, RETRYING, SWITCHING }

/**
 * One line in a live activity feed: a small coloured dot (amber for a retry, blue for a model switch,
 * grey otherwise) and the text. It fades and slides in the first time it appears; [isLatest] makes it ink instead of grey.
 */
@Composable
fun ActivityFeedLine(text: String, tone: ActivityTone, isLatest: Boolean, modifier: Modifier = Modifier) {
    val siteColours = LocalSiteColours.current
    val appearanceState = remember {
        MutableTransitionState(false).apply {
            targetState = true
        }
    }
    val dotColor = when (tone) {
        ActivityTone.PROGRESS -> MaterialTheme.colorScheme.outline
        ActivityTone.RETRYING -> siteColours.warningContent
        ActivityTone.SWITCHING -> MaterialTheme.colorScheme.secondary
    }
    val textColor = when {
        tone == ActivityTone.RETRYING -> siteColours.warningContent
        isLatest -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    AnimatedVisibility(
        visibleState = appearanceState,
        enter = fadeIn(tween(FEED_ITEM_ENTER_DURATION_MILLISECONDS)) + expandVertically(
            tween(
                FEED_ITEM_ENTER_DURATION_MILLISECONDS
            )
        ),
        modifier = modifier,
    ) {
        Row(modifier = Modifier.padding(top = spacingXSmall), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier.padding(top = paddingXSmallForFeedDot).size(feedIconSize).clip(CircleShape)
                    .background(dotColor)
            )
            AddSpace(spacingSmall)
            SecondaryText(text, color = textColor)
        }
    }
}
