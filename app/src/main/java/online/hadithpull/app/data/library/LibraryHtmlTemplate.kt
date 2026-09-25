package online.hadithpull.app.data.library

import online.hadithpull.app.data.ExportFolder

/** R1.8: the static shell for an exported "Hadith Pull library" file -- readable and searchable
 * in any browser with JavaScript off, and the one place the embedded JSON id/placeholders live. */
object LibraryHtmlTemplate {

    fun htmlEscape(s: String): String = s
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;")

    val SHELL = """
        <!doctype html><html><head><meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>__TITLE__</title>
        <style>
          :root { color-scheme: light dark; }
          body { font-family: Georgia, 'Cormorant Garamond', serif; max-width: 720px; margin: 0 auto; padding: 24px 16px 64px; background: #F6F2EA; color: #1C1917; }
          @media (prefers-color-scheme: dark) { body { background: #0A0F14; color: #ECE9E4; } a { color: #5EEAD4; } }
          h1 { font-weight: 500; } h2 { font-size: 1.1rem; opacity: 0.7; margin-top: 2.5rem; border-bottom: 1px solid currentColor; padding-bottom: 0.4rem; }
          input#search { width: 100%; box-sizing: border-box; font-size: 1rem; padding: 10px 14px; margin: 16px 0; border-radius: 999px; border: 1px solid currentColor; background: transparent; color: inherit; }
          article { margin: 1.4rem 0; padding-bottom: 1.4rem; border-bottom: 1px solid rgba(128,128,128,0.25); }
          .ref { font-family: sans-serif; font-size: 0.85rem; opacity: 0.65; }
          .narrator { font-style: italic; opacity: 0.8; }
          article[hidden] { display: none; }
        </style></head>
        <body>
        <h1>__TITLE__</h1>
        <input id="search" type="search" placeholder="Search your saved narrations…">
        __BODY__
        <script type="application/json" id="hadith-pull-library">__JSON__</script>
        <script>
        (function() {
          var input = document.getElementById('search');
          var articles = Array.prototype.slice.call(document.querySelectorAll('article'));
          input.addEventListener('input', function() {
            var q = input.value.trim().toLowerCase();
            articles.forEach(function(a) { a.hidden = q.length > 0 && a.textContent.toLowerCase().indexOf(q) === -1; });
          });
        })();
        </script>
        </body></html>
    """.trimIndent()

    /** Folders in the order `exportSnapshot` returns them; every text field is HTML-escaped,
     * including folder names, which are user-typed and would otherwise be an XSS vector for
     * whoever opens the file. */
    fun renderFoldersHtml(folders: List<ExportFolder>): String = buildString {
        folders.forEach { folder ->
            append("<h2>").append(htmlEscape(folder.name)).append("</h2>\n")
            folder.items.forEach { item ->
                val hadith = item.hadith
                append("<article>\n")
                append("<p class=\"english\">").append(htmlEscape(hadith.english)).append("</p>\n")
                if (hadith.arabic.isNotEmpty()) {
                    append("<details><summary>Arabic</summary><p dir=\"rtl\" lang=\"ar\">")
                        .append(htmlEscape(hadith.arabic)).append("</p></details>\n")
                }
                if (hadith.narrator.isNotEmpty()) {
                    append("<p class=\"narrator\">").append(htmlEscape(hadith.narrator)).append("</p>\n")
                }
                val refText = buildString {
                    append(hadith.collectionTitle).append(" · Hadith ").append(hadith.ref)
                    if (hadith.chapter.isNotEmpty()) append(" · ").append(hadith.chapter)
                    hadith.primary?.grade?.let { append(" · ").append(it) }
                }
                append("<p class=\"ref\">").append(htmlEscape(refText)).append("</p>\n")
                hadith.sunnahUrl?.let { url ->
                    append("<p><a href=\"").append(htmlEscape(url))
                        .append("\" target=\"_blank\" rel=\"noopener\">View on Sunnah.com &rarr;</a></p>\n")
                }
                append("</article>\n")
            }
        }
    }
}
