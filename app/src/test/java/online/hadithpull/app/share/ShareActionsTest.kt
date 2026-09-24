package online.hadithpull.app.share

import org.junit.Assert.assertEquals
import org.junit.Test

class SlugifyTest {
    @Test
    fun `lowercases and hyphenates non-alphanumeric runs`() {
        assertEquals("sahih-bukhari", slugify("Sahih Bukhari"))
    }

    @Test
    fun `a compound number's comma and space become one hyphen`() {
        assertEquals("1645-1646", slugify("1645, 1646"))
    }

    @Test
    fun `trims leading and trailing hyphens`() {
        assertEquals("hadith", slugify("  --Hadith--  "))
    }

    @Test
    fun `empty input stays empty`() {
        assertEquals("", slugify(""))
    }
}

class CardFileNameTest {
    @Test
    fun `slugifies book and number, including a compound number`() {
        assertEquals("sahih-bukhari-1645-1646.png", cardFileName("Sahih Bukhari", "1645, 1646"))
    }

    @Test
    fun `falls back to hadith dot png when both are empty`() {
        assertEquals("hadith.png", cardFileName("", ""))
    }

    @Test
    fun `falls back to hadith dot png when the slugified name would start with a hyphen`() {
        assertEquals("hadith.png", cardFileName("", "1"))
    }

    @Test
    fun `a Muslim-style letter-suffixed ref like 11a slugifies cleanly`() {
        assertEquals("sahih-muslim-11a.png", cardFileName("Sahih Muslim", "11a"))
    }
}
