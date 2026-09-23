package online.hadithpull.app.domain.text

import org.junit.Assert.assertEquals
import org.junit.Test

class WordCountTest {
    @Test
    fun `empty string is zero words`() {
        assertEquals(0, wordCount(""))
    }

    @Test
    fun `whitespace-only string is zero words`() {
        assertEquals(0, wordCount("     "))
    }

    @Test
    fun `counts words split on JS whitespace, including U+00A0`() {
        assertEquals(4, wordCount("The Prophet said something."))
    }

    @Test
    fun `jsTrim is applied before counting`() {
        assertEquals(2, wordCount("  two words  "))
    }
}
