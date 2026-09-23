package online.hadithpull.app.domain.text

import online.hadithpull.app.domain.Hadith

/** Exact port of the web's plainText(h): the Copy action, identical on the Reader and in Bookmarks. */
fun plainText(h: Hadith): String {
    val lines = mutableListOf(h.english)
    if (h.narrator.isNotEmpty()) lines.add(h.narrator)
    lines.add("")
    val statusSuffix = if (h.status.isNotEmpty()) " (${h.status})" else ""
    lines.add("${h.book}, Hadith ${h.number}$statusSuffix")
    if (h.chapter.isNotEmpty()) lines.add("Chapter: ${h.chapter}")
    return lines.joinToString("\n")
}

/** Exact port of shareText() from card.js/script.js, used by §3.4's share paths. */
fun shareText(h: Hadith): String {
    val excerpt = buildExcerpt(h.english, PAGE_EXCERPT) ?: h.english
    return "\"$excerpt\"\n\n— ${h.book}, Hadith ${h.number}\nhttps://hadithpull.online"
}

/** Splits on '-', uppercases the first character of each part, joins with a space. */
fun titleCase(slug: String): String =
    slug.split("-").joinToString(" ") { part ->
        if (part.isEmpty()) part else part.replaceFirstChar { it.uppercaseChar() }
    }
