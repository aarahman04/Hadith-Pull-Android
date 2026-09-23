package online.hadithpull.app.domain.text

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ParagraphizeTest {

    @Test
    fun `existing multi-paragraph text is returned as-is regardless of length`() {
        val text = "Para one.\n\nPara two."
        assertEquals(listOf("Para one.", "Para two."), paragraphize(text))
    }

    @Test
    fun `short single-block text under 600 chars is returned as-is`() {
        val text = "Short text without newlines."
        assertEquals(listOf(text), paragraphize(text))
    }

    @Test
    fun `text with fewer than 4 sentences is returned as-is`() {
        val text = "word ".repeat(150).trimEnd() + "."
        assertTrue(text.length >= 600)
        assertEquals(listOf(text.jsTrim()), paragraphize(text))
    }

    @Test
    fun `text is returned as-is when the sentence matches do not cover its start`() {
        val filler = "This is filler content. ".repeat(30)
        val text = "." + filler
        assertTrue(text.length >= 600)
        assertEquals(listOf(text.jsTrim()), paragraphize(text))
    }

    @Test
    fun `text collapsing to a single paragraph is returned as-is`() {
        fun sentence(bodyLen: Int) = "a".repeat(bodyLen) + ". "
        val text = sentence(288) + sentence(288) + sentence(13) + sentence(13)
        assertTrue(text.length >= 600)
        assertEquals(listOf(text.jsTrim()), paragraphize(text))
    }

    @Test
    fun `a long lossless text splits into paragraphs at the 300 char threshold`() {
        fun sentence(bodyLen: Int) = "a".repeat(bodyLen) + ". "
        val text = sentence(150) + sentence(150) + sentence(150) + sentence(150)
        val result = paragraphize(text)
        assertEquals(2, result.size)
        assertEquals(text.jsTrim(), result.joinToString(" "))
    }
}
