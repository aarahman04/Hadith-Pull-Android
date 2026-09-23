package online.hadithpull.app.card

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import online.hadithpull.app.data.prefs.ArabicScript

/** Fixed width per character, ignoring font — for `wrap`/`ellipsize`/`trackedWidth` tests. */
private class ConstantCharMeasure(private val charWidth: Float) : TextMeasure {
    override fun width(text: String, font: CardFont): Float = text.length * charWidth
}

/** Fixed width per code point, ignoring font — isolates code-point counting from char counting. */
private class ConstantCodePointMeasure(private val perCp: Float) : TextMeasure {
    override fun width(text: String, font: CardFont): Float = codePointStrings(text).size * perCp
}

/** Always zero — lets `fitBlock` succeed on its first (largest-size) attempt, one line. */
private object ZeroWidthMeasure : TextMeasure {
    override fun width(text: String, font: CardFont): Float = 0f
}

private val testFont = CardFont(FONT_CORMORANT, 500, 40f)

class CodePointStringsTest {
    @Test
    fun `splits a surrogate pair as one code point`() {
        val emoji = "😀" // 😀, above the BMP
        assertEquals(listOf(emoji), codePointStrings(emoji))
    }

    @Test
    fun `splits BMP characters individually`() {
        assertEquals(listOf("a", "b", "c"), codePointStrings("abc"))
    }
}

class TrackedWidthTest {
    @Test
    fun `sums per-code-point width plus spacing, minus one trailing spacing`() {
        val measure = ConstantCodePointMeasure(10f)
        // (10+5) + (10+5) - 5 = 25
        assertEquals(25f, trackedWidth(measure, testFont, "AB", spacing = 5f), 0.001f)
    }

    @Test
    fun `counts code points, not UTF-16 chars`() {
        val measure = ConstantCodePointMeasure(10f)
        val text = "😀😀" // two emoji = 2 code points, 4 chars
        assertEquals(25f, trackedWidth(measure, testFont, text, spacing = 5f), 0.001f) // (10+5)*2 - 5
    }

    @Test
    fun `empty text has zero width`() {
        assertEquals(0f, trackedWidth(ConstantCodePointMeasure(10f), testFont, "", spacing = 5f), 0.001f)
    }
}

class WrapTest {
    @Test
    fun `greedily fills lines up to maxWidth`() {
        val lines = wrap(ConstantCharMeasure(1f), testFont, "one two three", maxWidth = 7f)
        assertEquals(listOf("one two", "three"), lines)
    }

    @Test
    fun `an over-wide single word overflows onto its own line`() {
        val lines = wrap(ConstantCharMeasure(1f), testFont, "reallylongword hi", maxWidth = 5f)
        assertEquals(listOf("reallylongword", "hi"), lines)
    }

    @Test
    fun `blank paragraphs produce no line`() {
        val lines = wrap(ConstantCharMeasure(1f), testFont, "\n\nhello", maxWidth = 100f)
        assertEquals(listOf("hello"), lines)
    }

    @Test
    fun `splits on explicit newlines into separate paragraphs`() {
        val lines = wrap(ConstantCharMeasure(1f), testFont, "first\nsecond", maxWidth = 100f)
        assertEquals(listOf("first", "second"), lines)
    }
}

class FitBlockTest {
    @Test
    fun `fits at the largest size when it already satisfies maxHeight`() {
        val result = fitBlock(
            ZeroWidthMeasure, "hi", maxWidth = 1000f, maxHeight = 200f,
            fontFor = { size -> CardFont(FONT_CORMORANT, 500, size.toFloat()) },
            min = 29, max = 60, lineHeight = 1.48f,
        )
        assertEquals(60, result.fontSize)
        assertFalse(result.truncated)
        assertEquals(listOf("hi"), result.lines)
    }

    @Test
    fun `descends linearly to the first size whose height fits`() {
        // Text always wraps to exactly one line regardless of size (ZeroWidthMeasure), so
        // height = size * lineHeight is monotonic — first fit is size 33 (33*1.48=48.84<=50,
        // 34*1.48=50.32>50).
        val result = fitBlock(
            ZeroWidthMeasure, "hi", maxWidth = 1000f, maxHeight = 50f,
            fontFor = { size -> CardFont(FONT_CORMORANT, 500, size.toFloat()) },
            min = 29, max = 60, lineHeight = 1.48f,
        )
        assertEquals(33, result.fontSize)
        assertFalse(result.truncated)
    }

    @Test
    fun `truncates with an ellipsis when even min overflows`() {
        // Constant per-char width means wrap's output is size-independent: "alpha beta" then
        // "gamma delta" at maxWidth 12. min*lineHeight=29, so even a single kept line (29)
        // exceeds maxHeight 20 -- keep = max(1, floor(20/29)) = 1. The kept line "alpha beta"
        // plus " …" is exactly 12 wide, so it fits without dropping any more characters.
        val result = fitBlock(
            ConstantCharMeasure(1f), "alpha beta gamma delta", maxWidth = 12f, maxHeight = 20f,
            fontFor = { size -> CardFont(FONT_CORMORANT, 500, size.toFloat()) },
            min = 29, max = 29, lineHeight = 1f,
        )
        assertTrue(result.truncated)
        assertEquals(29, result.fontSize)
        assertEquals(listOf("alpha beta …"), result.lines)
        assertEquals(29f, result.height, 0.001f)
    }
}

class EllipsizeTest {
    @Test
    fun `returns the text unchanged when it already fits`() {
        assertEquals("hi", ellipsize(ConstantCharMeasure(1f), testFont, "hi", maxWidth = 100f))
    }

    @Test
    fun `drops characters until the ellipsis fits`() {
        // len(out)+1 <= 8 -> out.length <= 7 -> "hello w"
        assertEquals("hello w…", ellipsize(ConstantCharMeasure(1f), testFont, "hello world", maxWidth = 8f))
    }
}

class PillWidthTest {
    @Test
    fun `adds 72 for a dot, 52 without`() {
        val measure = ConstantCodePointMeasure(10f)
        val font = CardFont(FONT_INTER, 600, 21f)
        // trackedWidth("ABC", 2.6) = (10+2.6)*3 - 2.6 = 35.2
        assertEquals(107.2f, pillWidth(measure, font, "ABC", hasDot = true), 0.001f)
        assertEquals(87.2f, pillWidth(measure, font, "ABC", hasDot = false), 0.001f)
    }
}

class RefSizeForTest {
    private class ScalingMeasure(private val perCharPerPt: Float) : TextMeasure {
        override fun width(text: String, font: CardFont): Float = text.length * font.size * perCharPerPt
    }

    @Test
    fun `stays at 31 when it already fits`() {
        val measure = ScalingMeasure(1f)
        assertEquals(31, refSizeFor(measure, refText = "x", refRoom = 1000f))
    }

    @Test
    fun `stops at 25 even if that still doesn't fit`() {
        val measure = ScalingMeasure(1f) // width = len(20) * size
        assertEquals(25, refSizeFor(measure, refText = "x".repeat(20), refRoom = 300f))
    }
}

class LayoutCardTest {
    @Test
    fun `no arabic, no narrator, no reference`() {
        val input = CardInput(
            english = "short english", arabic = "", narrator = "", book = "", number = "",
            status = "", script = ArabicScript.NASKH, excerpt = false,
        )
        val result = layoutCard(ZeroWidthMeasure, input, CardFonts.forScript(ArabicScript.NASKH))

        assertEquals(744f, result.band, 0.001f) // bandBottom 940 - bandTop 196
        assertNull(result.arabicBlock)
        assertEquals(0f, result.narratorH, 0.001f)
        assertEquals(0f, result.arabicGap, 0.001f)
        assertEquals(88.8f, result.englishBlock.height, 0.01f) // 60 * 1.48
        assertEquals(88.8f, result.totalH, 0.01f)
        assertEquals(556.36f, result.startY, 0.01f) // 196 + (744-88.8)*0.55
    }

    @Test
    fun `a reference and a narrator shrink the band and add narratorH`() {
        val input = CardInput(
            english = "short english", arabic = "", narrator = "— Narrated X", book = "Sahih Bukhari",
            number = "1", status = "", script = ArabicScript.NASKH, excerpt = false,
        )
        val result = layoutCard(ZeroWidthMeasure, input, CardFonts.forScript(ArabicScript.NASKH))

        assertEquals(596f, result.band, 0.001f) // bandBottom 792 - bandTop 196
        assertEquals(50f, result.narratorH, 0.001f)
        assertEquals(138.8f, result.totalH, 0.01f) // 88.8 + 50
        assertEquals(447.46f, result.startY, 0.01f) // 196 + (596-138.8)*0.55
    }

    @Test
    fun `a fitting Arabic block reduces the available height for English`() {
        val input = CardInput(
            english = "short english", arabic = "arabic text here", narrator = "", book = "",
            number = "", status = "", script = ArabicScript.NASKH, excerpt = false,
        )
        val result = layoutCard(ZeroWidthMeasure, input, CardFonts.forScript(ArabicScript.NASKH))

        assertTrue(result.arabicBlock != null)
        assertFalse(result.arabicBlock!!.truncated)
        assertEquals(95f, result.arabicBlock.height, 0.01f) // NASKH max 50 * lineHeight 1.9
        assertEquals(64f, result.arabicGap, 0.001f)
        assertEquals(247.8f, result.totalH, 0.01f) // (95+64) + 88.8
        assertEquals(468.91f, result.startY, 0.01f) // 196 + (744-247.8)*0.55
    }

    @Test
    fun `an Arabic block truncated to under 2 lines is dropped entirely`() {
        // min=max=2000 guarantees fitBlock can never satisfy any realistic maxHeight, and the
        // single-word input guarantees wrap() always yields exactly one line either way — so
        // "kept.size < 2" fires regardless of maxWidth/measure specifics.
        val fonts = CardFonts("TestArabic", 400, arabicMin = 2000, arabicMax = 2000, arabicLineHeight = 1f)
        val input = CardInput(
            english = "short english", arabic = "waahid", narrator = "", book = "", number = "",
            status = "", script = ArabicScript.NASKH, excerpt = false,
        )
        val result = layoutCard(ZeroWidthMeasure, input, fonts)

        assertNull(result.arabicBlock)
        assertEquals(64f, result.arabicGap, 0.001f) // the constant itself is untouched (parity with card.js)
        assertEquals(88.8f, result.englishBlock.height, 0.01f) // available fell back to band - narratorH
        assertEquals(88.8f, result.totalH, 0.01f)
        assertEquals(556.36f, result.startY, 0.01f)
    }
}
