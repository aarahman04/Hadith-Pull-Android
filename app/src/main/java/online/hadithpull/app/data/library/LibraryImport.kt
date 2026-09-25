package online.hadithpull.app.data.library

import kotlinx.serialization.json.Json
import online.hadithpull.app.data.BookmarkRepository
import online.hadithpull.app.data.EnsureSavedResult
import online.hadithpull.app.data.HadithSource
import online.hadithpull.app.data.resolve

sealed interface ParseResult {
    data class Ok(val document: LibraryExportDocument) : ParseResult
    data object NotALibraryFile : ParseResult
    data object TooLarge : ParseResult
}

/** R0.6: caps so a hostile or corrupted file can't exhaust memory or hang the app resolving
 * thousands of keys. */
object LibraryImport {
    private const val MAX_BYTES = 10 * 1024 * 1024
    private const val MAX_FOLDERS = 200
    private const val MAX_ITEMS = 5000
    private val SCRIPT_PATTERN = Regex(
        """<script type="application/json" id="hadith-pull-library">(.*?)</script>""",
        RegexOption.DOT_MATCHES_ALL,
    )
    private val json = Json { ignoreUnknownKeys = true }

    fun parse(bytes: ByteArray): ParseResult {
        if (bytes.size > MAX_BYTES) return ParseResult.TooLarge
        val text = bytes.toString(Charsets.UTF_8)
        val match = SCRIPT_PATTERN.find(text) ?: return ParseResult.NotALibraryFile
        val jsonText = match.groupValues[1].replace("<\\/", "</")
        val doc = try {
            json.decodeFromString<LibraryExportDocument>(jsonText)
        } catch (e: Exception) {
            return ParseResult.NotALibraryFile
        }
        if (doc.format != "hadith-pull-library") return ParseResult.NotALibraryFile
        if (doc.folders.size > MAX_FOLDERS || doc.folders.sumOf { it.items.size } > MAX_ITEMS) return ParseResult.TooLarge
        return ParseResult.Ok(doc)
    }
}

data class ImportPreview(val folderCount: Int, val itemCount: Int)
data class ImportSummary(val added: Int, val alreadySaved: Int, val notFound: Int, val foldersTouched: Int)

fun previewImport(doc: LibraryExportDocument): ImportPreview =
    ImportPreview(doc.folders.size, doc.folders.sumOf { it.items.size })

/** R0.6 merge rules: a matching folder name merges (case-insensitively); items are added with
 * ensureSaved, so duplicates are skipped; nothing is ever deleted or overwritten. Every hadith is
 * resolved against the bundled dataset -- the file's own text is never trusted. */
suspend fun BookmarkRepository.applyImport(doc: LibraryExportDocument, hadithSource: HadithSource): ImportSummary {
    var added = 0
    var alreadySaved = 0
    var notFound = 0
    val foldersTouched = mutableSetOf<Long>()
    for (folder in doc.folders) {
        val folderId = ensureFolder(folder.name.take(40))
        foldersTouched += folderId
        for (item in folder.items) {
            val hadith = hadithSource.resolve(item.key)
            if (hadith == null) {
                notFound++
                continue
            }
            when (ensureSaved(folderId, hadith)) {
                EnsureSavedResult.Added -> added++
                EnsureSavedResult.AlreadyPresent -> alreadySaved++
            }
        }
    }
    return ImportSummary(added, alreadySaved, notFound, foldersTouched.size)
}
