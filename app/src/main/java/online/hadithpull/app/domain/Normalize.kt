package online.hadithpull.app.domain

import java.util.Locale
import online.hadithpull.app.data.HadithDto
import online.hadithpull.app.domain.text.WS
import online.hadithpull.app.domain.text.hasArabicWorthShowing
import online.hadithpull.app.domain.text.jsTrim
import online.hadithpull.app.domain.text.titleCase

private val leadingSeparatorRegex = Regex("^[$WS:.\\-—]+")
private val trailingNarratorPunctuationRegex = Regex("[$WS:]+$")

/** §1.3: exact port of displayHadith(dto) → Hadith. */
fun normalize(dto: HadithDto, slug: String): Hadith {
    val rawNarrator = dto.englishNarrator.orEmpty().jsTrim()
    var english = dto.hadithEnglish.orEmpty().jsTrim()
    if (rawNarrator.isNotEmpty() && english.lowercase(Locale.ROOT).startsWith(rawNarrator.lowercase(Locale.ROOT))) {
        english = english.substring(rawNarrator.length).replaceFirst(leadingSeparatorRegex, "")
    }
    val narrator = if (rawNarrator.isNotEmpty()) {
        "— " + rawNarrator.replace(trailingNarratorPunctuationRegex, "")
    } else {
        ""
    }
    val book = dto.book?.bookName?.takeIf { it.isNotEmpty() } ?: titleCase(slug)
    val chapter = dto.chapter?.chapterEnglish?.takeIf { it.isNotEmpty() }?.jsTrim() ?: ""
    val rawArabic = dto.hadithArabic.orEmpty().jsTrim()
    val arabic = if (hasArabicWorthShowing(rawArabic)) rawArabic else ""
    val status = dto.status.orEmpty().jsTrim()
    val number = dto.hadithNumber?.content.orEmpty()
    return Hadith(
        slug = slug,
        number = number,
        book = book,
        chapter = chapter,
        status = status,
        english = english,
        arabic = arabic,
        narrator = narrator,
    )
}
