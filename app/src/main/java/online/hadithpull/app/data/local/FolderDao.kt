package online.hadithpull.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class FolderCountRow(val id: Long, val name: String, val createdAt: Long, val count: Int)

@Dao
interface FolderDao {
    @Query(
        """
        SELECT f.id AS id, f.name AS name, f.createdAt AS createdAt, COUNT(b.id) AS count
        FROM folders f LEFT JOIN bookmarks b ON b.folderId = f.id
        GROUP BY f.id
        """,
    )
    fun foldersWithCounts(): Flow<List<FolderCountRow>>

    @Query("SELECT * FROM folders WHERE id = :id")
    fun folder(id: Long): Flow<FolderEntity?>

    @Query("SELECT * FROM folders WHERE id = :id")
    suspend fun folderOnce(id: Long): FolderEntity?

    @Query("SELECT * FROM folders WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun findByNameIgnoreCase(name: String): FolderEntity?

    @Insert
    suspend fun insert(folder: FolderEntity): Long

    @Update
    suspend fun update(folder: FolderEntity)

    @Delete
    suspend fun delete(folder: FolderEntity)
}
