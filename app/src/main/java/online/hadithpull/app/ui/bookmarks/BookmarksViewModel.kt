package online.hadithpull.app.ui.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import online.hadithpull.app.data.BookmarkRepository
import online.hadithpull.app.data.CreateFolderResult
import online.hadithpull.app.data.FolderSummary
import online.hadithpull.app.data.RenameFolderResult
import online.hadithpull.app.data.local.BookmarkEntity
import online.hadithpull.app.data.local.FolderEntity

/** §2.4: proxies BookmarkRepository's Room Flows for both the Folders list and Folder detail screens. */
class BookmarksViewModel(private val bookmarkRepository: BookmarkRepository) : ViewModel() {
    val folders: StateFlow<List<FolderSummary>> = bookmarkRepository.folders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun folder(id: Long): Flow<FolderEntity?> = bookmarkRepository.folder(id)

    fun items(folderId: Long): Flow<List<BookmarkEntity>> = bookmarkRepository.items(folderId)

    suspend fun createFolder(name: String): CreateFolderResult = bookmarkRepository.createFolder(name)

    suspend fun renameFolder(id: Long, name: String): RenameFolderResult = bookmarkRepository.renameFolder(id, name)

    suspend fun deleteFolder(id: Long) = bookmarkRepository.deleteFolder(id)

    suspend fun removeItem(itemId: Long) = bookmarkRepository.removeItem(itemId)

    suspend fun moveItem(itemId: Long, targetFolderId: Long) = bookmarkRepository.moveItem(itemId, targetFolderId)
}
