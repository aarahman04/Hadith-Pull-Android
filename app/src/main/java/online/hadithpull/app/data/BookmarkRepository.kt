package online.hadithpull.app.data

import java.text.Collator
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import online.hadithpull.app.data.local.BookmarkDao
import online.hadithpull.app.data.local.BookmarkEntity
import online.hadithpull.app.data.local.FolderDao
import online.hadithpull.app.data.local.FolderEntity
import online.hadithpull.app.data.local.HadithPullDatabase
import online.hadithpull.app.domain.Hadith
import online.hadithpull.app.domain.text.jsTrim

data class FolderSummary(val id: Long, val name: String, val count: Int, val createdAt: Long)

sealed interface CreateFolderResult {
    data class Created(val folder: FolderEntity) : CreateFolderResult
    data class Existing(val folder: FolderEntity) : CreateFolderResult
    data object Invalid : CreateFolderResult
}

sealed interface RenameFolderResult {
    data class Renamed(val folder: FolderEntity) : RenameFolderResult
    data object Duplicate : RenameFolderResult
    data object Invalid : RenameFolderResult
}

private const val MAX_FOLDER_NAME_LENGTH = 40

/** §1.4 P4: jsTrim, then empty or over 40 characters is invalid. Returns null when invalid. */
fun validFolderName(raw: String): String? {
    val trimmed = raw.jsTrim()
    return trimmed.takeIf { it.isNotEmpty() && it.length <= MAX_FOLDER_NAME_LENGTH }
}

/** Folder list order, approximating JS localeCompare with java.text.Collator. */
fun sortFolderSummaries(folders: List<FolderSummary>): List<FolderSummary> {
    val collator = Collator.getInstance()
    return folders.sortedWith(compareBy(collator) { it.name })
}

/** The Hadith snapshot stored on a bookmark row, for Copy/Share parity with the Reader (§2.4). */
fun BookmarkEntity.toHadith(): Hadith = Hadith(
    slug = slug,
    number = number,
    book = book,
    chapter = chapter,
    status = status,
    english = english,
    arabic = arabic,
    narrator = narrator,
)

private fun bookmarkEntityOf(folderId: Long, hadith: Hadith, savedAt: Long): BookmarkEntity = BookmarkEntity(
    folderId = folderId,
    hadithKey = hadith.key,
    slug = hadith.slug,
    number = hadith.number,
    book = hadith.book,
    chapter = hadith.chapter,
    status = hadith.status,
    english = hadith.english,
    arabic = hadith.arabic,
    narrator = hadith.narrator,
    savedAt = savedAt,
)

/** §1.6 BookmarkRepository contract. */
class BookmarkRepository(
    private val db: HadithPullDatabase,
    private val folderDao: FolderDao = db.folderDao(),
    private val bookmarkDao: BookmarkDao = db.bookmarkDao(),
) {
    fun folders(): Flow<List<FolderSummary>> = folderDao.foldersWithCounts().map { rows ->
        sortFolderSummaries(rows.map { FolderSummary(it.id, it.name, it.count, it.createdAt) })
    }

    fun folder(id: Long): Flow<FolderEntity?> = folderDao.folder(id)

    fun items(folderId: Long): Flow<List<BookmarkEntity>> = bookmarkDao.items(folderId)

    fun savedFolderIds(key: String): Flow<Set<Long>> = bookmarkDao.savedFolderIds(key).map { it.toSet() }

    fun isSaved(key: String): Flow<Boolean> = savedFolderIds(key).map { it.isNotEmpty() }

    suspend fun createFolder(name: String): CreateFolderResult {
        val trimmed = validFolderName(name) ?: return CreateFolderResult.Invalid
        folderDao.findByNameIgnoreCase(trimmed)?.let { return CreateFolderResult.Existing(it) }
        val createdAt = System.currentTimeMillis()
        val id = folderDao.insert(FolderEntity(name = trimmed, createdAt = createdAt))
        return CreateFolderResult.Created(FolderEntity(id = id, name = trimmed, createdAt = createdAt))
    }

    suspend fun renameFolder(id: Long, name: String): RenameFolderResult {
        val trimmed = validFolderName(name) ?: return RenameFolderResult.Invalid
        val duplicate = folderDao.findByNameIgnoreCase(trimmed)
        if (duplicate != null && duplicate.id != id) return RenameFolderResult.Duplicate
        val current = folderDao.folderOnce(id) ?: return RenameFolderResult.Invalid
        val renamed = current.copy(name = trimmed)
        folderDao.update(renamed)
        return RenameFolderResult.Renamed(renamed)
    }

    suspend fun deleteFolder(id: Long) {
        folderDao.folderOnce(id)?.let { folderDao.delete(it) }
    }

    /** Removes the item if (folderId, key) exists; otherwise inserts it. Returns the new saved state. */
    suspend fun toggle(folderId: Long, hadith: Hadith): Boolean = db.withTransaction {
        val existing = bookmarkDao.find(folderId, hadith.key)
        if (existing != null) {
            bookmarkDao.deleteById(existing.id)
            false
        } else {
            bookmarkDao.insert(bookmarkEntityOf(folderId, hadith, System.currentTimeMillis()))
            true
        }
    }

    /** Insert-or-ignore; never removes. Used by "create folder" inside the Save dialog (P4). */
    suspend fun ensureSaved(folderId: Long, hadith: Hadith) {
        bookmarkDao.insert(bookmarkEntityOf(folderId, hadith, System.currentTimeMillis()))
    }

    suspend fun removeItem(itemId: Long) {
        bookmarkDao.deleteById(itemId)
    }

    /** If the target folder already holds the same key, deletes the moved row instead of duplicating it. */
    suspend fun moveItem(itemId: Long, targetFolderId: Long) = db.withTransaction {
        val item = bookmarkDao.findById(itemId) ?: return@withTransaction
        val clash = bookmarkDao.find(targetFolderId, item.hadithKey)
        if (clash != null) {
            bookmarkDao.deleteById(itemId)
        } else {
            bookmarkDao.moveToFolder(itemId, targetFolderId)
        }
    }
}
