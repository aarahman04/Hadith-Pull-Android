package online.hadithpull.app.domain.text

import online.hadithpull.app.domain.Hadith
import org.junit.Assert.assertEquals
import org.junit.Test

class PlainTextTest {

    private fun hadith(
        status: String = "",
        chapter: String = "",
        narrator: String = "",
    ) = Hadith(
        slug = "sahih-bukhari",
        number = "1",
        book = "Sahih Bukhari",
        chapter = chapter,
        status = status,
        english = "The believer is not one who eats his fill while his neighbor goes hungry.",
        arabic = "",
        narrator = narrator,
    )

    @Test
    fun `plainText with status and chapter present`() {
        val h = hadith(status = "Sahih", chapter = "Chapter of neighborliness", narrator = "— Narrated Abu Huraira")
        val expected = listOf(
            h.english,
            "— Narrated Abu Huraira",
            "",
            "Sahih Bukhari, Hadith 1 (Sahih)",
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
            "Sahih Bukhari, Hadith 1",
        ).joinToString("\n")
        assertEquals(expected, plainText(h))
    }

    @Test
    fun `shareText quotes the excerpt or english and appends the reference and site`() {
        val h = hadith()
        val expected = "\"${h.english}\"\n\n— Sahih Bukhari, Hadith 1\nhttps://hadithpull.online"
        assertEquals(expected, shareText(h))
    }

    @Test
    fun `titleCase splits on hyphens and capitalizes each part`() {
        assertEquals("Ibn E Majah", titleCase("ibn-e-majah"))
        assertEquals("Sahih Bukhari", titleCase("sahih-bukhari"))
    }
}
