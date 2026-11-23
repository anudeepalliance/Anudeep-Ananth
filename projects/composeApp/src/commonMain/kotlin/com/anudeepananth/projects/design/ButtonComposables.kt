package com.anudeepananth.projects.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import com.anudeepananth.projects.platform.copyTextToBrowserClipboard
import kotlinx.coroutines.delay

private const val COPIED_CONFIRMATION_VISIBLE_MILLISECONDS = 1800L

private val buttonShape = RoundedCornerShape(cornerRadiusButton)
private val defaultButtonContentPadding = PaddingValues(horizontal = paddingXXLarge, vertical = paddingSmall)
private val smallButtonContentPadding = PaddingValues(horizontal = paddingDefault, vertical = paddingTiny)

/** The label inside every button, so all buttons share one text style. */
@Composable
private fun ButtonLabel(text: String, color: Color = Color.Unspecified) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

/** Shows the mouse as a pointing hand over anything clickable, as web visitors expect. */
fun Modifier.showHandCursorOnHover(): Modifier = pointerHoverIcon(PointerIcon.Hand)

/** The main action on a screen: solid blue with a white label. Use at most one per view. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    isLoading: Boolean = false,
) {
    Button(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = buttonHeightDefault).showHandCursorOnHover(),
        enabled = isEnabled && !isLoading,
        shape = buttonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = if (isLoading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = if (isLoading) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        contentPadding = defaultButtonContentPadding,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(progressSpinnerSize),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = progressSpinnerStrokeWidth,
            )
            AddSpace(spacingSmall)
        }
        ButtonLabel(text)
    }
}

/** A supporting action: white with a thin ink border, quieter than [PrimaryButton]. */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    isSmall: Boolean = false,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = if (isSmall) buttonHeightSmall else buttonHeightDefault)
            .showHandCursorOnHover(),
        enabled = isEnabled,
        shape = buttonShape,
        border = BorderStroke(
            borderWidthThin,
            if (isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        contentPadding = if (isSmall) smallButtonContentPadding else defaultButtonContentPadding,
    ) {
        ButtonLabel(text)
    }
}

/** A link-like action with no border; also used for navigation links. */
@Composable
fun TextLinkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    isEnabled: Boolean = true,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = buttonHeightSmall).showHandCursorOnHover(),
        enabled = isEnabled,
        shape = buttonShape,
        colors = ButtonDefaults.textButtonColors(contentColor = color),
        contentPadding = smallButtonContentPadding,
    ) {
        ButtonLabel(text)
    }
}

/** A destructive action such as delete: filled with the coral error colour. */
@Composable
fun DangerButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, isEnabled: Boolean = true) {
    Button(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = buttonHeightDefault).showHandCursorOnHover(),
        enabled = isEnabled,
        shape = buttonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
        ),
        contentPadding = defaultButtonContentPadding,
    ) {
        ButtonLabel(text)
    }
}

/** A left-pointing arrow drawn with lines, because the bundled fonts have no arrow glyph. */
@Composable
fun LeftArrowIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(arrowIconSize)) {
        val strokeWidth = size.minDimension / 9f
        val middleY = size.height / 2f
        val leftX = size.width * 0.15f
        drawLine(color, Offset(leftX, middleY), Offset(size.width * 0.85f, middleY), strokeWidth, StrokeCap.Round)
        drawLine(
            color,
            Offset(leftX, middleY),
            Offset(size.width * 0.45f, size.height * 0.2f),
            strokeWidth,
            StrokeCap.Round
        )
        drawLine(
            color,
            Offset(leftX, middleY),
            Offset(size.width * 0.45f, size.height * 0.8f),
            strokeWidth,
            StrokeCap.Round
        )
    }
}

/** A right-pointing arrow drawn with lines, used at the end of "open" links. */
@Composable
fun RightArrowIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(arrowIconSize)) {
        val strokeWidth = size.minDimension / 9f
        val middleY = size.height / 2f
        val rightX = size.width * 0.85f
        drawLine(color, Offset(size.width * 0.15f, middleY), Offset(rightX, middleY), strokeWidth, StrokeCap.Round)
        drawLine(
            color,
            Offset(rightX, middleY),
            Offset(size.width * 0.55f, size.height * 0.2f),
            strokeWidth,
            StrokeCap.Round
        )
        drawLine(
            color,
            Offset(rightX, middleY),
            Offset(size.width * 0.55f, size.height * 0.8f),
            strokeWidth,
            StrokeCap.Round
        )
    }
}

/** A small "back" control for the top of the hero panel: a left arrow and a short label such as "All runs", in the link blue. */
@Composable
fun HeroBackLink(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val linkColor = MaterialTheme.colorScheme.primary
    DisableSelection {
        Row(
            modifier = modifier
                .clip(buttonShape)
                .clickable(onClickLabel = label, role = Role.Button, onClick = onClick)
                .showHandCursorOnHover()
                .heightIn(min = buttonHeightSmall)
                .padding(vertical = paddingSmall),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LeftArrowIcon(color = linkColor)
            AddSpace(spacingSmall)
            ButtonLabel(label, color = linkColor)
        }
    }
}

/** Copies [textToCopy] to the visitor's clipboard and briefly says "Copied" so they know it worked. */
@Composable
fun CopyToClipboardButton(
    textToCopy: String,
    modifier: Modifier = Modifier,
    label: String = "Copy",
) {
    var wasJustCopied by remember {
        mutableStateOf(false)
    }
    LaunchedEffect(wasJustCopied) {
        if (wasJustCopied) {
            delay(COPIED_CONFIRMATION_VISIBLE_MILLISECONDS)
            wasJustCopied = false
        }
    }
    SecondaryButton(
        text = if (wasJustCopied) "Copied" else label,
        onClick = {
            copyTextToBrowserClipboard(textToCopy)
            wasJustCopied = true
        },
        modifier = modifier,
        isSmall = true,
    )
}
