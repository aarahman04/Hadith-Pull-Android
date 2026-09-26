package online.hadithpull.app.domain

import java.io.IOException
import kotlin.random.Random
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerializationException
import online.hadithpull.app.data.HadithCollectionInfo
import online.hadithpull.app.data.HadithSource
import online.hadithpull.app.data.HadithIndex
import online.hadithpull.app.data.parseGrading
import online.hadithpull.app.data.toHadith
import online.hadithpull.app.data.prefs.HadithGradeFilter

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
    private data class Candidate(
        val collection: HadithCollectionInfo,
        val shard: Int,
        val offset: Int,
    )

    private val candidateMutex = Mutex()
    private var gradeCandidates: Map<HadithGradeFilter, List<Candidate>>? = null

    suspend fun draw(currentKey: String?, filter: HadithGradeFilter = HadithGradeFilter.ALL_GRADES): DrawResult {
        val first = drawOnce(filter) ?: return DrawResult.Failure
        if (first.key != currentKey) return DrawResult.Success(first)
        val second = drawOnce(filter) ?: return DrawResult.Success(first)
        return DrawResult.Success(second)
    }

    private suspend fun drawOnce(filter: HadithGradeFilter): Hadith? = try {
        val index = source.index()
        if (filter != HadithGradeFilter.ALL_GRADES) {
            val candidates = gradeCandidates ?: candidateMutex.withLock {
                gradeCandidates ?: buildGradeCandidates(index).also { gradeCandidates = it }
            }
            val pool = candidates[filter].orEmpty()
            if (pool.isEmpty()) return null
            val candidate = pool[random.nextInt(pool.size)]
            return source.get(candidate.collection.id, candidate.shard)[candidate.offset]
                .toHadith(candidate.collection.id, candidate.collection.title)
        }
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

    private suspend fun buildGradeCandidates(index: HadithIndex): Map<HadithGradeFilter, List<Candidate>> {
        val sahih = mutableListOf<Candidate>()
        val other = mutableListOf<Candidate>()
        for (collection in index.collections) {
            for (shard in collection.shards) {
                val records = source.get(collection.id, shard)
                records.forEachIndexed { offset, record ->
                    val grade = record.primary?.let { parseGrading(it.cat) }
                    val candidate = Candidate(collection, shard, offset)
                    when {
                        grade == Grading.SAHIH -> sahih += candidate
                        record.primary != null -> other += candidate
                    }
                }
            }
        }
        return mapOf(
            HadithGradeFilter.SAHIH_ONLY to sahih,
            HadithGradeFilter.OTHER_GRADES to other,
        )
    }

    private companion object {
        const val SHARD_SIZE = 250
    }
}
