package online.hadithpull.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** G1: last 30 successfully shown narrations, trimmed first-in first-out by shownAt. */
@Entity(tableName = "recent_hadiths")
data class RecentHadithEntity(
    @PrimaryKey val hadithKey: String,
    val slug: String,
    val number: String,
    val book: String,
    val chapter: String,
    val status: String,
    val english: String,
    val arabic: String,
    val narrator: String,
    val shownAt: Long,
)
