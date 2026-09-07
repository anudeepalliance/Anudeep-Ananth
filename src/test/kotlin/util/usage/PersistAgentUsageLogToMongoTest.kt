package util.usage

import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class PersistAgentUsageLogToMongoTest {

    @Test
    fun `persistAgentUsageLogToMongo does not throw when mongo is unreachable`() = runBlocking {
        persistAgentUsageLogToMongo(
            kind = UsageKind.AGENT,
            ok = true,
            durationMs = 12,
            agentId = "job_matching",
            inputChars = 40,
        )
    }
}
