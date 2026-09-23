package online.hadithpull.app.data

import androidx.room.withTransaction
import online.hadithpull.app.data.local.HadithPullDatabase
import online.hadithpull.app.data.local.RecentDao
import online.hadithpull.app.data.local.RecentHadithEntity
import online.hadithpull.app.domain.DrawEngine
import online.hadithpull.app.domain.DrawResult
import online.hadithpull.app.domain.Hadith

/** §4.1 seam: a plain class owned by AppContainer, callable without a ViewModel. */
sealed interface DrawOutcome {
    data class Success(val hadith: Hadith) : DrawOutcome
    data class Fallback(val hadith: Hadith, val cause: DrawResult.Failure) : DrawOutcome
    data class Failure(val cause: DrawResult.Failure) : DrawOutcome
}

/** G2: the one-line muted note shown above the reference for a fallback narration. */
fun fallbackNote(cause: DrawResult.Failure): String = when (cause) {
    DrawResult.Failure.Network -> "Offline — showing a narration you've read before"
    DrawResult.Failure.Busy -> "Service busy — showing a narration you've read before"
    DrawResult.Failure.KeyRejected, DrawResult.Failure.Exhausted ->
        "Couldn't fetch a new one — showing a narration you've read before"
}

private fun recentHadithEntityOf(hadith: Hadith, shownAt: Long) = RecentHadithEntity(
    hadithKey = hadith.key,
    slug = hadith.slug,
    number = hadith.number,
    book = hadith.book,
    chapter = hadith.chapter,
    status = hadith.status,
    english = hadith.english,
    arabic = hadith.arabic,
    narrator = hadith.narrator,
    shownAt = shownAt,
)

private fun RecentHadithEntity.toHadith() = Hadith(
    slug = slug,
    number = number,
    book = book,
    chapter = chapter,
    status = status,
    english = english,
    arabic = arabic,
    narrator = narrator,
)

class HadithRepository(
    private val drawEngine: DrawEngine,
    private val db: HadithPullDatabase,
    private val recentDao: RecentDao = db.recentDao(),
) {
    /**
     * G1/G2 (P6): a success writes recent_hadiths (upsert + trim to the newest 30). A failure
     * falls back to a uniformly random cached narration whose key isn't [currentKey], without
     * writing recent_hadiths; if none qualifies, the ordinary failure is returned.
     */
    suspend fun draw(currentKey: String?): DrawOutcome = when (val result = drawEngine.draw()) {
        is DrawResult.Success -> {
            db.withTransaction {
                recentDao.upsert(recentHadithEntityOf(result.hadith, System.currentTimeMillis()))
                recentDao.trimToNewest30()
            }
            DrawOutcome.Success(result.hadith)
        }
        is DrawResult.Failure -> {
            val fallback = recentDao.randomExcept(currentKey.orEmpty())
            if (fallback != null) {
                DrawOutcome.Fallback(fallback.toHadith(), result)
            } else {
                DrawOutcome.Failure(result)
            }
        }
    }
}
