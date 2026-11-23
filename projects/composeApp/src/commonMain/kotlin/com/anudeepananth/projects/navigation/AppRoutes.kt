package com.anudeepananth.projects.navigation

/** The page addresses of this site, kept in one place so links and the router can never disagree. */
object AppRoutePaths {
    const val HOME = "/"

    /** Anudeep's main site. This subdomain has no home page of its own; its root sends visitors there. */
    const val MAIN_SITE_ADDRESS = "https://www.anudeepananth.com"
    const val OCROLUS_ASSIGNMENT = "/ocrolus"
    const val OCROLUS_COMPLETED_RUNS = "/ocrolus/runs"
    private const val RUN_RESULT_PART_QUERY_NAME = "part"

    /** Address of one run's result page; [partNumber] picks the tab to open, and part 1 needs no query. */
    fun ocrolusRunResult(runId: String, partNumber: Int = 1): String {
        val runResultPath = "$OCROLUS_COMPLETED_RUNS/$runId"
        return if (partNumber <= 1) runResultPath else "$runResultPath?$RUN_RESULT_PART_QUERY_NAME=$partNumber"
    }

    /** Reads the `part` number from a query such as `part=3`; returns 1 when it is missing or not a number. */
    fun readRunResultPartNumberFromQuery(queryWithoutQuestionMark: String): Int = queryWithoutQuestionMark
        .split('&')
        .firstOrNull {
            it.startsWith("$RUN_RESULT_PART_QUERY_NAME=")
        }
        ?.substringAfter('=')
        ?.toIntOrNull() ?: 1
}

/** Every page the site can show; [fromBrowserAddress] turns an address into one of these. */
sealed interface AppRoute {
    data object Home : AppRoute
    data object OcrolusAssignment : AppRoute
    data object OcrolusCompletedRuns : AppRoute

    /** The saved result of one run, identified by [runId], opened on part [partNumber] (1 to 4). */
    data class OcrolusRunResult(val runId: String, val partNumber: Int) : AppRoute
    data object NotFound : AppRoute

    companion object {
        /** Works out which page [browserAddress] (a path with an optional `?query`) points to; unknown addresses become [NotFound]. */
        fun fromBrowserAddress(browserAddress: String): AppRoute {
            val pathWithoutTrailingSlash = browserAddress.substringBefore('?').trimEnd('/')
            val query = browserAddress.substringAfter('?', missingDelimiterValue = "")
            val runResultPrefix = AppRoutePaths.OCROLUS_COMPLETED_RUNS + "/"
            return when {
                pathWithoutTrailingSlash.isEmpty() -> Home
                pathWithoutTrailingSlash == AppRoutePaths.OCROLUS_ASSIGNMENT -> OcrolusAssignment
                pathWithoutTrailingSlash == AppRoutePaths.OCROLUS_COMPLETED_RUNS -> OcrolusCompletedRuns
                pathWithoutTrailingSlash.startsWith(runResultPrefix) -> {
                    val runId = pathWithoutTrailingSlash.removePrefix(runResultPrefix)
                    if (runId.isNotBlank() && !runId.contains('/')) {
                        OcrolusRunResult(runId, AppRoutePaths.readRunResultPartNumberFromQuery(query))
                    } else {
                        NotFound
                    }
                }

                else -> NotFound
            }
        }
    }
}
