package online.hadithpull.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bookmarks",
    foreignKeys = [
        ForeignKey(
            entity = FolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["folderId", "hadithKey"], unique = true),
        Index(value = ["hadithKey"]),
    ],
)
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val folderId: Long,
    val hadithKey: String,
    val slug: String,
    val number: String,
    val book: String,
    val chapter: String,
    val status: String,
    val english: String,
    val arabic: String,
    val narrator: String,
    val savedAt: Long,
)
