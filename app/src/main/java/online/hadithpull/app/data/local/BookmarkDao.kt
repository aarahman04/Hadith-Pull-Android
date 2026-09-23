package online.hadithpull.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks WHERE folderId = :folderId ORDER BY savedAt DESC")
    fun items(folderId: Long): Flow<List<BookmarkEntity>>

    @Query("SELECT folderId FROM bookmarks WHERE hadithKey = :key")
    fun savedFolderIds(key: String): Flow<List<Long>>

    @Query("SELECT * FROM bookmarks WHERE folderId = :folderId AND hadithKey = :key LIMIT 1")
    suspend fun find(folderId: Long, key: String): BookmarkEntity?

    @Query("SELECT * FROM bookmarks WHERE id = :id")
    suspend fun findById(id: Long): BookmarkEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(bookmark: BookmarkEntity): Long

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM bookmarks WHERE folderId = :folderId AND hadithKey = :key")
    suspend fun deleteByFolderAndKey(folderId: Long, key: String)

    @Query("UPDATE bookmarks SET folderId = :folderId WHERE id = :id")
    suspend fun moveToFolder(id: Long, folderId: Long)
}
