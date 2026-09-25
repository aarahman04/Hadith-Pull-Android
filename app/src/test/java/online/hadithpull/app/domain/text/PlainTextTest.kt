package online.hadithpull.app.domain.text

import online.hadithpull.app.domain.Grading
import online.hadithpull.app.domain.Hadith
import online.hadithpull.app.domain.PrimaryGrade
import org.junit.Assert.assertEquals
import org.junit.Test

class PlainTextTest {

    private fun hadith(
        primary: PrimaryGrade? = null,
        chapter: String = "",
        narrator: String = "",
    ) = Hadith(
        collection = "bukhari",
        collectionTitle = "Sahih al-Bukhari",
        ref = "1",
        book = 1,
        inBook = 1,
        chapter = chapter,
        english = "The believer is not one who eats his fill while his neighbor goes hungry.",
        arabic = "",
        narrator = narrator,
        grades = emptyList(),
        primary = primary,
        sunnahUrl = null,
    )

    @Test
    fun `plainText with status and chapter present`() {
        val h = hadith(
            primary = PrimaryGrade(grade = "Sahih", by = null, cat = Grading.SAHIH, consensus = true),
            chapter = "Chapter of neighborliness",
            narrator = "— Narrated Abu Huraira",
        )
        val expected = listOf(
            h.english,
            "— Narrated Abu Huraira",
            "",
            "Sahih al-Bukhari, Hadith 1 (Sahih)",
            "Chapter: Chapter of neighborliness",
        ).joinToString("\n")
        assertEquals(expected, plainText(h))
    }

    @Test
    fun `plainText without status or chapter omits both lines`() {
        val h = hadith()
        val expected = listOf(
            h.english,
            "",
            "Sahih al-Bukhari, Hadith 1",
        ).joinToString("\n")
        assertEquals(expected, plainText(h))
    }

    @Test
    fun `shareText quotes the excerpt or english and appends the reference and site`() {
        val h = hadith()
        val expected = "\"${h.english}\"\n\nSahih al-Bukhari, Hadith 1\nhttps://hadithpull.online"
        assertEquals(expected, shareText(h))
    }
}
