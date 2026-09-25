package online.hadithpull.app.domain.text

import online.hadithpull.app.domain.Hadith

/** Exact port of the web's plainText(h): the Copy action, identical on the Reader and in Bookmarks. */
fun plainText(h: Hadith): String {
    val lines = mutableListOf(h.english)
    if (h.narrator.isNotEmpty()) lines.add(h.narrator)
    lines.add("")
    val status = h.primary?.grade.orEmpty()
    val statusSuffix = if (status.isNotEmpty()) " ($status)" else ""
    lines.add("${h.collectionTitle}, Hadith ${h.ref}$statusSuffix")
    if (h.chapter.isNotEmpty()) lines.add("Chapter: ${h.chapter}")
    return lines.joinToString("\n")
}

/** Exact port of shareText() from card.js/script.js, used by §3.4's share paths. */
fun shareText(h: Hadith): String {
    val excerpt = buildExcerpt(h.english, PAGE_EXCERPT) ?: h.english
    return "\"$excerpt\"\n\n${h.collectionTitle}, Hadith ${h.ref}\nhttps://hadithpull.online"
}
