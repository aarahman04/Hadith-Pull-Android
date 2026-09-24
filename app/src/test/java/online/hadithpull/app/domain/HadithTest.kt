package online.hadithpull.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class HadithTest {

    private fun hadith(collection: String = "bukhari", ref: String = "1") = Hadith(
        collection = collection,
        collectionTitle = "Sahih al-Bukhari",
        ref = ref,
        book = 1,
        inBook = 1,
        chapter = "",
        english = "",
        arabic = "",
        narrator = "",
        grades = emptyList(),
        primary = null,
        sunnahUrl = null,
    )

    @Test
    fun `key joins collection and ref with a colon`() {
        assertEquals("bukhari:1", hadith("bukhari", "1").key)
    }

    @Test
    fun `key handles a Muslim-style letter suffix ref`() {
        assertEquals("muslim:11a", hadith("muslim", "11a").key)
    }
}
