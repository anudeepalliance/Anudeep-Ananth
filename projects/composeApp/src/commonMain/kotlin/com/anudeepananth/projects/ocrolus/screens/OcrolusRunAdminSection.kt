package com.anudeepananth.projects.ocrolus.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import com.anudeepananth.projects.design.AddSpace
import com.anudeepananth.projects.design.ExpandableSection
import com.anudeepananth.projects.design.LabelledCheckbox
import com.anudeepananth.projects.design.LabelledTextField
import com.anudeepananth.projects.design.NoticeBanner
import com.anudeepananth.projects.design.NoticeTone
import com.anudeepananth.projects.design.PrimaryButton
import com.anudeepananth.projects.design.SecondaryButton
import com.anudeepananth.projects.design.ThinDivider
import com.anudeepananth.projects.design.spacingDefault
import com.anudeepananth.projects.design.spacingSmall
import com.anudeepananth.projects.design.spacingXLarge
import com.anudeepananth.projects.ocrolus.OcrolusAssignmentRun
import com.anudeepananth.projects.ocrolus.OcrolusUpdateRunRequest
import com.anudeepananth.projects.ocrolus.api.OcrolusServerApi
import com.anudeepananth.projects.ocrolus.api.ServerReply
import com.anudeepananth.projects.platform.formatEpochMillisecondsAsViewerLocalDateAndTime
import kotlinx.coroutines.launch

/**
 * Tools for the site owner at the bottom of a result page: rename the run, add presenter notes,
 * mark it as the featured reference run, or delete it. Saving needs the admin passcode; deleting needs it only for the featured run.
 */
@Composable
fun OcrolusRunAdminSection(
    run: OcrolusAssignmentRun,
    onRunEdited: (OcrolusAssignmentRun) -> Unit,
    onRunDeleted: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var adminPasscode by remember {
        mutableStateOf("")
    }
    var editedDisplayLabel by remember(run.runId) {
        mutableStateOf(run.displayLabel)
    }
    var editedPresenterNotes by remember(run.runId) {
        mutableStateOf(run.presenterNotes)
    }
    var isMarkedFeatured by remember(run.runId) {
        mutableStateOf(run.isFeatured)
    }
    var isRequestInFlight by remember {
        mutableStateOf(false)
    }
    var isAskingToConfirmDelete by remember {
        mutableStateOf(false)
    }
    var outcomeMessage by remember {
        mutableStateOf<String?>(null)
    }
    var didLastActionFail by remember {
        mutableStateOf(false)
    }
    val hasPasscode = adminPasscode.isNotBlank()

    ExpandableSection(title = "Admin", supportingText = "For the site owner; needs the admin passcode") {
        LabelledTextField(
            value = adminPasscode,
            onValueChange = {
                adminPasscode = it
            },
            label = "Admin passcode",
            isSecret = true,
        )
        AddSpace(spacingDefault)
        LabelledTextField(
            value = editedDisplayLabel,
            onValueChange = {
                editedDisplayLabel = it
            },
            label = "Label for this run",
        )
        AddSpace(spacingDefault)
        LabelledTextField(
            value = editedPresenterNotes,
            onValueChange = {
                editedPresenterNotes = it
            },
            label = "Presenter notes",
            isMultiLine = true,
        )
        AddSpace(spacingSmall)
        LabelledCheckbox(
            label = "Feature this run as the reference run",
            isChecked = isMarkedFeatured,
            onCheckedChange = {
                isMarkedFeatured = it
            },
        )
        AddSpace(spacingDefault)
        val currentOutcomeMessage = outcomeMessage
        if (currentOutcomeMessage != null) {
            NoticeBanner(currentOutcomeMessage, tone = if (didLastActionFail) NoticeTone.PROBLEM else NoticeTone.TIP)
            AddSpace(spacingDefault)
        }
        PrimaryButton(
            text = "Save changes",
            onClick = {
                isRequestInFlight = true
                coroutineScope.launch {
                    val updateRunRequest = OcrolusUpdateRunRequest(
                        editedDisplayLabel.trim(),
                        editedPresenterNotes.trim(),
                        isMarkedFeatured
                    )
                    when (val saveReply =
                        OcrolusServerApi.saveRunEditsToServer(run.runId, adminPasscode, updateRunRequest)) {
                        is ServerReply.Success -> {
                            didLastActionFail = false
                            outcomeMessage = "Changes saved."
                            onRunEdited(saveReply.value)
                        }

                        is ServerReply.Failure -> {
                            didLastActionFail = true
                            outcomeMessage = saveReply.message
                        }
                    }
                    isRequestInFlight = false
                }
            },
            isEnabled = hasPasscode,
            isLoading = isRequestInFlight,
        )

        AddSpace(spacingXLarge)
        ThinDivider()
        AddSpace(spacingXLarge)
        SecondaryButton(
            text = "Delete this run",
            onClick = {
                isAskingToConfirmDelete = true
            },
        )
    }

    if (isAskingToConfirmDelete) {
        OcrolusRunDeletionSheet(
            runId = run.runId,
            runDescription = "${run.modelDisplayName}, ${formatEpochMillisecondsAsViewerLocalDateAndTime(run.completedAtEpochMilliseconds ?: run.startedAtEpochMilliseconds)}",
            adminPasscode = adminPasscode.takeIf {
                it.isNotBlank()
            },
            onRunDeleted = onRunDeleted,
            onDismiss = {
                isAskingToConfirmDelete = false
            },
        )
    }
}
