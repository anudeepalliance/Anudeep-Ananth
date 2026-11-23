package com.anudeepananth.projects.design

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import com.anudeepananth.projects.resources.Res
import com.anudeepananth.projects.resources.anudeep_portrait
import org.jetbrains.compose.resources.painterResource

/**
 * A link shown at the right of the top bar.
 *
 * @property compactLabel a shorter label used on phones, where the full one would not fit
 * @property isCurrentPage true when the visitor is already on this link's page, so it is drawn as active
 */
data class TopBarLink(val label: String, val compactLabel: String, val isCurrentPage: Boolean, val onClick: () -> Unit)

/** Side padding of the page for the given width band: tighter on phones, roomier on laptops. */
fun pageHorizontalPaddingFor(widthClass: WidthClass): Dp = when (widthClass) {
    WidthClass.COMPACT -> pageHorizontalPaddingCompact
    WidthClass.MEDIUM -> pageHorizontalPaddingMedium
    WidthClass.EXPANDED -> pageHorizontalPaddingExpanded
}

/**
 * The frame every screen sits in: a white top bar with the site name and links, then the screen's
 * [content] in one scrolling column that lets visitors select text. Screens start with a [HeroBand]
 * and put the rest inside a [PageBody]. [scrollResetKey] should change when the page changes so the
 * new page starts scrolled to the top.
 */
@Composable
fun PageScaffold(
    siteName: String,
    compactSiteName: String,
    onSiteNameClick: () -> Unit,
    topBarLinks: List<TopBarLink>,
    scrollResetKey: Any,
    footerText: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val widthClass = widthClassForAvailableWidth(maxWidth)
        CompositionLocalProvider(LocalWidthClass provides widthClass) {
            Column(modifier = Modifier.fillMaxSize()) {
                SiteTopBar(
                    siteName = if (widthClass == WidthClass.COMPACT) compactSiteName else siteName,
                    onSiteNameClick = onSiteNameClick,
                    topBarLinks = topBarLinks,
                )
                key(scrollResetKey) {
                    SelectionContainer(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                            content()
                            AddSpace(spacingHuge)
                            PageWidthColumn {
                                ThinDivider()
                                AddSpace(spacingDefault)
                                SecondaryText(footerText)
                                AddSpace(spacingXXLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}

/** The white strip at the top of every page: Anudeep's round photo and the site name on the left (together the home link), page links on the right, a hairline below. */
@Composable
private fun SiteTopBar(siteName: String, onSiteNameClick: () -> Unit, topBarLinks: List<TopBarLink>) {
    Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)) {
        PageWidthColumn {
            Row(
                modifier = Modifier.fillMaxWidth().height(topBarHeight),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val siteNameInteractionSource = remember {
                    MutableInteractionSource()
                }
                val isCompact = LocalWidthClass.current == WidthClass.COMPACT
                Row(
                    modifier = Modifier
                        .clickable(
                            interactionSource = siteNameInteractionSource,
                            indication = null,
                            onClick = onSiteNameClick
                        )
                        .showHandCursorOnHover(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(Res.drawable.anudeep_portrait),
                        contentDescription = "Anudeep Ananth",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(if (isCompact) portraitSizeCompact else portraitSizeExpanded)
                            .clip(CircleShape)
                            .border(borderWidthThin, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                    )
                    AddSpace(spacingSmall)
                    Text(
                        text = siteName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                    )
                }
                Box(modifier = Modifier.weight(1f))
                DisableSelection {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        topBarLinks.forEach { topBarLink ->
                            TopBarLinkWithUnderline(topBarLink)
                        }
                    }
                }
            }
        }
        ThinDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

/** One top bar link; the current page's link is blue with a thin blue underline, the others are muted ink. */
@Composable
private fun TopBarLinkWithUnderline(topBarLink: TopBarLink) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        TextLinkButton(
            text = if (LocalWidthClass.current == WidthClass.COMPACT) topBarLink.compactLabel else topBarLink.label,
            onClick = topBarLink.onClick,
            color = if (topBarLink.isCurrentPage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Box(
            modifier = Modifier
                .padding(horizontal = paddingDefault)
                .width(accentRuleWidth / 2)
                .height(topBarActiveUnderlineHeight)
                .background(if (topBarLink.isCurrentPage) MaterialTheme.colorScheme.primary else Color.Transparent),
        )
    }
}
