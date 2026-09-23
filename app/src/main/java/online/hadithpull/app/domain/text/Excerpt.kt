package online.hadithpull.app.domain.text

data class Budget(val whole: Int, val min: Int, val cap: Int, val worthHiding: Int)

val PAGE_EXCERPT = Budget(whole = 900, min = 480, cap = 760, worthHiding = 200)
val CARD_EXCERPT = Budget(whole = 420, min = 240, cap = 400, worthHiding = 100)

/** Shared sentence regex; no match at all is treated as JS null. */
private val sentenceRegex = Regex("""[^.!?]+[.!?]+["'”’)\]]*[$WS]*""")

private val trailingWsPunctRegex = Regex("[$WS,;:]+$")

/** Exact port of buildExcerpt(text, budget). */
fun buildExcerpt(text: String, budget: Budget): String? {
    if (text.length <= budget.whole) return null

    var excerpt = StringBuilder()
    var matched = false
    for (match in sentenceRegex.findAll(text)) {
        matched = true
        excerpt.append(match.value)
        if (excerpt.length >= budget.min) break
    }
    var result = if (matched) excerpt.toString().jsTrim() else ""

    if (result.isEmpty() || result.length > budget.cap) {
        var cut = text.substring(0, budget.cap)
        val i = cut.lastIndexOf(' ')
        cut = if (i >= 0) cut.substring(0, i) else cut.dropLast(1)
        result = cut.replace(trailingWsPunctRegex, "") + "…"
    }

    val saved = text.length - result.length
    if (saved < budget.worthHiding || result.length > text.length * 0.8) return null
    return result
}

/** Exact port of bookmarkExcerpt(english, 320) from bookmarks.js / §2.4. */
fun bookmarkExcerpt(text: String, limit: Int = 320): String {
    if (text.length <= limit) return text
    val cut = text.substring(0, limit)
    val i = cut.lastIndexOf(' ')
    val short = if (i > 40) cut.substring(0, i) else cut
    return short.replace(Regex("[$WS,;:.]+$"), "") + "…"
}
