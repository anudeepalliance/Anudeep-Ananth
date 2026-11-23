package com.anudeepananth.projects.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.anudeepananth.projects.resources.Res
import com.anudeepananth.projects.resources.jetbrainsmono_variable
import com.anudeepananth.projects.resources.manrope_variable
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.FontResource

// Ink and neutrals. Secondary text is the same ink at lower opacity.
private val ink = Color(0xFF01001A)
private val inkAtSeventyPercent = Color(0xB301001A)
private val inkAtSixtyPercent = Color(0x9901001A)
private val paperWhite = Color(0xFFFFFFFF)
private val lavenderHairline = Color(0xFFDADBF8)
private val lavenderBorder = Color(0xFFD0D1E7)
private val paleBlueWash = Color(0xFFF4F7FF)
private val paleBlueTint = Color(0xFFEEEFFF)
private val lightBlueTint = Color(0xFFDFE8FF)
private val otherBarBlue = Color(0xFFC7D5FF)
private val softShadow = Color(0x1401001A)

// Blues. The button blue carries white text; the royal-to-sky pair is the signature gradient.
private val buttonBlue = Color(0xFF5064FA)
private val royalBlue = Color(0xFF015CEF)
private val skyBlue = Color(0xFF01BAEF)
private val deepSkyBlue = Color(0xFF0186D0)
private val deepRoyalBlue = Color(0xFF102CC8)
private val tagBlueText = Color(0xFF0150D6)
private val runningBlueText = Color(0xFF1240C4)

// Pastel hero panel, top to bottom.
private val heroPanelTop = Color(0xFFECF5FB)
private val heroPanelMiddle = Color(0xFFF4FAFF)
private val heroPanelBottom = Color(0xFFFAFDFF)

// Mint, coral and amber accents.
private val mintTint = Color(0xFFDCF9F2)
private val mintBorder = Color(0xFFB1E5D7)
private val tealText = Color(0xFF0B6B57)
private val brightMint = Color(0xFF5EFFBA)
private val coralTint = Color(0xFFFFE6EB)
private val coralText = Color(0xFFB4233F)
private val coralDestructive = Color(0xFFD63A5C)
private val amberTint = Color(0xFFFFF0D2)
private val amberText = Color(0xFF7F5200)

/** CSS colour of the page background; `index.html` uses the same value so the loading splash does not flash. */
const val PAGE_BACKGROUND_CSS_COLOUR = "#FFFFFF"

private val lightSiteColourScheme = lightColorScheme(
    primary = buttonBlue,
    onPrimary = paperWhite,
    primaryContainer = lightBlueTint,
    onPrimaryContainer = runningBlueText,
    secondary = royalBlue,
    onSecondary = paperWhite,
    secondaryContainer = paleBlueTint,
    onSecondaryContainer = runningBlueText,
    tertiary = tealText,
    onTertiary = paperWhite,
    tertiaryContainer = mintTint,
    onTertiaryContainer = tealText,
    background = paperWhite,
    onBackground = ink,
    surface = paperWhite,
    onSurface = ink,
    surfaceVariant = paleBlueWash,
    onSurfaceVariant = inkAtSeventyPercent,
    surfaceContainerLowest = paperWhite,
    surfaceContainerLow = paleBlueWash,
    surfaceContainer = paleBlueWash,
    surfaceContainerHigh = paleBlueTint,
    surfaceContainerHighest = lightBlueTint,
    outline = lavenderBorder,
    outlineVariant = lavenderHairline,
    error = coralDestructive,
    onError = paperWhite,
    errorContainer = coralTint,
    onErrorContainer = coralText,
)

/**
 * Colours the Material colour scheme has no slot for: the pastel hero panel, the blue gradients,
 * the "AI" and "Code" tags, and the four progress states.
 *
 * @property captionText ink at 60% opacity, for the quietest captions
 * @property headlineGradientStart first colour of the gradient used on key words of hero titles
 * @property emphasisBlockTop top colour of the strong blue card used once per page
 * @property aiTagContainer background of the tag that marks a step done by the AI model
 * @property codeTagContainer background of the tag that marks a step done by plain code
 * @property runningContainer background of an "in progress" chip; the other states follow the same naming
 * @property livePulse bright mint used only for the pulsing "running" dot
 * @property otherBar fill of the losing bars in a bar comparison
 * @property cardShadow colour of the very soft shadow under cards
 */
@Immutable
data class SiteColours(
    val captionText: Color,
    val heroPanelTop: Color,
    val heroPanelMiddle: Color,
    val heroPanelBottom: Color,
    val heroDecoration: Color,
    val headlineGradientStart: Color,
    val headlineGradientEnd: Color,
    val progressGradientStart: Color,
    val progressGradientEnd: Color,
    val emphasisBlockTop: Color,
    val emphasisBlockBottom: Color,
    val onEmphasisBlock: Color,
    val onEmphasisBlockMuted: Color,
    val aiTagContainer: Color,
    val aiTagContent: Color,
    val codeTagContainer: Color,
    val codeTagContent: Color,
    val codeTagBorder: Color,
    val pendingContainer: Color,
    val pendingContent: Color,
    val runningContainer: Color,
    val runningContent: Color,
    val livePulse: Color,
    val doneContainer: Color,
    val doneContent: Color,
    val failedContainer: Color,
    val failedContent: Color,
    val warningContainer: Color,
    val warningContent: Color,
    val otherBar: Color,
    val cardShadow: Color,
) {
    /** Royal-blue-to-sky gradient for the key words of hero titles; the end is darkened so it stays readable on the pastel panel. */
    fun headlineGradientBrush(): Brush = Brush.horizontalGradient(listOf(headlineGradientStart, headlineGradientEnd))

    /** Royal-blue-to-sky gradient for finished progress segments and the winning bar (decoration only, never text). */
    fun progressGradientBrush(): Brush = Brush.horizontalGradient(listOf(progressGradientStart, progressGradientEnd))

    /** Top-to-bottom deep blue fill for the one emphasis card on a page. */
    fun emphasisBlockBrush(): Brush = Brush.verticalGradient(listOf(emphasisBlockTop, emphasisBlockBottom))

    /** Top-to-bottom pastel fill for the hero panel at the top of each page. */
    fun heroPanelBrush(): Brush = Brush.verticalGradient(listOf(heroPanelTop, heroPanelMiddle, heroPanelBottom))
}

private val lightSiteColours = SiteColours(
    captionText = inkAtSixtyPercent,
    heroPanelTop = heroPanelTop,
    heroPanelMiddle = heroPanelMiddle,
    heroPanelBottom = heroPanelBottom,
    heroDecoration = lightBlueTint,
    headlineGradientStart = royalBlue,
    headlineGradientEnd = deepSkyBlue,
    progressGradientStart = royalBlue,
    progressGradientEnd = skyBlue,
    emphasisBlockTop = royalBlue,
    emphasisBlockBottom = deepRoyalBlue,
    onEmphasisBlock = paperWhite,
    onEmphasisBlockMuted = Color(0xD9FFFFFF),
    aiTagContainer = lightBlueTint,
    aiTagContent = tagBlueText,
    codeTagContainer = mintTint,
    codeTagContent = tealText,
    codeTagBorder = mintBorder,
    pendingContainer = paleBlueWash,
    pendingContent = inkAtSeventyPercent,
    runningContainer = lightBlueTint,
    runningContent = runningBlueText,
    livePulse = brightMint,
    doneContainer = mintTint,
    doneContent = tealText,
    failedContainer = coralTint,
    failedContent = coralText,
    warningContainer = amberTint,
    warningContent = amberText,
    otherBar = otherBarBlue,
    cardShadow = softShadow,
)

/** Gives composables the extra site colours; read it as `LocalSiteColours.current`. */
val LocalSiteColours = staticCompositionLocalOf {
    lightSiteColours
}

/**
 * The font families the site uses.
 *
 * @property sansSerif Manrope: every heading, body text, label and button
 * @property monospace JetBrains Mono: prompts, replies, the formula, ids and table numbers
 */
@Immutable
data class SiteFontFamilies(
    val sansSerif: FontFamily,
    val monospace: FontFamily,
)

/** Gives composables the site's font families; the theme fills it in, read it as `LocalSiteFontFamilies.current`. */
val LocalSiteFontFamilies = staticCompositionLocalOf {
    SiteFontFamilies(FontFamily.Default, FontFamily.Monospace)
}

/** Loads one weight of a variable font by setting its weight axis to [weight]. */
@Composable
private fun variableFontAtWeight(fontResource: FontResource, weight: FontWeight) = Font(
    resource = fontResource,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

/** Loads the bundled font families from the app's resources. */
@Composable
private fun loadSiteFontFamilies(): SiteFontFamilies {
    val sansSerifWeights =
        listOf(FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold, FontWeight.Bold, FontWeight.ExtraBold)
    return SiteFontFamilies(
        sansSerif = FontFamily(
            sansSerifWeights.map {
                variableFontAtWeight(Res.font.manrope_variable, it)
            },
        ),
        monospace = FontFamily(
            variableFontAtWeight(Res.font.jetbrainsmono_variable, FontWeight.Normal),
            variableFontAtWeight(Res.font.jetbrainsmono_variable, FontWeight.SemiBold),
        ),
    )
}

/** The site's type scale: heavy, slightly tightened Manrope headings over relaxed, readable body text. */
private fun buildSiteTypography(fontFamilies: SiteFontFamilies): Typography {
    val sans = fontFamilies.sansSerif
    val tightTracking = (-0.01).em
    return Typography(
        displayLarge = TextStyle(
            fontFamily = sans,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 62.sp,
            lineHeight = 68.sp,
            letterSpacing = tightTracking
        ),
        displayMedium = TextStyle(
            fontFamily = sans,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 48.sp,
            lineHeight = 54.sp,
            letterSpacing = tightTracking
        ),
        displaySmall = TextStyle(
            fontFamily = sans,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 36.sp,
            lineHeight = 42.sp,
            letterSpacing = tightTracking
        ),
        headlineLarge = TextStyle(
            fontFamily = sans,
            fontWeight = FontWeight.Bold,
            fontSize = 40.sp,
            lineHeight = 48.sp,
            letterSpacing = tightTracking
        ),
        headlineMedium = TextStyle(
            fontFamily = sans,
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
            lineHeight = 37.sp,
            letterSpacing = tightTracking
        ),
        headlineSmall = TextStyle(
            fontFamily = sans,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            lineHeight = 26.sp,
            letterSpacing = tightTracking
        ),
        titleLarge = TextStyle(
            fontFamily = sans,
            fontWeight = FontWeight.Bold,
            fontSize = 19.sp,
            lineHeight = 26.sp,
            letterSpacing = tightTracking
        ),
        titleMedium = TextStyle(fontFamily = sans, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 22.sp),
        titleSmall = TextStyle(fontFamily = sans, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 20.sp),
        bodyLarge = TextStyle(fontFamily = sans, fontWeight = FontWeight.Medium, fontSize = 18.sp, lineHeight = 29.sp),
        bodyMedium = TextStyle(fontFamily = sans, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 24.sp),
        bodySmall = TextStyle(fontFamily = sans, fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 20.sp),
        labelLarge = TextStyle(fontFamily = sans, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 22.sp),
        labelMedium = TextStyle(
            fontFamily = sans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            lineHeight = 18.sp
        ),
        labelSmall = TextStyle(
            fontFamily = sans,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.1.em
        ),
    )
}

/** Style for prompts, replies, ids and other text that must keep its exact spacing. */
@Composable
fun monospaceTextStyle(): TextStyle = TextStyle(
    fontFamily = LocalSiteFontFamilies.current.monospace,
    fontWeight = FontWeight.Normal,
    fontSize = 13.sp,
    lineHeight = 20.sp,
)

/** Wraps the whole site in its light, white-first look with blue accents. Every screen must sit inside this. */
@Composable
fun ProjectsSiteTheme(content: @Composable () -> Unit) {
    val siteFontFamilies = loadSiteFontFamilies()
    CompositionLocalProvider(
        LocalSiteColours provides lightSiteColours,
        LocalSiteFontFamilies provides siteFontFamilies,
    ) {
        MaterialTheme(
            colorScheme = lightSiteColourScheme,
            typography = buildSiteTypography(siteFontFamilies),
            content = content,
        )
    }
}
