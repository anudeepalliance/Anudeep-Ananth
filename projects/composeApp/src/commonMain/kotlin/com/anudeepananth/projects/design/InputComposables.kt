package com.anudeepananth.projects.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

/** A labelled box the visitor can type into; set [isSecret] to hide what is typed, as for a passcode. */
@Composable
fun LabelledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isSecret: Boolean = false,
    isMultiLine: Boolean = false,
) {
    DisableSelection {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier
                .widthIn(max = if (isMultiLine) readableTextMaximumWidth else textFieldMaximumWidth)
                .fillMaxWidth()
                .heightIn(min = if (isMultiLine) multiLineTextFieldMinimumHeight else touchTargetMinimum),
            label = {
                Text(text = label, style = MaterialTheme.typography.labelMedium)
            },
            textStyle = MaterialTheme.typography.bodyMedium,
            singleLine = !isMultiLine,
            visualTransformation = if (isSecret) PasswordVisualTransformation() else VisualTransformation.None,
            shape = RoundedCornerShape(cornerRadiusMedium),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                cursorColor = MaterialTheme.colorScheme.primary,
            ),
        )
    }
}

/** One choice in a pick-one list: a radio dot, a title and optional supporting text in a bordered row. */
@Composable
fun SelectableOptionRow(
    title: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
) {
    val rowShape = RoundedCornerShape(cornerRadiusMedium)
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val containerColor =
        if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(rowShape)
            .background(containerColor)
            .border(if (isSelected) borderWidthSelected else borderWidthThin, borderColor, rowShape)
            .clickable(onClick = onSelect)
            .showHandCursorOnHover()
            .heightIn(min = touchTargetMinimum)
            .padding(horizontal = paddingDefault, vertical = paddingMedium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(iconSizeDefault).border(borderWidthSelected, borderColor, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier.size(statusDotSize).clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
        AddSpace(spacingMedium)
        Column(modifier = Modifier.weight(1f)) {
            SubheadingText(title)
            if (supportingText != null) {
                SecondaryText(supportingText)
            }
        }
    }
}

/** A tick box with a label beside it; clicking either one toggles it. */
@Composable
fun LabelledCheckbox(
    label: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadiusMedium))
            .clickable {
                onCheckedChange(!isChecked)
            }
            .showHandCursorOnHover()
            .heightIn(min = touchTargetMinimum)
            .padding(end = paddingMedium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = null,
            modifier = Modifier.padding(horizontal = paddingSmall),
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )
        BodyText(label)
    }
}
