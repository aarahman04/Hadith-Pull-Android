package online.hadithpull.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [FolderEntity::class, BookmarkEntity::class], version = 1, exportSchema = true)
abstract class HadithPullDatabase : RoomDatabase() {
    abstract fun folderDao(): FolderDao
    abstract fun bookmarkDao(): BookmarkDao
}
