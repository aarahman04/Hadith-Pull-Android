package online.hadithpull.app.domain

import kotlinx.serialization.Serializable

/** §4 (bundled offline dataset): the record shape read from the bundled dataset shards. */
@Serializable
data class Hadith(
    val collection: String,
    val collectionTitle: String,
    val ref: String,
    val book: Int?,
    val inBook: Int?,
    val chapter: String,
    val english: String,
    val arabic: String,
    val narrator: String,
    val grades: List<Grade>,
    val primary: PrimaryGrade?,
    val sunnahUrl: String?,
) {
    val key: String get() = "$collection:$ref"
}

@Serializable
data class Grade(val by: String, val grade: String)

@Serializable
data class PrimaryGrade(val grade: String, val by: String?, val cat: Grading, val consensus: Boolean)

enum class Grading { SAHIH, HASAN, DAIF, UNKNOWN }
