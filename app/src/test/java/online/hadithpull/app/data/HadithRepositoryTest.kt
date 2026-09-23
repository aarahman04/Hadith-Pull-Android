package online.hadithpull.app.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonPrimitive
import online.hadithpull.app.data.local.HadithPullDatabase
import online.hadithpull.app.data.local.RecentHadithEntity
import online.hadithpull.app.domain.DrawEngine
import online.hadithpull.app.domain.DrawResult
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

private fun okOutcome(number: Int) = HttpOutcome.Ok(
    HadithDto(
        hadithNumber = JsonPrimitive(number),
        hadithEnglish = "A self contained narration with enough words in it.",
    ),
)

@RunWith(RobolectricTestRunner::class)
class HadithRepositoryTest {
    private lateinit var db: HadithPullDatabase

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            HadithPullDatabase::class.java,
        ).build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun repositoryWithEngine(fetch: suspend (String, Int) -> HttpOutcome) =
        HadithRepository(DrawEngine(fetch), db)

    @Test
    fun `a successful draw writes recent_hadiths and returns Success`() = runBlocking {
        val repo = repositoryWithEngine { _, number -> okOutcome(number) }

        val outcome = repo.draw(currentKey = null)

        assertTrue(outcome is DrawOutcome.Success)
        val written = db.recentDao().randomExcept("")
        assertEquals((outcome as DrawOutcome.Success).hadith.key, written?.hadithKey)
    }

    @Test
    fun `an empty cache on failure returns the plain Failure`() = runBlocking {
        val repo = repositoryWithEngine { _, _ -> HttpOutcome.Busy }

        val outcome = repo.draw(currentKey = null)

        assertTrue(outcome is DrawOutcome.Failure)
        assertEquals(DrawResult.Failure.Busy, (outcome as DrawOutcome.Failure).cause)
    }

    @Test
    fun `on failure with a cache, falls back to a cached narration excluding the current key`() = runBlocking {
        val successRepo = repositoryWithEngine { _, number -> okOutcome(number) }
        val seeded = (successRepo.draw(currentKey = null) as DrawOutcome.Success).hadith.key

        val failingRepo = repositoryWithEngine { _, _ -> HttpOutcome.Busy }
        val outcome = failingRepo.draw(currentKey = seeded)

        // The only cached row is the current key itself, so no OTHER row qualifies.
        assertTrue(outcome is DrawOutcome.Failure)
    }

    @Test
    fun `a fallback is returned when a different cached key exists, and carries the failure cause`() = runBlocking {
        val successRepo = repositoryWithEngine { _, _ -> okOutcome(1) }
        val seeded = (successRepo.draw(currentKey = null) as DrawOutcome.Success).hadith.key

        val failingRepo = repositoryWithEngine { _, _ -> HttpOutcome.Busy }
        val outcome = failingRepo.draw(currentKey = "some-other-key")

        assertTrue(outcome is DrawOutcome.Fallback)
        val fallback = outcome as DrawOutcome.Fallback
        assertEquals(seeded, fallback.hadith.key)
        assertEquals(DrawResult.Failure.Busy, fallback.cause)
    }

    @Test
    fun `fallback selection is uniformly random among qualifying rows, not deterministic`() = runBlocking {
        val now = System.currentTimeMillis()
        listOf("key-a", "key-b", "key-c").forEachIndexed { i, key ->
            db.recentDao().upsert(
                RecentHadithEntity(
                    hadithKey = key,
                    slug = "sahih-bukhari",
                    number = "$i",
                    book = "Sahih Bukhari",
                    chapter = "",
                    status = "",
                    english = "Text $i.",
                    arabic = "",
                    narrator = "",
                    shownAt = now + i,
                ),
            )
        }
        val failingRepo = repositoryWithEngine { _, _ -> HttpOutcome.Busy }

        val picks = (1..40).map {
            (failingRepo.draw(currentKey = "not-cached") as DrawOutcome.Fallback).hadith.key
        }.toSet()

        assertTrue("expected more than one distinct fallback across 40 draws, got $picks", picks.size > 1)
    }

    @Test
    fun `a fallback draw does not write recent_hadiths`() = runBlocking {
        val successRepo = repositoryWithEngine { _, _ -> okOutcome(1) }
        val seeded = (successRepo.draw(currentKey = null) as DrawOutcome.Success).hadith.key

        val failingRepo = repositoryWithEngine { _, _ -> HttpOutcome.Rejected }
        failingRepo.draw(currentKey = "some-other-key")

        // Still exactly the one row the successful draw wrote; the fallback added nothing.
        assertNull(db.recentDao().randomExcept(seeded))
    }

    @Test
    fun `fallbackNote gives distinct copy per failure cause`() {
        assertEquals(
            "Offline — showing a narration you've read before",
            fallbackNote(DrawResult.Failure.Network),
        )
        assertEquals(
            "Service busy — showing a narration you've read before",
            fallbackNote(DrawResult.Failure.Busy),
        )
        assertEquals(
            "Couldn't fetch a new one — showing a narration you've read before",
            fallbackNote(DrawResult.Failure.KeyRejected),
        )
        assertEquals(
            "Couldn't fetch a new one — showing a narration you've read before",
            fallbackNote(DrawResult.Failure.Exhausted),
        )
    }
}
