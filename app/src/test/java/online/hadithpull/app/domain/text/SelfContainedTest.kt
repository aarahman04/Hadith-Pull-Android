package online.hadithpull.app.domain.text

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SelfContainedTest {

    // Each pair embeds a cross-reference phrase in a short (<300 char) carrier sentence
    // (expected false) next to a similar-shaped sentence that does not match (expected true).
    private val crossReferenceCases = listOf(
        "This point was as mentioned above in the earlier chapter." to
            "This point was raised for the very first time in this chapter.",
        "The ruling here is the same as above in every respect." to
            "The ruling here is the same as his father in every respect.",
        "This account is similar to the above in its wording." to
            "This account is similar to the sunrise in its beauty.",
        "Scholars relate a similar hadith on this very topic." to
            "Scholars relate a similar story on this very topic.",
        "It reaches us through another chain of narrators entirely." to
            "It reaches us through a chain of command entirely.",
        "This matter has already been mentioned above at length." to
            "This matter has been discussed thoroughly at length.",
        "It reads like the previous hadith in nearly every word." to
            "It reads like the weather report in nearly every word.",
        "The two rulings apply to the same effect in practice." to
            "The two rulings apply to the same place in practice.",
        "Both carry the same meaning despite different wording." to
            "Both happened on the same day despite different weather.",
        "For more detail see hadith no. 1234 in this collection." to
            "For more detail see the mountain in this collection.",
        "This was mentioned in the previous hadith at some length." to
            "This was mentioned in the book at some length.",
    )

    @Test
    fun `each cross-reference pattern blocks a short carrier text`() {
        crossReferenceCases.forEachIndexed { i, (positive, _) ->
            assertFalse("pattern $i should have matched: $positive", isSelfContained(positive))
        }
    }

    @Test
    fun `each cross-reference pattern's negative counterpart is unaffected`() {
        crossReferenceCases.forEachIndexed { i, (_, negative) ->
            assertTrue("pattern $i should not have matched: $negative", isSelfContained(negative))
        }
    }

    @Test
    fun `text under 15 characters is not self-contained`() {
        assertFalse(isSelfContained("short"))
    }

    @Test
    fun `text with fewer than 3 whitespace-split words is not self-contained`() {
        assertFalse(isSelfContained("AAAAAAAAAAAAAA BBBBBBBBBBBBBBB"))
    }

    @Test
    fun `a cross-reference phrase is ignored once the text reaches 300 characters`() {
        val padding = "This narration carries extra detail and context. ".repeat(7)
        val text = "The ruling here is the same as above in every respect. $padding"
        assertTrue(text.length >= 300)
        assertTrue(isSelfContained(text))
    }

    @Test
    fun `an ordinary self-contained sentence passes`() {
        assertTrue(isSelfContained("The Prophet said whoever believes in Allah should speak good or remain silent."))
    }

    @Test
    fun `hasArabicWorthShowing requires length 12 and 3 words`() {
        assertFalse(hasArabicWorthShowing("قصير"))
        assertFalse(hasArabicWorthShowing("كلمةكلمةكلمة"))
        assertTrue(hasArabicWorthShowing("هذا نص عربي كافٍ"))
    }
}
