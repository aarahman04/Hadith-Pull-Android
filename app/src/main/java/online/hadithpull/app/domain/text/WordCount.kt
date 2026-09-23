package online.hadithpull.app.domain.text

/** N in the expand-toggle label: english.jsTrim().split([WS]+).size, 0 for an empty/blank string. */
fun wordCount(text: String): Int {
    val clean = text.jsTrim()
    if (clean.isEmpty()) return 0
    return clean.split(Regex("[$WS]+")).size
}
