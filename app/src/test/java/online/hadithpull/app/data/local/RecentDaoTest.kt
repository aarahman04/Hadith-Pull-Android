package online.hadithpull.app.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RecentDaoTest {
    private lateinit var db: HadithPullDatabase
    private lateinit var dao: RecentDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            HadithPullDatabase::class.java,
        ).build()
        dao = db.recentDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun entity(key: String, shownAt: Long) = RecentHadithEntity(
        hadithKey = key,
        slug = "sahih-bukhari",
        number = key,
        book = "Sahih Bukhari",
        chapter = "",
        status = "",
        english = "Text.",
        arabic = "",
        narrator = "",
        shownAt = shownAt,
    )

    @Test
    fun `a 31st distinct narration evicts the oldest rather than growing past 30`() = runBlocking {
        for (i in 1..31) {
            dao.upsert(entity(key = "key-$i", shownAt = 1_000L + i))
        }
        dao.trimToNewest30()

        assertEquals(30, dao.count())
        assertTrue("key-1" !in dao.allKeys()) // oldest shownAt, evicted
        assertTrue("key-31" in dao.allKeys()) // newest shownAt, kept
    }

    @Test
    fun `re-inserting a key already present updates shownAt instead of duplicating`() = runBlocking {
        dao.upsert(entity(key = "same-key", shownAt = 100L))
        dao.upsert(entity(key = "same-key", shownAt = 200L))

        assertEquals(1, dao.count())
        assertEquals(200L, dao.shownAtOf("same-key"))
    }
}
