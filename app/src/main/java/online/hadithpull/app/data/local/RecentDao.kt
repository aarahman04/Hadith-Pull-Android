package online.hadithpull.app.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface RecentDao {
    /** A re-drawn key just updates shownAt (and the unchanged snapshot fields). */
    @Upsert
    suspend fun upsert(entity: RecentHadithEntity)

    @Query(
        "DELETE FROM recent_hadiths WHERE hadithKey NOT IN " +
            "(SELECT hadithKey FROM recent_hadiths ORDER BY shownAt DESC LIMIT 30)",
    )
    suspend fun trimToNewest30()

    /** G2: a uniformly random row whose key isn't the current narration's. */
    @Query("SELECT * FROM recent_hadiths WHERE hadithKey != :excludeKey ORDER BY RANDOM() LIMIT 1")
    suspend fun randomExcept(excludeKey: String): RecentHadithEntity?

    @Query("SELECT COUNT(*) FROM recent_hadiths")
    suspend fun count(): Int

    @Query("SELECT hadithKey FROM recent_hadiths")
    suspend fun allKeys(): List<String>

    @Query("SELECT shownAt FROM recent_hadiths WHERE hadithKey = :key")
    suspend fun shownAtOf(key: String): Long?
}
