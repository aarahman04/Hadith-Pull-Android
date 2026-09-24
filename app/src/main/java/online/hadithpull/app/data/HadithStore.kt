package online.hadithpull.app.data

import android.content.res.AssetManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import online.hadithpull.app.domain.Grade
import online.hadithpull.app.domain.Grading
import online.hadithpull.app.domain.Hadith
import online.hadithpull.app.domain.PrimaryGrade

private val json = Json { ignoreUnknownKeys = true }

/** §3.3: index.json's shape. */
@Serializable
data class HadithIndex(
    val source: HadithSourceInfo,
    val generatedAt: String,
    val collections: List<HadithCollectionInfo>,
)

@Serializable
data class HadithSourceInfo(val repo: String, val sha: String)

@Serializable
data class HadithCollectionInfo(val id: String, val title: String, val count: Int, val shards: List<Int>)

/** §3.2: one record in a shard file, as written by the data pipeline. */
@Serializable
data class HadithRecordDto(
    val ref: String,
    val book: Int? = null,
    val inBook: Int? = null,
    val chapter: String = "",
    val narrator: String = "",
    val english: String = "",
    val arabic: String = "",
    val grades: List<HadithGradeDto> = emptyList(),
    val primary: HadithPrimaryGradeDto? = null,
    val url: String? = null,
)

@Serializable
data class HadithGradeDto(val by: String, val grade: String)

@Serializable
data class HadithPrimaryGradeDto(val grade: String, val by: String? = null, val cat: String, val consensus: Boolean = false)

/** H8: the pipeline already computes the category; this just maps its string to the enum. */
private fun parseGrading(cat: String): Grading = when (cat.lowercase()) {
    "sahih" -> Grading.SAHIH
    "hasan" -> Grading.HASAN
    "daif" -> Grading.DAIF
    else -> Grading.UNKNOWN
}

fun HadithRecordDto.toHadith(collectionId: String, collectionTitle: String): Hadith = Hadith(
    collection = collectionId,
    collectionTitle = collectionTitle,
    ref = ref,
    book = book,
    inBook = inBook,
    chapter = chapter,
    english = english,
    arabic = arabic,
    narrator = narrator,
    grades = grades.map { Grade(by = it.by, grade = it.grade) },
    primary = primary?.let {
        PrimaryGrade(grade = it.grade, by = it.by, cat = parseGrading(it.cat), consensus = it.consensus)
    },
    sunnahUrl = url,
)

private const val ASSET_BASE = "hadith/v1"

/** DrawEngine's dependency, so tests can supply a fake without an AssetManager. */
interface HadithSource {
    suspend fun index(): HadithIndex
    suspend fun get(collection: String, shard: Int): List<HadithRecordDto>
}

/**
 * H2/H12: reads the bundled offline dataset from assets/hadith/v1, no network. index.json is read
 * once and cached; shard files are kept in a 2-entry LRU cache.
 */
class HadithStore(private val assets: AssetManager) : HadithSource {
    private var cachedIndex: HadithIndex? = null
    private val shardCache = object : LinkedHashMap<Pair<String, Int>, List<HadithRecordDto>>(3, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Pair<String, Int>, List<HadithRecordDto>>): Boolean =
            size > 2
    }

    override suspend fun index(): HadithIndex = withContext(Dispatchers.IO) {
        cachedIndex ?: readAsset("$ASSET_BASE/index.json").let { text ->
            json.decodeFromString<HadithIndex>(text).also { cachedIndex = it }
        }
    }

    override suspend fun get(collection: String, shard: Int): List<HadithRecordDto> = withContext(Dispatchers.IO) {
        val key = collection to shard
        synchronized(shardCache) { shardCache[key] }?.let { return@withContext it }
        val text = readAsset("$ASSET_BASE/$collection/$shard.json")
        val records = json.decodeFromString<List<HadithRecordDto>>(text)
        synchronized(shardCache) { shardCache[key] = records }
        records
    }

    private fun readAsset(path: String): String =
        assets.open(path).bufferedReader().use { it.readText() }
}
