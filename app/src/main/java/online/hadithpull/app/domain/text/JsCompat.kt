package online.hadithpull.app.domain.text

/** The JS `\s` set, written as a character-class body. */
const val WS = "\\s\\u00A0\\u1680\\u2000-\\u200A\\u2028\\u2029\\u202F\\u205F\\u3000\\uFEFF"

private val wsTrimRegex = Regex("^[$WS]+|[$WS]+$")

fun String.jsTrim(): String = replace(wsTrimRegex, "")
