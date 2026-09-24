package online.hadithpull.app.domain

import java.io.IOException
import kotlin.random.Random
import kotlinx.serialization.SerializationException
import online.hadithpull.app.data.HadithSource
import online.hadithpull.app.data.toHadith

/** §4 (bundled offline dataset): the runtime draw can't miss, so there is no retry loop. */
sealed interface DrawResult {
    data class Success(val hadith: Hadith) : DrawResult
    data object Failure : DrawResult
}

/** Copy for [DrawResult.Failure] (an asset IO error — the only way a draw can fail now). */
const val DRAW_FAILURE_MESSAGE = "Couldn't load a narration. Please try again."

/**
 * §4: uniform draw over every eligible hadith in the bundled dataset (H6), not over collections.
 * If the drawn key equals [DrawEngine.draw]'s currentKey, redraw once (never loops).
 */
class DrawEngine(
    private val source: HadithSource,
    private val random: Random = Random,
) {
    suspend fun draw(currentKey: String?): DrawResult {
        val first = drawOnce() ?: return DrawResult.Failure
        if (first.key != currentKey) return DrawResult.Success(first)
        val second = drawOnce() ?: return DrawResult.Success(first)
        return DrawResult.Success(second)
    }

    private suspend fun drawOnce(): Hadith? = try {
        val index = source.index()
        val total = index.collections.sumOf { it.count }
        if (total <= 0) return null
        var r = random.nextInt(total)
        var found: Hadith? = null
        for (collection in index.collections) {
            if (r < collection.count) {
                val shardIndex = r / SHARD_SIZE
                val offset = r % SHARD_SIZE
                val shardNumber = collection.shards[shardIndex]
                val records = source.get(collection.id, shardNumber)
                found = records[offset].toHadith(collection.id, collection.title)
                break
            }
            r -= collection.count
        }
        found
    } catch (e: IOException) {
        null
    } catch (e: SerializationException) {
        null
    }

    private companion object {
        const val SHARD_SIZE = 250
    }
}
