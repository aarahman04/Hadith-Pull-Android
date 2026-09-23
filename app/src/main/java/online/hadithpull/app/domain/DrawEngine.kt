package online.hadithpull.app.domain

import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.random.Random
import kotlinx.serialization.SerializationException
import online.hadithpull.app.data.HttpOutcome
import online.hadithpull.app.domain.text.isSelfContained
import online.hadithpull.app.domain.text.jsTrim

/** P1 revised: draw pool = all 9 collections (parity); slug → total item count. */
data class Collection(val slug: String, val count: Int)

val COLLECTIONS = listOf(
    Collection("sahih-bukhari", 7563),
    Collection("sahih-muslim", 3033),
    Collection("al-tirmidhi", 3956),
    Collection("abu-dawood", 5274),
    Collection("ibn-e-majah", 4341),
    Collection("sunan-nasai", 5758),
    Collection("mishkat", 6294),
    Collection("musnad-ahmad", 28199),
    Collection("al-silsila-sahiha", 4035),
)

private const val MAX_RETRIES = 10

sealed interface DrawResult {
    data class Success(val hadith: Hadith) : DrawResult

    sealed interface Failure : DrawResult {
        data object Network : Failure
        data object KeyRejected : Failure
        data object Busy : Failure
        data object Exhausted : Failure
    }
}

/**
 * P5, per-process demotion: a slug with >= 3 consecutive misses is left out of the pool for the
 * rest of the process, but only while >= 1 other slug is still healthy. The count resets on
 * success. There is no revival mid-process once a slug is demoted.
 */
class ProcessHealth(private val totalSlugs: Int = COLLECTIONS.size) {
    private val consecutiveMisses = mutableMapOf<String, Int>()
    private val demotedSlugs = mutableSetOf<String>()

    val demoted: Set<String> get() = demotedSlugs

    fun onMiss(slug: String) {
        val count = (consecutiveMisses[slug] ?: 0) + 1
        consecutiveMisses[slug] = count
        if (count >= 3 && demotedSlugs.size < totalSlugs - 1) {
            demotedSlugs += slug
        }
    }

    fun onSuccess(slug: String) {
        consecutiveMisses[slug] = 0
    }
}

/** §1.5: exact port of getHadith() + retry(), revised by P1/P2/P3/P5. */
class DrawEngine(
    private val fetch: suspend (slug: String, number: Int) -> HttpOutcome,
    private val random: Random = Random,
    private val processHealth: ProcessHealth = ProcessHealth(),
) {
    suspend fun draw(): DrawResult {
        val tried = mutableSetOf<String>()
        var retryCount = 0
        while (true) {
            val pool = COLLECTIONS.filter { it.slug !in processHealth.demoted }
            var candidates = pool.filter { it.slug !in tried }
            if (candidates.isEmpty()) {
                tried.clear()
                candidates = pool
            }
            val collection = candidates[random.nextInt(candidates.size)]
            val number = random.nextInt(1, collection.count + 1)

            val outcome = try {
                fetch(collection.slug, number)
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                return DrawResult.Failure.Network
            } catch (e: SerializationException) {
                return DrawResult.Failure.Network
            }

            when (outcome) {
                is HttpOutcome.Rejected -> return DrawResult.Failure.KeyRejected
                is HttpOutcome.Busy -> return DrawResult.Failure.Busy
                is HttpOutcome.NotOk -> {
                    tried += collection.slug
                    processHealth.onMiss(collection.slug)
                }
                is HttpOutcome.Ok -> {
                    val item = outcome.item
                    val english = item?.hadithEnglish.orEmpty().jsTrim()
                    if (item != null && english.isNotEmpty() && isSelfContained(english)) {
                        processHealth.onSuccess(collection.slug)
                        return DrawResult.Success(normalize(item, collection.slug))
                    }
                    tried += collection.slug
                    processHealth.onMiss(collection.slug)
                }
            }

            retryCount++
            if (retryCount > MAX_RETRIES) return DrawResult.Failure.Exhausted
        }
    }
}
