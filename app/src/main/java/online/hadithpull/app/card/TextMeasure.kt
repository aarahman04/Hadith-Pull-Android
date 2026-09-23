package online.hadithpull.app.card

import kotlin.math.floor
import online.hadithpull.app.data.prefs.ArabicScript
import online.hadithpull.app.domain.text.WS
import online.hadithpull.app.domain.text.jsTrim

/** A CSS `"<weight> <size>px <family>"` font, reduced to the fields that matter for measuring. */
data class CardFont(val family: String, val weight: Int, val size: Float)

/**
 * Every text measurement in the card renderer goes through this, so `wrap`/`fitBlock`/
 * `ellipsize`/`trackedWidth` are unit-testable with a fixed-advance fake (§3.1).
 */
fun interface TextMeasure {
    fun width(text: String, font: CardFont): Float
}

/** Font families used by the card, distinct from the Reader's Compose FontFamily names. */
const val FONT_INTER = "Inter"
const val FONT_CORMORANT = "Cormorant"
const val FONT_AMIRI = "Amiri"
const val FONT_SCHEHERAZADE = "ScheherazadeNew"
const val FONT_NOTO_NASKH = "NotoNaskhArabic"

/** §3.1 Arabic faces table. */
data class CardArabicFace(val family: String, val weight: Int, val min: Int, val max: Int, val lineHeight: Float)

fun cardArabicFace(script: ArabicScript): CardArabicFace = when (script) {
    ArabicScript.NASKH -> CardArabicFace(FONT_AMIRI, 400, 28, 50, 1.9f)
    ArabicScript.CLEAR -> CardArabicFace(FONT_SCHEHERAZADE, 400, 31, 56, 2.05f)
    ArabicScript.BOLD -> CardArabicFace(FONT_NOTO_NASKH, 700, 27, 48, 2.05f)
}

/** Splits `text` into its Unicode code points (the JS `Array.from`). */
fun codePointStrings(text: String): List<String> {
    val result = mutableListOf<String>()
    var i = 0
    while (i < text.length) {
        val cp = text.codePointAt(i)
        val charCount = Character.charCount(cp)
        result.add(text.substring(i, i + charCount))
        i += charCount
    }
    return result
}

/** Exact port of `trackedWidth`: sum each code point's width + spacing, minus one trailing spacing. */
fun trackedWidth(measure: TextMeasure, font: CardFont, text: String, spacing: Float): Float {
    val codePoints = codePointStrings(text)
    if (codePoints.isEmpty()) return 0f
    var total = 0f
    for (cp in codePoints) total += measure.width(cp, font) + spacing
    return total - spacing
}

/** Exact port of `wrap`: greedy fill per paragraph, blank paragraphs produce no line. */
fun wrap(measure: TextMeasure, font: CardFont, text: String, maxWidth: Float): List<String> {
    val lines = mutableListOf<String>()
    for (paragraph in text.split("\n")) {
        val words = paragraph.split(Regex("[$WS]+")).filter { it.isNotEmpty() }
        if (words.isEmpty()) continue

        var line = words[0]
        for (i in 1 until words.size) {
            val candidate = "$line ${words[i]}"
            if (measure.width(candidate, font) <= maxWidth) {
                line = candidate
            } else {
                lines.add(line)
                line = words[i]
            }
        }
        lines.add(line)
    }
    return lines
}

data class CardBlock(
    val fontSize: Int,
    val lines: List<String>,
    val height: Float,
    val lineHeight: Float,
    val font: CardFont,
    val truncated: Boolean,
)

private val trailingWsPunctRegex = Regex("[$WS,;:.]+$")

/** Exact port of `fitBlock`: linear descent (D6), then ellipsis-truncate at `min` if nothing fit. */
fun fitBlock(
    measure: TextMeasure,
    text: String,
    maxWidth: Float,
    maxHeight: Float,
    fontFor: (Int) -> CardFont,
    min: Int,
    max: Int,
    lineHeight: Float,
): CardBlock {
    for (size in max downTo min) {
        val font = fontFor(size)
        val lines = wrap(measure, font, text, maxWidth)
        val height = lines.size * size * lineHeight
        if (height <= maxHeight) {
            return CardBlock(size, lines, height, lineHeight, font, truncated = false)
        }
    }

    val font = fontFor(min)
    val lines = wrap(measure, font, text, maxWidth)
    val keep = maxOf(1, floor(maxHeight / (min * lineHeight)).toInt())
    val kept = lines.take(keep)
    if (kept.isEmpty()) {
        return CardBlock(min, emptyList(), 0f, lineHeight, font, truncated = true)
    }

    var last = kept.last()
    while (last.isNotEmpty() && measure.width("$last …", font) > maxWidth) {
        last = last.dropLast(1)
    }
    val finalLast = last.replace(trailingWsPunctRegex, "") + " …"
    val finalLines = kept.dropLast(1) + finalLast
    return CardBlock(min, finalLines, kept.size * min * lineHeight, lineHeight, font, truncated = true)
}

/** Exact port of `ellipsize`. */
fun ellipsize(measure: TextMeasure, font: CardFont, text: String, maxWidth: Float): String {
    if (measure.width(text, font) <= maxWidth) return text
    var out = text
    while (out.length > 1 && measure.width("$out…", font) > maxWidth) {
        out = out.dropLast(1)
    }
    return out.jsTrim() + "…"
}

/** Exact port of `pillWidth`. */
fun pillWidth(measure: TextMeasure, font: CardFont, label: String, hasDot: Boolean): Float =
    trackedWidth(measure, font, label, 2.6f) + (if (hasDot) 72f else 52f)

/** Exact port of the reference-row shrink loop (§3.1 step 12). */
fun refSizeFor(measure: TextMeasure, refText: String, refRoom: Float): Int {
    var size = 31
    while (size > 25 && measure.width(refText, CardFont(FONT_INTER, 600, size.toFloat())) > refRoom) {
        size--
    }
    return size
}

/**
 * Per-script font pickers, so `layoutCard` doesn't need to know about weights or asset paths.
 * Constructed directly (rather than only from `ArabicScript`) so tests can pick arbitrary
 * min/max/lineHeight to exercise `layoutCard`'s branches without needing unrealistic band sizes.
 */
class CardFonts(arabicFamily: String, arabicWeight: Int, val arabicMin: Int, val arabicMax: Int, val arabicLineHeight: Float) {
    val arabicFontFor: (Int) -> CardFont = { size -> CardFont(arabicFamily, arabicWeight, size.toFloat()) }
    val englishFontFor: (Int) -> CardFont = { size -> CardFont(FONT_CORMORANT, 500, size.toFloat()) }

    companion object {
        fun forScript(script: ArabicScript): CardFonts {
            val face = cardArabicFace(script)
            return CardFonts(face.family, face.weight, face.min, face.max, face.lineHeight)
        }
    }
}

data class CardLayoutResult(
    val arabicBlock: CardBlock?,
    val englishBlock: CardBlock,
    val narratorH: Float,
    val arabicGap: Float,
    val band: Float,
    val totalH: Float,
    val startY: Float,
)

private const val INNER_WIDTH = 844f
private const val BAND_TOP = 196f
private const val REFERENCE_BAND_BOTTOM = 792f
private const val NO_REFERENCE_BAND_BOTTOM = 940f

/** Exact port of §3.1 steps 5–8: band/available sizing, the Arabic fit-or-drop, vertical placement. */
fun layoutCard(measure: TextMeasure, input: CardInput, fonts: CardFonts): CardLayoutResult {
    val english = input.english.jsTrim()
    val arabic = input.arabic.jsTrim()
    val narrator = input.narrator.jsTrim()
    val hasReference = input.book.isNotEmpty() || input.number.isNotEmpty()

    val narratorH = if (narrator.isNotEmpty()) 50f else 0f
    val arabicGap = if (arabic.isNotEmpty()) 64f else 0f
    val bandBottom = if (hasReference) REFERENCE_BAND_BOTTOM else NO_REFERENCE_BAND_BOTTOM
    val band = bandBottom - BAND_TOP
    var available = band - narratorH - arabicGap

    var arabicBlock: CardBlock? = null
    if (arabic.isNotEmpty()) {
        val maxHeight = available * (if (english.length > 280) 0.32f else 0.40f)
        val block = fitBlock(measure, arabic, INNER_WIDTH, maxHeight, fonts.arabicFontFor, fonts.arabicMin, fonts.arabicMax, fonts.arabicLineHeight)
        if (block.truncated && block.lines.size < 2) {
            available = band - narratorH
        } else {
            arabicBlock = block
            available -= block.height
        }
    }

    val englishBlock = fitBlock(measure, english, INNER_WIDTH, available, fonts.englishFontFor, 29, 60, 1.48f)

    val totalH = (arabicBlock?.let { it.height + arabicGap } ?: 0f) + englishBlock.height + narratorH
    val startY = BAND_TOP + maxOf(0f, (band - totalH) * 0.55f)

    return CardLayoutResult(arabicBlock, englishBlock, narratorH, arabicGap, band, totalH, startY)
}
