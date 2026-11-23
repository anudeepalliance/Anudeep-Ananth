package com.anudeepananth.projects.ocrolus.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.anudeepananth.projects.design.DestructiveConfirmationBottomSheet
import com.anudeepananth.projects.ocrolus.api.OcrolusServerApi
import com.anudeepananth.projects.ocrolus.api.ServerReply
import kotlinx.coroutines.launch

/**
 * The bottom sheet that asks "Delete this run?" and, on confirm, deletes the run on the server.
 * It closes through [onRunDeleted] on success; on failure it stays open and shows the server's message.
 * [runDescription] names the run, for example its model and completion time; [adminPasscode] is sent only when typed.
 */
@Composable
fun OcrolusRunDeletionSheet(
    runId: String,
    runDescription: String,
    adminPasscode: String?,
    onRunDeleted: () -> Unit,
    onDismiss: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    var isDeleteRequestInFlight by remember(runId) {
        mutableStateOf(false)
    }
    var deletionErrorMessage by remember(runId) {
        mutableStateOf<String?>(null)
    }
    DestructiveConfirmationBottomSheet(
        title = "Delete this run?",
        subjectLine = runDescription,
        warningText = "This removes the run and its saved prompts. It cannot be undone.",
        confirmLabel = "Delete run",
        isConfirming = isDeleteRequestInFlight,
        errorMessage = deletionErrorMessage,
        onConfirm = {
            isDeleteRequestInFlight = true
            deletionErrorMessage = null
            coroutineScope.launch {
                when (val deleteReply = OcrolusServerApi.deleteRunFromServer(runId, adminPasscode)) {
                    is ServerReply.Success -> onRunDeleted()
                    is ServerReply.Failure -> deletionErrorMessage = deleteReply.message
                }
                isDeleteRequestInFlight = false
            }
        },
        onDismiss = onDismiss,
    )
}
