package online.hadithpull.app.domain.text

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ExcerptTest {

    @Test
    fun `null when text length is at or under the whole budget`() {
        val text = "x".repeat(50)
        assertNull(buildExcerpt(text, Budget(whole = 50, min = 10, cap = 40, worthHiding = 5)))
    }

    @Test
    fun `sentence path stops as soon as the excerpt reaches min`() {
        val text = "First sentence here now. Second sentence adds more length total for the test padding here."
        val result = buildExcerpt(text, Budget(whole = 20, min = 20, cap = 30, worthHiding = 5))
        assertEquals("First sentence here now.", result)
    }

    @Test
    fun `cap fallback trims to the last space when one exists`() {
        val text = "abcdefghij klmno"
        val result = buildExcerpt(text, Budget(whole = 10, min = 5, cap = 12, worthHiding = 2))
        assertEquals("abcdefghij…", result)
    }

    @Test
    fun `cap fallback drops the last character when no space exists`() {
        val text = "abcdefghijklmno"
        val result = buildExcerpt(text, Budget(whole = 5, min = 3, cap = 8, worthHiding = 1))
        assertEquals("abcdefg…", result)
    }

    @Test
    fun `null when the excerpt would not save enough characters`() {
        val text = "abcdefghijklmnopqrstuvwxy"
        assertNull(buildExcerpt(text, Budget(whole = 20, min = 100, cap = 15, worthHiding = 50)))
    }

    @Test
    fun `null when the excerpt exceeds 80 percent of the original text`() {
        val text = "abcdefghi"
        assertNull(buildExcerpt(text, Budget(whole = 8, min = 3, cap = 8, worthHiding = 1)))
    }

    @Test
    fun `bookmarkExcerpt returns short text unchanged`() {
        assertEquals("short text", bookmarkExcerpt("short text", 320))
    }

    @Test
    fun `bookmarkExcerpt cuts at the last space past 40 chars in and appends an ellipsis`() {
        val text = "a".repeat(50) + " " + "b".repeat(50)
        val result = bookmarkExcerpt(text, 60)
        assertEquals("a".repeat(50) + "…", result)
    }
}
