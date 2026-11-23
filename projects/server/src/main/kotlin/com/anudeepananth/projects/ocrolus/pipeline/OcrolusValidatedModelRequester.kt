package com.anudeepananth.projects.ocrolus.pipeline

import com.anudeepananth.projects.ocrolus.OcrolusModelCallLog
import com.anudeepananth.projects.ocrolus.OcrolusModelOption
import com.anudeepananth.projects.ocrolus.data.OcrolusRunStorage
import com.anudeepananth.projects.ocrolus.model.AssignmentModelCaller
import com.anudeepananth.projects.ocrolus.model.OcrolusModelCatalogue
import com.anudeepananth.projects.ocrolus.model.stripModelReasoningBlocks
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

/** Thrown when no model produced a usable reply for a step, after repairs and backups were tried. */
class OcrolusModelStepFailedException(message: String) : Exception(message)

/** How many times the requester goes round the whole model list before giving up on a step. */
private const val MAXIMUM_ROUNDS_THROUGH_ALL_MODELS = 3

/** Pause after a full round in which every model failed; free models are often busy for only a few seconds. */
private const val PAUSE_BETWEEN_ROUNDS_MILLISECONDS = 15_000L

/**
 * Asks a model for a reply and refuses to accept it until it passes a check.
 *
 * It starts with the model that last worked in this run (the chosen model at first), then goes round
 * the other models in turn. For each model:
 * 1. send the prompt;
 * 2. if the reply fails the check, send the prompt once more with the reason it was rejected;
 * 3. if it still fails, or the call errors (busy, down, too slow), move to the next model.
 * If every model fails it pauses and goes round again, up to [MAXIMUM_ROUNDS_THROUGH_ALL_MODELS] times,
 * because free models are usually only busy for a short while.
 * Every call, good or bad, is saved so the run page can show the real prompts and replies.
 */
class OcrolusValidatedModelRequester(
    private val chosenModel: OcrolusModelOption,
    private val modelCaller: AssignmentModelCaller,
    private val runStorage: OcrolusRunStorage,
    private val progressTracker: OcrolusRunProgressTracker,
) {
    private val modelsToTryInOrder = OcrolusModelCatalogue.modelsToTryInOrder(chosenModel)

    @Volatile
    private var indexOfModelThatLastWorked = 0

    /**
     * @param parseAndValidateReply turns the reply text into the wanted value, or throws with a
     * message that explains what is wrong (that message is sent back to the model on the repair try)
     */
    suspend fun <ParsedReply> requestValidatedReply(
        stepKey: String,
        promptText: String,
        temperature: Double,
        parseAndValidateReply: (String) -> ParsedReply,
    ): ParsedReply {
        val failureReasons = mutableListOf<String>()
        val startingModelIndex = indexOfModelThatLastWorked
        for (roundIndex in 0 until MAXIMUM_ROUNDS_THROUGH_ALL_MODELS) {
            if (roundIndex > 0) {
                progressTracker.reportProgress(
                    stepKey,
                    "Every model was busy. Waiting ${PAUSE_BETWEEN_ROUNDS_MILLISECONDS / 1000} seconds, then trying again"
                )
                delay(PAUSE_BETWEEN_ROUNDS_MILLISECONDS)
            }
            for (offset in modelsToTryInOrder.indices) {
                val modelIndex = (startingModelIndex + offset) % modelsToTryInOrder.size
                val model = modelsToTryInOrder[modelIndex]
                if (offset > 0 || roundIndex > 0) {
                    progressTracker.reportProgress(stepKey, "Trying ${model.displayName}")
                }
                val parsedReply = requestFromOneModelWithOneRepair(
                    stepKey,
                    model,
                    promptText,
                    temperature,
                    failureReasons,
                    parseAndValidateReply
                )
                if (parsedReply != null) {
                    indexOfModelThatLastWorked = modelIndex
                    if (model != chosenModel) {
                        progressTracker.updateUsage { usage ->
                            usage.copy(fallbackModelIdsUsed = (usage.fallbackModelIdsUsed + model.modelId).distinct())
                        }
                    }
                    return parsedReply.value
                }
            }
        }
        throw OcrolusModelStepFailedException(
            "No model gave a usable reply. ${
                failureReasons.takeLast(3).joinToString(" | ")
            }"
        )
    }

    /** Wraps a reply so a step whose valid answer is itself null can still be told apart from "no answer". */
    private class AcceptedReply<Value>(val value: Value)

    /** Tries one model: the prompt, then at most one repair attempt. Returns null when the model could not deliver. */
    private suspend fun <ParsedReply> requestFromOneModelWithOneRepair(
        stepKey: String,
        model: OcrolusModelOption,
        promptText: String,
        temperature: Double,
        failureReasons: MutableList<String>,
        parseAndValidateReply: (String) -> ParsedReply,
    ): AcceptedReply<ParsedReply>? {
        var promptForThisAttempt = promptText
        for (attemptIndex in 0..1) {
            val replyText = callModelAndSaveLog(stepKey, model, promptForThisAttempt, temperature)
            if (replyText == null) {
                failureReasons += "${model.displayName}: the call failed"
                return null
            }
            try {
                return AcceptedReply(parseAndValidateReply(replyText))
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                val rejectionReason = exception.message ?: "The reply could not be read."
                failureReasons += "${model.displayName}: $rejectionReason"
                if (attemptIndex == 0) {
                    progressTracker.reportProgress(
                        stepKey,
                        "Reply rejected (${rejectionReason.take(140)}). Asking the model to fix it"
                    )
                }
                promptForThisAttempt =
                    "$promptText\n\nYOUR PREVIOUS REPLY WAS REJECTED.\nReason: $rejectionReason\nReply again and follow the rules and output format exactly."
            }
        }
        return null
    }

    /** Calls the model once, records the call, and returns the cleaned reply text or null when the call errored. */
    private suspend fun callModelAndSaveLog(
        stepKey: String,
        model: OcrolusModelOption,
        promptText: String,
        temperature: Double
    ): String? {
        val startedAtEpochMilliseconds = System.currentTimeMillis()
        return try {
            val modelReply = modelCaller.requestModelReply(model, promptText, temperature)
            val cleanedReplyText = stripModelReasoningBlocks(modelReply.text)
            runStorage.saveModelCallLog(
                OcrolusModelCallLog(
                    runId = progressTracker.currentRun.runId,
                    stepKey = stepKey,
                    modelId = model.modelId,
                    promptText = promptText,
                    responseText = cleanedReplyText,
                    wasSuccessful = true,
                    durationMilliseconds = System.currentTimeMillis() - startedAtEpochMilliseconds,
                    promptTokens = modelReply.promptTokens,
                    completionTokens = modelReply.completionTokens,
                    createdAtEpochMilliseconds = startedAtEpochMilliseconds,
                ),
            )
            progressTracker.updateUsage { usage ->
                usage.copy(
                    modelCallCount = usage.modelCallCount + 1,
                    promptTokens = usage.promptTokens + modelReply.promptTokens,
                    completionTokens = usage.completionTokens + modelReply.completionTokens,
                )
            }
            cleanedReplyText
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            runStorage.saveModelCallLog(
                OcrolusModelCallLog(
                    runId = progressTracker.currentRun.runId,
                    stepKey = stepKey,
                    modelId = model.modelId,
                    promptText = promptText,
                    responseText = "",
                    wasSuccessful = false,
                    errorMessage = exception.message,
                    durationMilliseconds = System.currentTimeMillis() - startedAtEpochMilliseconds,
                    createdAtEpochMilliseconds = startedAtEpochMilliseconds,
                ),
            )
            progressTracker.updateUsage { usage ->
                usage.copy(modelCallCount = usage.modelCallCount + 1)
            }
            val shortReason =
                Regex("Status code: \\d+").find(exception.message.orEmpty())?.value ?: exception.message.orEmpty()
                    .replace("\n", " ").take(100)
            progressTracker.reportProgress(stepKey, "${model.displayName} did not answer ($shortReason)")
            null
        }
    }
}
