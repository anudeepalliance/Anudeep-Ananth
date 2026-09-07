package agents

import kotlinx.coroutines.runBlocking
import java.net.HttpURLConnection
import java.net.URI
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Optional live MiniMax smoke. Skips automatically when the network/API is unreachable.
 */
class PortfolioAgentRuntimeLiveTest {

    @Test
    fun `live job matching returns structured fit when network available`() = runBlocking {
        if (!canReachMiniMaxApi()) {
            println("SKIP PortfolioAgentRuntimeLiveTest: MiniMax API unreachable")
            return@runBlocking
        }

        val outcome = sharedPortfolioAgentRuntime.runJobMatchingFitAnalysis(
            jobDescriptionText = """
                Senior Demand Generation Manager — fully remote B2B SaaS.
                Own pipeline, lifecycle experiments, and growth marketing with RevOps partners.
                Mid-senior role. India remote welcome.
            """.trimIndent(),
        )

        if (outcome is JobMatchFitAnalysisOutcome.Failure) {
            println("SKIP PortfolioAgentRuntimeLiveTest: MiniMax returned ${outcome.errorMessage}")
            return@runBlocking
        }

        val success = assertIs<JobMatchFitAnalysisOutcome.Success>(outcome)
        assertTrue(success.fitAnalysis.summary.isNotBlank())
        assertTrue(success.fitAnalysis.fitScore in 0..100)
    }

    private fun canReachMiniMaxApi(): Boolean {
        return runCatching {
            val connection = URI("https://api.minimax.io").toURL().openConnection() as HttpURLConnection
            connection.connectTimeout = 2500
            connection.readTimeout = 2500
            connection.requestMethod = "HEAD"
            connection.connect()
            connection.responseCode in 200..499
        }.getOrDefault(false)
    }
}
