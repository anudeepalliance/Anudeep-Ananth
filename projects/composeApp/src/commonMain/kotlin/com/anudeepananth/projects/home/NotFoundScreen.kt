package com.anudeepananth.projects.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.anudeepananth.projects.design.AddSpace
import com.anudeepananth.projects.design.BodyText
import com.anudeepananth.projects.design.ContentCard
import com.anudeepananth.projects.design.EyebrowText
import com.anudeepananth.projects.design.GradientRule
import com.anudeepananth.projects.design.HeroBand
import com.anudeepananth.projects.design.MessageWithAction
import com.anudeepananth.projects.design.PageBody
import com.anudeepananth.projects.design.RightArrowIcon
import com.anudeepananth.projects.design.SecondaryText
import com.anudeepananth.projects.design.SubheadingText
import com.anudeepananth.projects.design.TagChip
import com.anudeepananth.projects.design.TagTone
import com.anudeepananth.projects.design.TitleText
import com.anudeepananth.projects.design.WrappingRow
import com.anudeepananth.projects.design.projectCardMaximumWidth
import com.anudeepananth.projects.design.spacingDefault
import com.anudeepananth.projects.design.spacingMedium
import com.anudeepananth.projects.design.spacingSmall
import com.anudeepananth.projects.design.spacingXLarge
import com.anudeepananth.projects.navigation.AppRoutePaths

/** Shown for any address the site does not know, with a way to the Ocrolus assignment. */
@Composable
fun NotFoundScreen(onNavigateToPath: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        HeroBand(eyebrow = "Page not found", title = "Nothing at this address", gradientWordCount = 1)
        PageBody {
            MessageWithAction(
                title = "This page does not exist",
                body = "The link may be mistyped, or the page may have moved.",
                actionLabel = "Go to the Ocrolus assignment",
                onAction = {
                    onNavigateToPath(AppRoutePaths.OCROLUS_ASSIGNMENT)
                },
            )
        }
    }
}
