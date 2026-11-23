package com.anudeepananth.projects.design

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow

/**
 * A row of pill-shaped tabs; the selected one is solid blue. When the tabs do not fit
 * the window, the row scrolls sideways.
 */
@Composable
fun PillTabStrip(
    tabLabels: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val pillShape = RoundedCornerShape(cornerRadiusPill)
    val tabStripScrollState = rememberScrollState()
    val tabLeftEdgesInPixels = remember {
        mutableStateMapOf<Int, Int>()
    }
    val selectedTabLeftEdgeInPixels = tabLeftEdgesInPixels[selectedTabIndex]
    LaunchedEffect(selectedTabIndex, selectedTabLeftEdgeInPixels) {
        // On phones the selected tab can sit off screen to the right, so slide the strip to show it.
        if (selectedTabLeftEdgeInPixels != null) tabStripScrollState.animateScrollTo(selectedTabLeftEdgeInPixels)
    }
    DisableSelection {
        Row(
            modifier = modifier.fillMaxWidth().horizontalScroll(tabStripScrollState),
            horizontalArrangement = Arrangement.spacedBy(spacingSmall),
        ) {
            tabLabels.forEachIndexed { tabIndex, tabLabel ->
                val isSelected = tabIndex == selectedTabIndex
                val pillBackground = if (isSelected) {
                    Modifier.background(MaterialTheme.colorScheme.primary)
                } else {
                    Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .border(borderWidthThin, MaterialTheme.colorScheme.outline, pillShape)
                }
                Box(
                    modifier = Modifier
                        .onPlaced { tabLayoutCoordinates ->
                            tabLeftEdgesInPixels[tabIndex] = tabLayoutCoordinates.positionInParent().x.toInt()
                        }
                        .clip(pillShape)
                        .then(pillBackground)
                        .clickable(role = Role.Tab) {
                            onTabSelected(tabIndex)
                        }
                        .showHandCursorOnHover()
                        .heightIn(min = buttonHeightDefault)
                        .padding(horizontal = paddingLarge, vertical = paddingMedium),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = tabLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

/** A rubbish-bin outline drawn with lines, because no icon library is bundled. */
@Composable
private fun TrashCanIcon(color: Color) {
    Canvas(modifier = Modifier.size(iconSizeDefault)) {
        val strokeWidth = size.minDimension / 11f
        val lidY = size.height * 0.24f
        drawLine(
            color,
            Offset(size.width * 0.12f, lidY),
            Offset(size.width * 0.88f, lidY),
            strokeWidth,
            StrokeCap.Round
        )
        drawLine(
            color,
            Offset(size.width * 0.38f, size.height * 0.1f),
            Offset(size.width * 0.62f, size.height * 0.1f),
            strokeWidth,
            StrokeCap.Round
        )
        drawLine(
            color,
            Offset(size.width * 0.38f, size.height * 0.1f),
            Offset(size.width * 0.38f, lidY),
            strokeWidth,
            StrokeCap.Round
        )
        drawLine(
            color,
            Offset(size.width * 0.62f, size.height * 0.1f),
            Offset(size.width * 0.62f, lidY),
            strokeWidth,
            StrokeCap.Round
        )
        drawRoundRect(
            color = color,
            topLeft = Offset(size.width * 0.22f, lidY),
            size = Size(size.width * 0.56f, size.height * 0.66f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(strokeWidth * 1.5f),
            style = Stroke(width = strokeWidth),
        )
        drawLine(
            color,
            Offset(size.width * 0.42f, size.height * 0.42f),
            Offset(size.width * 0.42f, size.height * 0.74f),
            strokeWidth,
            StrokeCap.Round
        )
        drawLine(
            color,
            Offset(size.width * 0.58f, size.height * 0.42f),
            Offset(size.width * 0.58f, size.height * 0.74f),
            strokeWidth,
            StrokeCap.Round
        )
    }
}

/** A round delete button with a rubbish-bin icon and a 48dp hit area; clicks on it never reach the row behind it. */
@Composable
fun DeleteIconButton(accessibleLabel: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val siteColours = LocalSiteColours.current
    DisableSelection {
        Box(
            modifier = modifier
                .size(touchTargetMinimum)
                .clip(RoundedCornerShape(cornerRadiusPill))
                .clickable(role = Role.Button, onClick = onClick)
                .showHandCursorOnHover()
                .semantics {
                    contentDescription = accessibleLabel
                },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(touchTargetMinimum / 1.4f)
                    .clip(RoundedCornerShape(cornerRadiusPill))
                    .background(siteColours.failedContainer),
                contentAlignment = Alignment.Center,
            ) {
                TrashCanIcon(siteColours.failedContent)
            }
        }
    }
}

/**
 * A sheet that slides up from the bottom to confirm a destructive action, such as deleting a run.
 * It shows [errorMessage] when the action failed, and a spinner on the confirm button while [isConfirming].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestructiveConfirmationBottomSheet(
    title: String,
    subjectLine: String,
    warningText: String,
    confirmLabel: String,
    isConfirming: Boolean,
    errorMessage: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = {
            if (!isConfirming) onDismiss()
        },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = cornerRadiusLarge, topEnd = cornerRadiusLarge),
    ) {
        // The sheet lives in its own window layer, outside the page's text selection area; letting its text join
        // that area crashes Compose ("layouts are not part of the same hierarchy"), so selection is switched off here.
        DisableSelection {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = paddingXLarge).padding(bottom = paddingXXLarge)
            ) {
                TitleText(title, color = MaterialTheme.colorScheme.onSurface)
                AddSpace(spacingMedium)
                GradientRule()
                AddSpace(spacingDefault)
                SubheadingText(subjectLine)
                AddSpace(spacingSmall)
                BodyText(warningText, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (errorMessage != null) {
                    AddSpace(spacingDefault)
                    NoticeBanner(errorMessage, tone = NoticeTone.PROBLEM)
                }
                AddSpace(spacingXLarge)
                WrappingRow(horizontalSpacing = spacingMedium) {
                    Button(
                        onClick = onConfirm,
                        enabled = !isConfirming,
                        modifier = Modifier.heightIn(min = buttonHeightDefault).showHandCursorOnHover(),
                        shape = RoundedCornerShape(cornerRadiusButton),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                            disabledContainerColor = MaterialTheme.colorScheme.error,
                            disabledContentColor = MaterialTheme.colorScheme.onError,
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = paddingXXLarge,
                            vertical = paddingSmall
                        ),
                    ) {
                        if (isConfirming) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(progressSpinnerSize),
                                color = MaterialTheme.colorScheme.onError,
                                strokeWidth = progressSpinnerStrokeWidth,
                            )
                            AddSpace(spacingSmall)
                        }
                        Text(
                            text = confirmLabel,
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    SecondaryButton(text = "Cancel", onClick = onDismiss, isEnabled = !isConfirming)
                }
            }
        }
    }
}
