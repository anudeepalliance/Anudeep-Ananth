package web

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ThinkingProgressLinesTest {

    @Test
    fun `thinking progress has fifteen lines and holds a final almost-there beat`() {
        assertEquals(15, ThinkingProgressLines.lines.size)
        assertEquals("Thinking", ThinkingProgressLines.lines.first())
        assertEquals("Making final touches", ThinkingProgressLines.lines[12])
        assertEquals("Almost there", ThinkingProgressLines.lines.last())
        assertTrue(ThinkingProgressLines.lines.all { line -> line == line.trim() })
    }
}
