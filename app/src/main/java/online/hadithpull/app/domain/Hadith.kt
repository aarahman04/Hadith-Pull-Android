package online.hadithpull.app.domain

import kotlinx.serialization.Serializable

@Serializable
data class Hadith(
    val slug: String,
    val number: String,
    val book: String,
    val chapter: String,
    val status: String,
    val english: String,
    val arabic: String,
    val narrator: String,
) {
    val key: String get() = "$slug-$number"
}

enum class Grading { SAHIH, HASAN, DAIF, UNKNOWN }

private val gradingPunctuationRegex = Regex("[`'’]")

/** P4 grading rule: strip backtick/apostrophe/right-quote, then match the cleaned string. */
fun grading(status: String): Grading {
    val s = status.lowercase().replace(gradingPunctuationRegex, "")
    return when (s) {
        "sahih" -> Grading.SAHIH
        "hasan" -> Grading.HASAN
        "daif", "daeef", "weak" -> Grading.DAIF
        else -> Grading.UNKNOWN
    }
}
