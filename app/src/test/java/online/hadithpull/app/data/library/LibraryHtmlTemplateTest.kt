package online.hadithpull.app.data.library

import online.hadithpull.app.data.ExportFolder
import online.hadithpull.app.data.ExportItem
import online.hadithpull.app.domain.Hadith
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryHtmlTemplateTest {

    @Test
    fun `htmlEscape covers the five HTML-significant characters`() {
        assertEquals("&amp;", LibraryHtmlTemplate.htmlEscape("&"))
        assertEquals("&lt;", LibraryHtmlTemplate.htmlEscape("<"))
        assertEquals("&gt;", LibraryHtmlTemplate.htmlEscape(">"))
        assertEquals("&quot;", LibraryHtmlTemplate.htmlEscape("\""))
        assertEquals("&#39;", LibraryHtmlTemplate.htmlEscape("'"))
        assertEquals("&lt;script&gt;alert(1)&lt;/script&gt;", LibraryHtmlTemplate.htmlEscape("<script>alert(1)</script>"))
    }

    private fun hadith(english: String = "Text.") = Hadith(
        collection = "bukhari",
        collectionTitle = "Sahih al-Bukhari",
        ref = "1",
        book = 1,
        inBook = 1,
        chapter = "",
        english = english,
        arabic = "",
        narrator = "",
        grades = emptyList(),
        primary = null,
        sunnahUrl = null,
    )

    @Test
    fun `a folder name containing script tags renders escaped, not as live markup`() {
        val folders = listOf(ExportFolder("<script>alert(1)</script>", listOf(ExportItem("bukhari:1", hadith()))))
        val html = LibraryHtmlTemplate.renderFoldersHtml(folders)
        assertFalse("raw <script> tag leaked into the rendered HTML", html.contains("<script>alert(1)</script>"))
        assertTrue(html.contains("&lt;script&gt;alert(1)&lt;/script&gt;"))
    }

    @Test
    fun `english text containing HTML-significant characters is escaped in the rendered article`() {
        val folders = listOf(ExportFolder("Folder", listOf(ExportItem("bukhari:1", hadith(english = "He said \"<b>bold</b>\" & left.")))))
        val html = LibraryHtmlTemplate.renderFoldersHtml(folders)
        assertTrue(html.contains("He said &quot;&lt;b&gt;bold&lt;/b&gt;&quot; &amp; left."))
    }
}
