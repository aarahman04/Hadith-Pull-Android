package online.hadithpull.app.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import online.hadithpull.app.data.library.LibraryExportDocument
import online.hadithpull.app.data.library.LibraryExportFolder
import online.hadithpull.app.data.library.LibraryExportItem
import online.hadithpull.app.data.library.applyImport
import online.hadithpull.app.data.local.HadithPullDatabase
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

@RunWith(RobolectricTestRunner::class)
class BookmarkRepositoryImportTest {
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

    private fun hadith(key: String) = Hadith(
        collection = key.substringBefore(":"),
        collectionTitle = "Sahih al-Bukhari",
        ref = key.substringAfter(":"),
        book = 1,
        inBook = 1,
        chapter = "",
        english = "Text for $key.",
        arabic = "",
        narrator = "",
        grades = emptyList(),
        primary = PrimaryGrade(grade = "Sahih", by = "Al-Albani", cat = Grading.SAHIH, consensus = false),
        sunnahUrl = null,
    )

    /** A resolver that answers `resolve(key)` from a fixed map -- `HadithSource.resolve` is a
     * suspend extension, so this fakes it by wrapping applyImport's own HadithSource parameter
     * with a source whose get()/index() are shaped to make `resolve` find exactly these keys. */
    private fun sourceKnowing(vararg keys: String): HadithSource {
        val byCollection = keys.toList().groupBy { it.substringBefore(":") }
        return object : HadithSource {
            override suspend fun index(): HadithIndex = HadithIndex(
                source = HadithSourceInfo("fake", "0"),
                generatedAt = "0",
                collections = byCollection.keys.map { HadithCollectionInfo(it, "Sahih al-Bukhari", byCollection[it]!!.size, listOf(0)) },
            )

            override suspend fun get(collection: String, shard: Int): List<HadithRecordDto> =
                (byCollection[collection] ?: emptyList()).map { key ->
                    HadithRecordDto(ref = key.substringAfter(":"), english = "Text for $key.")
                }
        }
    }

    @Test
    fun `ensureFolder matches an existing folder case-insensitively and creates a new one otherwise`() = runBlocking {
        val created = (repository.createFolder("Ramadan") as CreateFolderResult.Created).folder

        val matchedId = repository.ensureFolder("ramadan")
        val newId = repository.ensureFolder("Hajj")

        assertEquals(created.id, matchedId)
        assertTrue(newId != created.id)
        assertEquals(2, repository.folders().first().size)
    }

    @Test
    fun `applyImport reports Added, AlreadySaved and NotFound counts, and never removes an existing bookmark`() = runBlocking {
        val existingFolder = (repository.createFolder("Ramadan") as CreateFolderResult.Created).folder
        val alreadySavedHadith = hadith("bukhari:1")
        repository.ensureSaved(existingFolder.id, alreadySavedHadith)

        val doc = LibraryExportDocument(
            exportedAt = 0,
            folders = listOf(
                LibraryExportFolder(
                    "Ramadan", // matches the existing folder case-insensitively (same case here)
                    listOf(LibraryExportItem("bukhari:1"), LibraryExportItem("bukhari:2")),
                ),
                LibraryExportFolder("Hajj", listOf(LibraryExportItem("bukhari:999999"))),
            ),
        )
        val source = sourceKnowing("bukhari:1", "bukhari:2")

        val summary = repository.applyImport(doc, source)

        assertEquals(1, summary.added) // bukhari:2 is new
        assertEquals(1, summary.alreadySaved) // bukhari:1 was already saved
        assertEquals(1, summary.notFound) // bukhari:999999 isn't resolvable
        assertEquals(2, summary.foldersTouched) // Ramadan (merged) + Hajj (created)

        val ramadanItems = repository.items(existingFolder.id).first()
        assertEquals(2, ramadanItems.size) // bukhari:1 kept (not duplicated), bukhari:2 added
        assertTrue(ramadanItems.any { it.hadithKey == "bukhari:1" })
        assertTrue(ramadanItems.any { it.hadithKey == "bukhari:2" })
    }

    @Test
    fun `applyImport merges into an existing folder rather than creating a duplicate`() = runBlocking {
        repository.createFolder("Ramadan")
        val doc = LibraryExportDocument(
            exportedAt = 0,
            folders = listOf(LibraryExportFolder("RAMADAN", listOf(LibraryExportItem("bukhari:1")))),
        )
        repository.applyImport(doc, sourceKnowing("bukhari:1"))

        assertEquals(1, repository.folders().first().size)
    }
}
