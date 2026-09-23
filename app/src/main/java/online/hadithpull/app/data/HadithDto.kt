package online.hadithpull.app.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

@Serializable
data class HadithDto(
    val hadithNumber: JsonPrimitive? = null,
    val englishNarrator: String? = null,
    val hadithEnglish: String? = null,
    val hadithArabic: String? = null,
    val status: String? = null,
    val book: BookDto? = null,
    val chapter: ChapterDto? = null,
)

@Serializable
data class BookDto(val bookName: String? = null)

@Serializable
data class ChapterDto(val chapterEnglish: String? = null)

val hadithJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    isLenient = true
    explicitNulls = false
}

/**
 * §1.2 envelope parsing: the item is hadiths.data[0] if that array exists and is non-empty,
 * otherwise data[0] if that exists and is non-empty, otherwise null.
 * Throws SerializationException on malformed JSON.
 */
fun parseHadithItem(body: String): HadithDto? {
    val root = hadithJson.parseToJsonElement(body)
    val obj = root.jsonObject
    val fromHadiths = (obj["hadiths"] as? kotlinx.serialization.json.JsonObject)?.get("data")?.jsonArray
    val fromData = obj["data"]?.jsonArray
    val item = when {
        !fromHadiths.isNullOrEmpty() -> fromHadiths[0]
        !fromData.isNullOrEmpty() -> fromData[0]
        else -> null
    } ?: return null
    return hadithJson.decodeFromJsonElement<HadithDto>(item)
}
