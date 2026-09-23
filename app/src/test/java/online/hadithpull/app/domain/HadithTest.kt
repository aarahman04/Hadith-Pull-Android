package online.hadithpull.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class HadithTest {

    private fun hadith(number: String) = Hadith(
        slug = "sahih-bukhari",
        number = number,
        book = "Sahih Bukhari",
        chapter = "",
        status = "",
        english = "",
        arabic = "",
        narrator = "",
    )

    @Test
    fun `key joins slug and number verbatim, including a compound number`() {
        assertEquals("sahih-bukhari-1645, 1646", hadith("1645, 1646").key)
    }

    @Test
    fun `grading matches Sahih case-insensitively`() {
        assertEquals(Grading.SAHIH, grading("Sahih"))
        assertEquals(Grading.SAHIH, grading("sahih"))
    }

    @Test
    fun `grading matches Hasan`() {
        assertEquals(Grading.HASAN, grading("Hasan"))
    }

    @Test
    fun `grading matches the backtick, apostrophe and plain weak forms as Daif`() {
        assertEquals(Grading.DAIF, grading("Da`eef"))
        assertEquals(Grading.DAIF, grading("Da'if"))
        assertEquals(Grading.DAIF, grading("Daif"))
        assertEquals(Grading.DAIF, grading("weak"))
    }

    @Test
    fun `grading is Unknown for empty or unrecognised status`() {
        assertEquals(Grading.UNKNOWN, grading(""))
        assertEquals(Grading.UNKNOWN, grading("unknown string"))
    }
}
