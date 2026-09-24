package online.hadithpull.app.domain

import kotlin.random.Random
import kotlinx.coroutines.runBlocking
import online.hadithpull.app.data.HadithCollectionInfo
import online.hadithpull.app.data.HadithIndex
import online.hadithpull.app.data.HadithRecordDto
import online.hadithpull.app.data.HadithSource
import online.hadithpull.app.data.HadithSourceInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private class FixedRandom(private val value: Int) : Random() {
    override fun nextBits(bitCount: Int): Int = throw UnsupportedOperationException()
    override fun nextInt(until: Int): Int = value
}

private fun record(ref: String) = HadithRecordDto(ref = ref, english = "Narration $ref.", narrator = "")

/** A fake HadithSource: collection "a" (300 records over shards 0-1) and "b" (10 records, shard 0). */
private class FakeHadithSource : HadithSource {
    private val shardA0 = (0 until 250).map { record("a-$it") }
    private val shardA1 = (250 until 300).map { record("a-$it") }
    private val shardB0 = (0 until 10).map { record("b-$it") }

    override suspend fun index(): HadithIndex = HadithIndex(
        source = HadithSourceInfo("fake", "sha"),
        generatedAt = "now",
        collections = listOf(
            HadithCollectionInfo(id = "a", title = "Collection A", count = 300, shards = listOf(0, 1)),
            HadithCollectionInfo(id = "b", title = "Collection B", count = 10, shards = listOf(0)),
        ),
    )

    override suspend fun get(collection: String, shard: Int): List<HadithRecordDto> = when {
        collection == "a" && shard == 0 -> shardA0
        collection == "a" && shard == 1 -> shardA1
        collection == "b" && shard == 0 -> shardB0
        else -> error("no such shard $collection/$shard")
    }
}

/** §4/H6: uniform draw over every eligible hadith, then a redraw once if it repeats the current key. */
class DrawEngineTest {

    @Test
    fun `a draw at the last offset of the first shard lands on that record`() = runBlocking {
        val engine = DrawEngine(FakeHadithSource(), FixedRandom(249))
        val result = engine.draw(currentKey = null)
        assertTrue(result is DrawResult.Success)
        assertEquals("a:a-249", (result as DrawResult.Success).hadith.key)
    }

    @Test
    fun `a draw crossing the 250-record shard boundary lands on the second shard's first record`() = runBlocking {
        val engine = DrawEngine(FakeHadithSource(), FixedRandom(250))
        val result = engine.draw(currentKey = null)
        assertTrue(result is DrawResult.Success)
        assertEquals("a:a-250", (result as DrawResult.Success).hadith.key)
    }

    @Test
    fun `a draw past the first collection's count lands in the second collection`() = runBlocking {
        val engine = DrawEngine(FakeHadithSource(), FixedRandom(300))
        val result = engine.draw(currentKey = null)
        assertTrue(result is DrawResult.Success)
        assertEquals("b:b-0", (result as DrawResult.Success).hadith.key)
    }

    @Test
    fun `a draw landing on the current key redraws once and returns even if it repeats again`() = runBlocking {
        val engine = DrawEngine(FakeHadithSource(), FixedRandom(249))
        val result = engine.draw(currentKey = "a:a-249")
        assertTrue(result is DrawResult.Success)
        assertEquals("a:a-249", (result as DrawResult.Success).hadith.key)
    }

    @Test
    fun `a redraw that lands on a different record is used, and only one redraw happens`() = runBlocking {
        var calls = 0
        val random = object : Random() {
            override fun nextBits(bitCount: Int): Int = throw UnsupportedOperationException()
            override fun nextInt(until: Int): Int {
                calls++
                return if (calls == 1) 0 else 5
            }
        }
        val engine = DrawEngine(FakeHadithSource(), random)
        val result = engine.draw(currentKey = "a:a-0")
        assertTrue(result is DrawResult.Success)
        assertEquals("a:a-5", (result as DrawResult.Success).hadith.key)
        assertEquals(2, calls)
    }

    @Test
    fun `an asset IO error is a single Failure`() = runBlocking {
        val failingSource = object : HadithSource {
            override suspend fun index(): HadithIndex = throw java.io.IOException("missing asset")
            override suspend fun get(collection: String, shard: Int): List<HadithRecordDto> = error("unused")
        }
        val engine = DrawEngine(failingSource, FixedRandom(0))
        val result = engine.draw(currentKey = null)
        assertEquals(DrawResult.Failure, result)
    }
}
