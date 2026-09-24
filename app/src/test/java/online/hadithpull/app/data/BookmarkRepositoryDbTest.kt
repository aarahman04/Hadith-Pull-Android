package online.hadithpull.app.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import online.hadithpull.app.data.local.HadithPullDatabase
import online.hadithpull.app.domain.Grade
import online.hadithpull.app.domain.Grading
import online.hadithpull.app.domain.Hadith
import online.hadithpull.app.domain.PrimaryGrade
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Room-backed tests for the repository behaviour that pure functions can't exercise. */
@RunWith(RobolectricTestRunner::class)
class BookmarkRepositoryDbTest {
    private lateinit var db: HadithPullDatabase
    private lateinit var repository: BookmarkRepository

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            HadithPullDatabase::class.java,
        ).build()
        repository = BookmarkRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun hadith(ref: String = "1") = Hadith(
        collection = "bukhari",
        collectionTitle = "Sahih al-Bukhari",
        ref = ref,
        book = 1,
        inBook = 1,
        chapter = "",
        english = "Text.",
        arabic = "",
        narrator = "",
        grades = listOf(Grade(by = "Al-Albani", grade = "Sahih")),
        primary = PrimaryGrade(grade = "Sahih", by = "Al-Albani", cat = Grading.SAHIH, consensus = false),
        sunnahUrl = "https://sunnah.com/bukhari:1",
    )

    @Test
    fun `moveItem into a folder that already holds the same key deletes the moved row instead of duplicating`() = runBlocking {
        val folderA = (repository.createFolder("A") as CreateFolderResult.Created).folder
        val folderB = (repository.createFolder("B") as CreateFolderResult.Created).folder
        val h = hadith()

        repository.ensureSaved(folderA.id, h)
        repository.ensureSaved(folderB.id, h)
        val itemInA = repository.items(folderA.id).first().single()

        repository.moveItem(itemInA.id, folderB.id)

        assertTrue(repository.items(folderA.id).first().isEmpty())
        assertEquals(1, repository.items(folderB.id).first().size)
    }

    @Test
    fun `moveItem into a folder with no clash actually moves the row`() = runBlocking {
        val folderA = (repository.createFolder("A") as CreateFolderResult.Created).folder
        val folderB = (repository.createFolder("B") as CreateFolderResult.Created).folder
        val h = hadith()

        repository.ensureSaved(folderA.id, h)
        val itemInA = repository.items(folderA.id).first().single()

        repository.moveItem(itemInA.id, folderB.id)

        assertTrue(repository.items(folderA.id).first().isEmpty())
        assertEquals(h.key, repository.items(folderB.id).first().single().hadithKey)
    }

    @Test
    fun `renameFolder to its own current name is Renamed, not Duplicate`() = runBlocking {
        val folder = (repository.createFolder("Ramadan") as CreateFolderResult.Created).folder

        val result = repository.renameFolder(folder.id, "Ramadan")

        assertTrue(result is RenameFolderResult.Renamed)
    }

    @Test
    fun `renameFolder to a case-different form of its own name is Renamed, not Duplicate`() = runBlocking {
        val folder = (repository.createFolder("Ramadan") as CreateFolderResult.Created).folder

        val result = repository.renameFolder(folder.id, "ramadan")

        assertTrue(result is RenameFolderResult.Renamed)
        assertEquals("ramadan", (result as RenameFolderResult.Renamed).folder.name)
    }

    @Test
    fun `renameFolder to a different folder's name (any case) is Duplicate`() = runBlocking {
        repository.createFolder("Ramadan")
        val other = (repository.createFolder("Hajj") as CreateFolderResult.Created).folder

        val result = repository.renameFolder(other.id, "ramadan")

        assertTrue(result is RenameFolderResult.Duplicate)
    }

    @Test
    fun `H9 the hadithJson snapshot round-trips a saved bookmark exactly`() = runBlocking {
        val folder = (repository.createFolder("Ramadan") as CreateFolderResult.Created).folder
        val h = hadith()

        repository.ensureSaved(folder.id, h)
        val saved = repository.items(folder.id).first().single()

        assertEquals(h, saved.toHadith())
    }
}
