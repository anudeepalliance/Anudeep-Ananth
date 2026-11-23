package com.anudeepananth.projects.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

/** The one place the raw `Text` composable is used; every other text composable goes through here. */
@Composable
private fun StyledText(
    text: String,
    style: TextStyle,
    modifier: Modifier,
    color: Color,
    textAlign: TextAlign?,
    maximumLines: Int,
) {
    Text(
        text = text,
        style = style,
        modifier = modifier,
        color = color,
        textAlign = textAlign,
        maxLines = maximumLines,
        overflow = TextOverflow.Ellipsis,
    )
}

/**
 * The large headline in a page's hero panel; one per page. The first [gradientWordCount] words are
 * filled with the blue gradient and the rest stay in ink. It shrinks on narrower windows.
 */
@Composable
fun HeroTitleText(text: String, modifier: Modifier = Modifier, gradientWordCount: Int = 0) {
    val heroTitleStyle = when (LocalWidthClass.current) {
        WidthClass.COMPACT -> MaterialTheme.typography.displayLarge.copy(
            fontSize = heroTitleFontSizeCompact,
            lineHeight = heroTitleLineHeightCompact
        )

        WidthClass.MEDIUM -> MaterialTheme.typography.displayLarge.copy(
            fontSize = heroTitleFontSizeMedium,
            lineHeight = heroTitleLineHeightMedium
        )

        WidthClass.EXPANDED -> MaterialTheme.typography.displayLarge.copy(
            fontSize = heroTitleFontSizeExpanded,
            lineHeight = heroTitleLineHeightExpanded
        )
    }
    val titleWords = text.split(' ')
    val gradientPart = titleWords.take(gradientWordCount).joinToString(" ")
    val inkPart = titleWords.drop(gradientWordCount).joinToString(" ")
    val headlineGradientBrush = LocalSiteColours.current.headlineGradientBrush()
    val titleWithGradientWords = buildAnnotatedString {
        if (gradientPart.isNotEmpty()) {
            withStyle(SpanStyle(brush = headlineGradientBrush)) {
                append(gradientPart)
            }
            if (inkPart.isNotEmpty()) append(" ")
        }
        append(inkPart)
    }
    Text(
        text = titleWithGradientWords,
        style = heroTitleStyle,
        modifier = modifier,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

/** A big display number, such as elapsed time or a model call count, shown above a [LabelText]. */
@Composable
fun StatNumberText(text: String, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.onSurface) {
    val isCompact = LocalWidthClass.current == WidthClass.COMPACT
    val statStyle = MaterialTheme.typography.displaySmall.copy(
        fontSize = if (isCompact) statNumberFontSizeCompact else statNumberFontSize,
        lineHeight = if (isCompact) statNumberFontSizeCompact else statNumberLineHeight,
    )
    StyledText(text, statStyle, modifier, color, null, 1)
}

/** Names a major section of a page, such as "Part 1". */
@Composable
fun TitleText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onBackground,
) {
    val isCompact = LocalWidthClass.current == WidthClass.COMPACT
    val style = if (isCompact) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineLarge
    StyledText(text, style, modifier, color, null, Int.MAX_VALUE)
}

/** Names a card or a sub-section inside a major section. */
@Composable
fun HeadingText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    maximumLines: Int = Int.MAX_VALUE,
) {
    StyledText(text, MaterialTheme.typography.titleLarge, modifier, color, null, maximumLines)
}

/** A smaller heading for rows and compact cards. */
@Composable
fun SubheadingText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    maximumLines: Int = Int.MAX_VALUE,
) {
    StyledText(text, MaterialTheme.typography.titleSmall, modifier, color, null, maximumLines)
}

/** Introductory paragraph text, slightly larger than normal body text. */
@Composable
fun LeadBodyText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    StyledText(text, MaterialTheme.typography.bodyLarge, modifier, color, null, Int.MAX_VALUE)
}

/** Normal paragraph text; keeps any line breaks that are in [text]. */
@Composable
fun BodyText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    textAlign: TextAlign? = null,
    maximumLines: Int = Int.MAX_VALUE,
) {
    StyledText(text, MaterialTheme.typography.bodyMedium, modifier, color, textAlign, maximumLines)
}

/** Quieter, smaller text for captions and supporting details. */
@Composable
fun SecondaryText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    maximumLines: Int = Int.MAX_VALUE,
) {
    StyledText(text, MaterialTheme.typography.bodySmall, modifier, color, null, maximumLines)
}

/** Short text that names a value, a chip or a table column. */
@Composable
fun LabelText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    maximumLines: Int = 1,
) {
    StyledText(text, MaterialTheme.typography.labelMedium, modifier, color, null, maximumLines)
}

/** Tiny capital-letter label that sits above a heading to say what kind of thing follows. */
@Composable
fun EyebrowText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    maximumLines: Int = 1,
) {
    StyledText(text.uppercase(), MaterialTheme.typography.labelSmall, modifier, color, null, maximumLines)
}

/** Fixed-width text for prompts, model replies, ids and formulas. */
@Composable
fun MonospaceText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    maximumLines: Int = Int.MAX_VALUE,
) {
    StyledText(text, monospaceTextStyle(), modifier, color, null, maximumLines)
}

/** Text for a message that says something went wrong. */
@Composable
fun ErrorText(text: String, modifier: Modifier = Modifier) {
    StyledText(
        text,
        MaterialTheme.typography.bodyMedium,
        modifier,
        MaterialTheme.colorScheme.error,
        null,
        Int.MAX_VALUE
    )
}
