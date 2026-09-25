package online.hadithpull.app.data.library

import kotlinx.serialization.Serializable

/** R0.6/R1.8: the JSON schema embedded in an exported "Hadith Pull library" .html file. Only
 * each item's key is stored -- import resolves every hadith against the app's own bundled
 * dataset, never trusting text carried in the file itself. */
@Serializable
data class LibraryExportDocument(
    val format: String = "hadith-pull-library",
    val version: Int = 1,
    val exportedAt: Long,
    val folders: List<LibraryExportFolder>,
)

@Serializable
data class LibraryExportFolder(val name: String, val items: List<LibraryExportItem>)

@Serializable
data class LibraryExportItem(val key: String)
