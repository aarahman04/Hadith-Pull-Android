package online.hadithpull.app.domain.text

/**
 * Cross-reference patterns, verbatim from script.js:28-40. Inside these patterns `\s`
 * becomes `[WS]`; literal spaces stay literal.
 */
private val crossReferencePatterns: List<Regex> = listOf(
    """\bas (?:mentioned|stated|narrated|reported|described) (?:above|before|earlier|previously)\b""",
    """\bsame as (?:above|the (?:above|previous|preceding|foregoing))\b""",
    """\bsimilar to the (?:above|previous|preceding|one above)\b""",
    """\ba similar (?:hadith|narration|tradition|report|version)\b""",
    """\b(?:through|with) (?:a|another|a different) (?:other )?chain of (?:narrators|transmitters|authorities)\b""",
    """\bhas (?:already )?been (?:mentioned|narrated|reported|transmitted) (?:above|before|earlier)\b""",
    """\blike the (?:previous|preceding|foregoing) (?:hadith|narration|tradition)\b""",
    """\bto the same effect\b""",
    """\bthe same (?:meaning|as the preceding|as the previous)\b""",
    """\bsee[$WS]+(?:hadith[$WS]*)?(?:no\.?|number)?[$WS]*\d+""",
    """\bmentioned in the (?:previous|preceding) (?:hadith|narration)\b""",
).map { Regex(it, RegexOption.IGNORE_CASE) }

/** Exact port of script.js's isSelfContained; runs on hadithEnglish.jsTrim() before the narrator is stripped. */
fun isSelfContained(text: String): Boolean {
    val clean = text.jsTrim()
    if (clean.length < 15) return false
    if (clean.split(Regex("[$WS]+")).size < 3) return false
    if (clean.length < 300 && crossReferencePatterns.any { it.containsMatchIn(clean) }) return false
    return true
}

fun hasArabicWorthShowing(a: String): Boolean {
    val clean = a.jsTrim()
    return clean.length >= 12 && clean.split(Regex("[$WS]+")).size >= 3
}
