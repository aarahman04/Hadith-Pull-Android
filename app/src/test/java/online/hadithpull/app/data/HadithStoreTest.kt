package online.hadithpull.app.data

import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * §4: loads EVERY real shard bundled under src/main/assets/hadith/v1 and checks the whole dataset,
 * not a sample -- this is the test that would catch a bad pipeline run or a bad copy into assets.
 */
@RunWith(RobolectricTestRunner::class)
class HadithStoreTest {
    private val store = HadithStore(ApplicationProvider.getApplicationContext<android.content.Context>().assets)
    private val validCats = setOf("sahih", "hasan", "daif", "unknown")

    @Test
    fun `every collection's shard count matches index_json's declared count, and keys are unique dataset-wide`() = runBlocking {
        val index = store.index()
        assertTrue("index.json lists no collections", index.collections.isNotEmpty())

        val allKeys = mutableSetOf<String>()
        for (collection in index.collections) {
            var actualCount = 0
            for (shard in collection.shards) {
                val records = store.get(collection.id, shard)
                actualCount += records.size
                records.forEach { record ->
                    val key = "${collection.id}:${record.ref}"
                    assertTrue("duplicate key $key", allKeys.add(key))
                }
            }
            assertEquals("count mismatch for ${collection.id}", collection.count, actualCount)
        }
    }

    @Test
    fun `every primary_cat decodes to a valid Grading value, and no record has empty English`() = runBlocking {
        val index = store.index()
        for (collection in index.collections) {
            for (shard in collection.shards) {
                val records = store.get(collection.id, shard)
                records.forEach { record ->
                    assertTrue(
                        "${collection.id}:${record.ref} has empty English",
                        record.english.isNotEmpty(),
                    )
                    val primary = record.primary
                    if (primary != null) {
                        assertTrue(
                            "${collection.id}:${record.ref} has an unrecognised primary.cat: ${primary.cat}",
                            primary.cat.lowercase() in validCats,
                        )
                    }
                }
            }
        }
    }
}
