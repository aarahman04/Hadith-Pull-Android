package online.hadithpull.app.card

import online.hadithpull.app.data.prefs.ArabicScript
import online.hadithpull.app.domain.Grading
import online.hadithpull.app.domain.Hadith
import online.hadithpull.app.domain.text.CARD_EXCERPT
import online.hadithpull.app.domain.text.buildExcerpt

enum class CardTheme { LIGHT, DARK }

data class CardInput(
    val english: String,
    val arabic: String,
    val narrator: String,
    val book: String,
    val number: String,
    val status: String,
    val statusCat: Grading? = null,
    val site: String = "hadithpull.online",
    val script: ArabicScript,
    val excerpt: Boolean,
) {
    companion object {
        /** §3.2: one mapping for both the Reader and Bookmarks (S1), so a saved card matches the drawn one. */
        fun from(h: Hadith, includeArabic: Boolean, script: ArabicScript): CardInput {
            val cardExcerpt = buildExcerpt(h.english, CARD_EXCERPT)
            return CardInput(
                english = cardExcerpt ?: h.english,
                arabic = if (includeArabic) h.arabic else "",
                narrator = h.narrator,
                book = h.collectionTitle,
                number = h.ref,
                status = h.primary?.grade.orEmpty(),
                statusCat = h.primary?.cat,
                script = script,
                excerpt = cardExcerpt != null,
            )
        }
    }
}
