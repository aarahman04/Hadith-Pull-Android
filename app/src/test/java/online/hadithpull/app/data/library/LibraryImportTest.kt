package online.hadithpull.app.data.library

import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryImportTest {

    private fun documentJson(format: String = "hadith-pull-library", folders: String = "[]") =
        """{"format":"$format","version":1,"exportedAt":0,"folders":$folders}"""

    private fun wrapped(json: String) =
        """<!doctype html><body><script type="application/json" id="hadith-pull-library">$json</script></body>"""

    @Test
    fun `a well-formed document parses successfully`() {
        val html = wrapped(documentJson(folders = """[{"name":"A","items":[{"key":"bukhari:1"}]}]"""))
        val result = LibraryImport.parse(html.toByteArray())
        assertTrue(result is ParseResult.Ok)
        val doc = (result as ParseResult.Ok).document
        assertTrue(doc.folders.size == 1 && doc.folders[0].items.size == 1)
    }

    @Test
    fun `the wrong format string is NotALibraryFile`() {
        val html = wrapped(documentJson(format = "something-else"))
        assertTrue(LibraryImport.parse(html.toByteArray()) is ParseResult.NotALibraryFile)
    }

    @Test
    fun `malformed JSON is NotALibraryFile`() {
        val html = wrapped("{not valid json")
        assertTrue(LibraryImport.parse(html.toByteArray()) is ParseResult.NotALibraryFile)
    }

    @Test
    fun `a file with no embedded library script is NotALibraryFile`() {
        val html = "<!doctype html><body>plain html, no library data</body>"
        assertTrue(LibraryImport.parse(html.toByteArray()) is ParseResult.NotALibraryFile)
    }

    @Test
    fun `201 folders is TooLarge`() {
        val folders = (1..201).joinToString(",") { """{"name":"F$it","items":[]}""" }
        val html = wrapped(documentJson(folders = "[$folders]"))
        assertTrue(LibraryImport.parse(html.toByteArray()) is ParseResult.TooLarge)
    }

    @Test
    fun `5001 items across folders is TooLarge`() {
        val items = (1..5001).joinToString(",") { """{"key":"bukhari:$it"}""" }
        val html = wrapped(documentJson(folders = """[{"name":"F","items":[$items]}]"""))
        assertTrue(LibraryImport.parse(html.toByteArray()) is ParseResult.TooLarge)
    }

    @Test
    fun `a byte array over 10MB is TooLarge without attempting to parse it`() {
        val huge = ByteArray(10 * 1024 * 1024 + 1)
        assertTrue(LibraryImport.parse(huge) is ParseResult.TooLarge)
    }

    @Test
    fun `an escaped closing script tag inside the JSON round-trips through the un-escape step`() {
        // LibraryExport escapes "</" to "<\/" so an embedded "</script>"-like string in a folder
        // name can't close the script block early; parse() must undo exactly that escaping.
        val json = documentJson(folders = """[{"name":"A <\/script> B","items":[]}]""")
        val html = wrapped(json)
        val result = LibraryImport.parse(html.toByteArray())
        assertTrue(result is ParseResult.Ok)
        assertTrue((result as ParseResult.Ok).document.folders[0].name == "A </script> B")
    }
}
