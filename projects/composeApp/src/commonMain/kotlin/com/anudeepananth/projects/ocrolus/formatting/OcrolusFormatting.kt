package com.anudeepananth.projects.ocrolus.formatting

import com.anudeepananth.projects.design.ProgressStatus
import com.anudeepananth.projects.ocrolus.OcrolusRunStatus
import com.anudeepananth.projects.ocrolus.OcrolusStepStatus
import kotlin.math.abs
import kotlin.math.roundToLong

private const val MILLISECONDS_PER_SECOND = 1000L
private const val SECONDS_PER_MINUTE = 60L
private const val DIGITS_PER_THOUSANDS_GROUP = 3

/** Turns a length of time into words like "2m 14s", or "48s" when it is under a minute. */
fun formatDurationAsMinutesAndSeconds(durationMilliseconds: Long): String {
    val totalSeconds = (durationMilliseconds.coerceAtLeast(0) / MILLISECONDS_PER_SECOND)
    val minutes = totalSeconds / SECONDS_PER_MINUTE
    val seconds = totalSeconds % SECONDS_PER_MINUTE
    return if (minutes == 0L) "${seconds}s" else "${minutes}m ${seconds}s"
}

/** Shows a number with exactly one digit after the decimal point, such as "42.5". */
fun formatNumberWithOneDecimal(value: Double): String {
    val tenths = (value * 10).roundToLong()
    val sign = if (tenths < 0) "-" else ""
    return "$sign${abs(tenths) / 10}.${abs(tenths) % 10}"
}

/** Shows a whole number with commas between thousands, such as "12,345". */
fun formatWholeNumberWithThousandsSeparators(value: Long): String {
    val digits = abs(value).toString()
    val groupedDigits = digits.reversed().chunked(DIGITS_PER_THOUSANDS_GROUP).joinToString(",").reversed()
    return if (value < 0) "-$groupedDigits" else groupedDigits
}

/** Shows a model's 0-10 note score, or a dash when there is none because the product is already adopted. */
fun formatOptionalScore(score: Int?): String = score?.toString() ?: "–"

fun OcrolusRunStatus.toProgressStatus(): ProgressStatus = when (this) {
    OcrolusRunStatus.QUEUED -> ProgressStatus.PENDING
    OcrolusRunStatus.RUNNING -> ProgressStatus.RUNNING
    OcrolusRunStatus.COMPLETED -> ProgressStatus.DONE
    OcrolusRunStatus.FAILED -> ProgressStatus.FAILED
}

fun OcrolusRunStatus.toSentenceCaseLabel(): String = when (this) {
    OcrolusRunStatus.QUEUED -> "Queued"
    OcrolusRunStatus.RUNNING -> "Running"
    OcrolusRunStatus.COMPLETED -> "Completed"
    OcrolusRunStatus.FAILED -> "Failed"
}

fun OcrolusStepStatus.toProgressStatus(): ProgressStatus = when (this) {
    OcrolusStepStatus.PENDING -> ProgressStatus.PENDING
    OcrolusStepStatus.RUNNING -> ProgressStatus.RUNNING
    OcrolusStepStatus.COMPLETED -> ProgressStatus.DONE
    OcrolusStepStatus.FAILED -> ProgressStatus.FAILED
}

/** True while a run is still waiting or working, so the live view is the right place to watch it. */
fun OcrolusRunStatus.isStillInProgress(): Boolean = this == OcrolusRunStatus.QUEUED || this == OcrolusRunStatus.RUNNING
