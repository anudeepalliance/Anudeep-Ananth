package util.usage

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PersistAgentUsageLogToMongoTest {

    @Test
    fun `persistAgentUsageLogToMongo does not throw when mongo is unreachable`() = runBlocking {
        persistAgentUsageLogToMongo(
            kind = UsageKind.AGENT,
            ok = true,
            durationMs = 12,
            agentId = "job_matching",
            inputChars = 40,
            visitorEnteredQueryText = "Paste a role. See the fit.",
            agentReplyText = "Fit score 84. Strong demand gen overlap.",
        )
    }

    @Test
    fun `clippedVisitorEnteredQueryText trims blanks and caps length`() {
        assertNull(clippedVisitorEnteredQueryText("   "))
        assertEquals("hello", clippedVisitorEnteredQueryText("  hello  "))
        val tooLongVisitorEnteredQueryText = "a".repeat(MAX_VISITOR_ENTERED_QUERY_TEXT_CHARACTERS + 25)
        assertEquals(
            MAX_VISITOR_ENTERED_QUERY_TEXT_CHARACTERS,
            clippedVisitorEnteredQueryText(tooLongVisitorEnteredQueryText)?.length,
        )
    }
}
