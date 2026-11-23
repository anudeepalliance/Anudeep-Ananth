package com.anudeepananth.projects.ocrolus.model

import com.anudeepananth.projects.ocrolus.OcrolusModelOption
import com.anudeepananth.projects.ocrolus.OcrolusModelProvider
import com.anudeepananth.projects.util.ConfidentialObjects

/**
 * The AI models a run may use, in the order they are tried when one fails.
 * Ids come from OpenRouter's public model list (checked 23 Nov 2026); free ids end in `:free`.
 * Free models are ordered fastest first, measured on the real note-scoring prompt.
 */
object OcrolusModelCatalogue {

    private val freeModels = listOf(
        // Not free on its own, but Anudeep's MiniMax token plan covers it, so visitors can use it at no cost.
        OcrolusModelOption(
            "MiniMax-M2.7-highspeed",
            "MiniMax M2.7 Highspeed (token plan)",
            isFree = true,
            provider = OcrolusModelProvider.MINIMAX
        ),
        OcrolusModelOption("particle-ai/particle-1:free", "Particle-1 (free)", isFree = true),
        OcrolusModelOption("baidu/ernie-5.5-flash-preview:free", "Baidu ERNIE 5.5 Flash (free)", isFree = true),
        OcrolusModelOption("nvidia/nemotron-3.5-super-vl:free", "NVIDIA Nemotron 3.5 Super (free)", isFree = true),
        OcrolusModelOption("dots-studio/dots-3-note-preview:free", "Dots3 Note preview (free)", isFree = true),
        OcrolusModelOption("inclusionai/ling-3.2-flash:free", "inclusionAI Ling 3.2 Flash (free, slow)", isFree = true),
        OcrolusModelOption("google/gemma-4-31b-it:free", "Google Gemma 4 31B (free, often busy)", isFree = true),
    )

    private val paidModels = listOf(
        OcrolusModelOption("deepseek/deepseek-v4.1-flash", "DeepSeek V4.1 Flash", isFree = false),
    )

    /** True once a paid OpenRouter key has been added to the secrets file. */
    val arePaidModelsConfigured: Boolean = ConfidentialObjects.OPEN_ROUTER_PAID_MODELS_KEY.isNotBlank()

    /** Models offered in the picker: free ones always, paid ones only when a paid key exists. */
    fun modelsOfferedToVisitors(): List<OcrolusModelOption> =
        if (arePaidModelsConfigured) freeModels + paidModels else freeModels

    /** The offered model with this id, or null when the id is unknown or not offered. */
    fun findOfferedModel(modelId: String): OcrolusModelOption? = modelsOfferedToVisitors().firstOrNull {
        it.modelId == modelId
    }

    /**
     * Models to try for a run that chose [chosenModel]: the chosen one first, then the other free
     * models as backups. A paid run never falls back, so the result is always from the model named on it.
     */
    fun modelsToTryInOrder(chosenModel: OcrolusModelOption): List<OcrolusModelOption> =
        if (chosenModel.isFree) {
            listOf(chosenModel) + freeModels.filter {
                it.modelId != chosenModel.modelId
            }
        } else {
            listOf(chosenModel)
        }
}
