package online.hadithpull.app.domain.text

private val newlineRegex = Regex("\n+")
private val sentenceSplitRegex = Regex("""[^.!?]+[.!?]+["'”’)\]]*[$WS]*""")
private val wsOnlyRegex = Regex("[$WS]")

private fun squash(s: String): String = s.replace(wsOnlyRegex, "")

/** Exact port of paragraphize(text); used only for the expanded/un-excerpted Reader body. */
fun paragraphize(text: String): List<String> {
    val existing = text.split(newlineRegex).map { it.jsTrim() }.filter { it.isNotEmpty() }

    if (existing.size > 1 || text.length < 600) return existing

    val sentences = sentenceSplitRegex.findAll(text).map { it.value }.toMutableList()
    if (sentences.isEmpty() || sentences.size < 4) return existing

    val consumed = sentences.joinToString("")
    if (!text.startsWith(consumed)) return existing

    val remainder = text.substring(consumed.length).jsTrim()
    if (remainder.isNotEmpty()) sentences.add(remainder)

    val paragraphs = mutableListOf<String>()
    var buffer = StringBuilder()
    for (sentence in sentences) {
        buffer.append(sentence)
        if (buffer.length >= 300) {
            paragraphs.add(buffer.toString().jsTrim())
            buffer = StringBuilder()
        }
    }

    val tail = buffer.toString().jsTrim()
    if (tail.isNotEmpty()) {
        if (tail.length < 120 && paragraphs.isNotEmpty()) {
            paragraphs[paragraphs.size - 1] = paragraphs.last() + " " + tail
        } else {
            paragraphs.add(tail)
        }
    }

    if (paragraphs.size < 2) return existing

    if (squash(paragraphs.joinToString(" ")) != squash(text)) return existing

    return paragraphs
}
