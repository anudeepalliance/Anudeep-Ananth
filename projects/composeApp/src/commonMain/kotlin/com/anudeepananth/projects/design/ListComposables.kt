package com.anudeepananth.projects.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A short list where each entry is one sentence, shown with a small blue dot in front.
 * Used where a paragraph would be hard to scan, such as the measurement plan.
 */
@Composable
fun BulletedSentenceList(sentences: List<String>, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(spacingMedium)) {
        sentences.forEach { sentence ->
            Row(horizontalArrangement = Arrangement.spacedBy(spacingMedium)) {
                Box(
                    modifier = Modifier
                        .padding(top = bulletDotTopOffset)
                        .size(statusDotSize)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                )
                BodyText(sentence, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}
