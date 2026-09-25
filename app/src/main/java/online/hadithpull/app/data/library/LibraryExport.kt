package online.hadithpull.app.data.library

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import online.hadithpull.app.data.BookmarkRepository

/** R1.8/R0.6: builds one self-contained "Hadith Pull library" .html file -- readable and
 * searchable in any browser, and re-importable into the app via its embedded JSON block. */
object LibraryExport {
    suspend fun buildDocument(bookmarkRepository: BookmarkRepository, folderId: Long? = null): String {
        val folders = bookmarkRepository.exportSnapshot(folderId)
        val document = LibraryExportDocument(
            exportedAt = System.currentTimeMillis(),
            folders = folders.map { f -> LibraryExportFolder(f.name, f.items.map { LibraryExportItem(it.key) }) },
        )
        val json = Json.encodeToString(document)
        // </script> inside the embedded JSON would otherwise close the script block early.
        val escapedJsonForScript = json.replace("</", "<\\/")
        val bodyHtml = LibraryHtmlTemplate.renderFoldersHtml(folders)
        return LibraryHtmlTemplate.SHELL
            .replace("__TITLE__", LibraryHtmlTemplate.htmlEscape("Hadith Pull: your saved narrations"))
            .replace("__BODY__", bodyHtml)
            .replace("__JSON__", escapedJsonForScript)
    }
}
