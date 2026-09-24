package online.hadithpull.app.card

import online.hadithpull.app.data.prefs.ArabicScript
import online.hadithpull.app.domain.Grading
import online.hadithpull.app.domain.Hadith
import online.hadithpull.app.domain.PrimaryGrade
import online.hadithpull.app.domain.text.CARD_EXCERPT
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** §3.2: one CardInput mapping for both the Reader and Bookmarks (S1). */
class CardInputFromTest {
    private fun hadith(english: String, arabic: String = "", narrator: String = "— Narrated X") = Hadith(
        collection = "bukhari", collectionTitle = "Sahih al-Bukhari", ref = "1", book = 1, inBook = 1,
        chapter = "", english = english, arabic = arabic, narrator = narrator, grades = emptyList(),
        primary = PrimaryGrade(grade = "Sahih", by = null, cat = Grading.SAHIH, consensus = true),
        sunnahUrl = null,
    )

    @Test
    fun `short english is passed through unexcerpted`() {
        val h = hadith(english = "short")
        val input = CardInput.from(h, includeArabic = true, script = ArabicScript.NASKH)
        assertEquals("short", input.english)
        assertFalse(input.excerpt)
    }

    @Test
    fun `english over the card budget is excerpted`() {
        val h = hadith(english = "word ".repeat(200).trim()) // well past CARD_EXCERPT.whole
        val input = CardInput.from(h, includeArabic = true, script = ArabicScript.NASKH)
        assertTrue(input.english.length <= CARD_EXCERPT.whole)
        assertTrue(input.excerpt)
    }

    @Test
    fun `arabic is dropped when includeArabic is false`() {
        val h = hadith(english = "short", arabic = "arabic text")
        val input = CardInput.from(h, includeArabic = false, script = ArabicScript.NASKH)
        assertEquals("", input.arabic)
    }

    @Test
    fun `arabic is kept when includeArabic is true`() {
        val h = hadith(english = "short", arabic = "arabic text")
        val input = CardInput.from(h, includeArabic = true, script = ArabicScript.NASKH)
        assertEquals("arabic text", input.arabic)
    }

    @Test
    fun `collectionTitle, ref, primary grade and narrator pass through verbatim`() {
        val h = hadith(english = "short")
        val input = CardInput.from(h, includeArabic = true, script = ArabicScript.BOLD)
        assertEquals(h.collectionTitle, input.book)
        assertEquals(h.ref, input.number)
        assertEquals(h.primary?.grade, input.status)
        assertEquals(h.primary?.cat, input.statusCat)
        assertEquals(h.narrator, input.narrator)
        assertEquals(ArabicScript.BOLD, input.script)
        assertEquals("hadithpull.online", input.site)
    }

    @Test
    fun `a null primary maps to an empty status and a null statusCat`() {
        val h = hadith(english = "short").copy(primary = null)
        val input = CardInput.from(h, includeArabic = true, script = ArabicScript.NASKH)
        assertEquals("", input.status)
        assertEquals(null, input.statusCat)
    }
}
