package online.hadithpull.app.card

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import androidx.compose.ui.graphics.toArgb
import java.util.Locale
import kotlin.math.roundToInt
import online.hadithpull.app.domain.grading
import online.hadithpull.app.domain.text.jsTrim
import online.hadithpull.app.ui.theme.statusPillColors

/**
 * §3.1's Paint setup: ANTI_ALIAS_FLAG, SUBPIXEL_TEXT_FLAG and isLinearText. Both the measuring
 * Paint (below) and the drawing Paint (CardRenderer.render) must build from this one function —
 * a measurement taken with different flags than the draw would make wrap/fitBlock's computed
 * line breaks not match what's actually drawn.
 */
private fun newCardPaint(): Paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply { isLinearText = true }

/** Production TextMeasure: a Paint-backed implementation, matching §3.1's Paint setup exactly. */
class PaintTextMeasure(private val assets: AssetManager) : TextMeasure {
    private val paint = newCardPaint()

    override fun width(text: String, font: CardFont): Float {
        paint.typeface = AppTypefaces.get(assets, font.family, font.weight)
        paint.textSize = font.size
        return paint.measureText(text)
    }
}

private data class CardPalette(
    val bgFrom: Int,
    val bgTo: Int,
    val glow: Int,
    val vignette: Int,
    val accent: Int,
    val text: Int,
    val muted: Int,
    val rule: Int,
    val pattern: Int,
    val patternAlpha: Float,
)

private fun colorWithAlpha(hex: String, alpha: Float): Int {
    val base = Color.parseColor(hex)
    return Color.argb((alpha * 255).roundToInt(), Color.red(base), Color.green(base), Color.blue(base))
}

/** §3.1's colour table. */
private fun paletteFor(theme: CardTheme): CardPalette = when (theme) {
    CardTheme.LIGHT -> CardPalette(
        bgFrom = Color.parseColor("#FDFBF6"),
        bgTo = Color.parseColor("#F0E9DC"),
        glow = colorWithAlpha("#A97E3C", 0.10f),
        vignette = colorWithAlpha("#785E37", 0.06f),
        accent = Color.parseColor("#A97E3C"),
        text = Color.parseColor("#1C1917"),
        muted = Color.parseColor("#7A716A"),
        rule = colorWithAlpha("#1C1917", 0.13f),
        pattern = Color.parseColor("#A97E3C"),
        patternAlpha = 0.03f,
    )
    CardTheme.DARK -> CardPalette(
        bgFrom = Color.parseColor("#141C22"),
        bgTo = Color.parseColor("#0A0F14"),
        glow = colorWithAlpha("#5EEAD4", 0.07f),
        vignette = colorWithAlpha("#000000", 0.38f),
        accent = Color.parseColor("#E0BD85"),
        text = Color.parseColor("#ECE9E4"),
        muted = Color.parseColor("#9AA3A8"),
        rule = colorWithAlpha("#E7E5E4", 0.16f),
        pattern = Color.parseColor("#E0BD85"),
        patternAlpha = 0.035f,
    )
}

private const val SIZE = 1080
private const val LEFT = 118f
private const val RIGHT = 962f
private const val INNER_WIDTH = 844f

/** A line-by-line port of card.js onto android.graphics. Pure drawing: no UI, Room or network. */
object CardRenderer {

    /** Draws each code point left-aligned at x, advancing by measure(cp) + spacing (`trackedAt`). */
    private fun trackedAt(canvas: Canvas, paint: Paint, font: CardFont, typeface: android.graphics.Typeface, text: String, x: Float, y: Float, spacing: Float, color: Int) {
        paint.typeface = typeface
        paint.textSize = font.size
        paint.color = color
        paint.style = Paint.Style.FILL
        var cx = x
        for (cp in codePointStrings(text)) {
            canvas.drawText(cp, cx, y, paint)
            cx += paint.measureText(cp) + spacing
        }
    }

    private fun drawBlock(canvas: Canvas, paint: Paint, assets: AssetManager, block: CardBlock, x: Float, top: Float, color: Int, rtl: Boolean) {
        paint.typeface = AppTypefaces.get(assets, block.font.family, block.font.weight)
        paint.textSize = block.font.size
        paint.color = color
        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.LEFT
        val step = block.fontSize * block.lineHeight
        block.lines.forEachIndexed { i, line ->
            val baseline = top + i * step + block.fontSize * 0.78f
            if (rtl) {
                val w = paint.measureText(line)
                canvas.drawTextRun(line, 0, line.length, 0, line.length, x - w, baseline, true, paint)
            } else {
                canvas.drawText(line, x, baseline, paint)
            }
        }
    }

    private fun drawPills(canvas: Canvas, paint: Paint, assets: AssetManager, measure: TextMeasure, pills: List<Pair<String, Int>>, dots: List<Boolean>, anchorRight: Float, top: Float): Float {
        if (pills.isEmpty()) return 0f
        val font = CardFont(FONT_INTER, 600, 21f)
        val gap = 14f
        val pillHeight = 46f
        val widths = pills.indices.map { i -> pillWidth(measure, font, pills[i].first, dots[i]) }
        val total = widths.sum() + gap * (pills.size - 1)
        var x = anchorRight - total

        pills.forEachIndexed { i, (label, color) ->
            val w = widths[i]
            val hasDot = dots[i]
            val rect = RectF(x, top, x + w, top + pillHeight)

            paint.style = Paint.Style.FILL
            paint.color = color
            paint.alpha = (0.14f * 255).roundToInt()
            canvas.drawRoundRect(rect, pillHeight / 2, pillHeight / 2, paint)

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.3f
            paint.alpha = (0.45f * 255).roundToInt()
            canvas.drawRoundRect(rect, pillHeight / 2, pillHeight / 2, paint)
            paint.alpha = 255

            if (hasDot) {
                paint.style = Paint.Style.FILL
                paint.color = color
                canvas.drawCircle(x + 26f, top + pillHeight / 2, 5f, paint)
            }

            trackedAt(canvas, paint, font, AppTypefaces.get(assets, FONT_INTER, 600), label, x + (if (hasDot) 44f else 26f), top + pillHeight / 2 + 7.5f, 2.6f, color)

            x += w + gap
        }
        return total
    }

    fun render(assets: AssetManager, input: CardInput, theme: CardTheme): Bitmap {
        val bitmap = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val palette = paletteFor(theme)
        val paint = newCardPaint()
        val measure = PaintTextMeasure(assets)
        val full = RectF(0f, 0f, SIZE.toFloat(), SIZE.toFloat())

        // Step 1: background, halo, pattern, vignette.
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(0f, 0f, SIZE * 0.3f, SIZE.toFloat(), palette.bgFrom, palette.bgTo, Shader.TileMode.CLAMP)
        canvas.drawRect(full, paint)

        paint.shader = RadialGradient(540f, 367.2f, 864f, intArrayOf(palette.glow, palette.glow, 0x00000000), floatArrayOf(0f, 40f / 864f, 1f), Shader.TileMode.CLAMP)
        canvas.drawRect(full, paint)
        paint.shader = null

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.4f
        paint.color = palette.pattern
        paint.alpha = (palette.patternAlpha * 255).roundToInt()
        var py = 54
        while (py < SIZE) {
            var px = 54
            while (px < SIZE) {
                for (k in 0..1) {
                    canvas.save()
                    canvas.translate(px.toFloat(), py.toFloat())
                    canvas.rotate(k * 45f)
                    canvas.drawRect(-21f, -21f, 21f, 21f, paint)
                    canvas.restore()
                }
                px += 108
            }
            py += 108
        }
        paint.alpha = 255

        paint.style = Paint.Style.FILL
        paint.shader = RadialGradient(540f, 540f, 864f, intArrayOf(0x00000000, 0x00000000, palette.vignette), floatArrayOf(0f, 453.6f / 864f, 1f), Shader.TileMode.CLAMP)
        canvas.drawRect(full, paint)
        paint.shader = null

        // Step 2: frame.
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.6f
        paint.color = palette.rule
        canvas.drawRoundRect(RectF(46f, 46f, 1034f, 1034f), 30f, 30f, paint)

        // Step 3: masthead.
        trackedAt(canvas, paint, CardFont(FONT_INTER, 600, 22f), AppTypefaces.get(assets, FONT_INTER, 600), "HADITH", LEFT, 122f, 10f, palette.accent)

        // Step 4: watermark.
        trackedAt(canvas, paint, CardFont(FONT_INTER, 500, 20f), AppTypefaces.get(assets, FONT_INTER, 500), input.site.uppercase(Locale.ROOT), LEFT, 1002f, 3.4f, palette.muted)

        // Steps 5–8: layout (pure, testable separately).
        val fonts = CardFonts.forScript(input.script)
        val layout = layoutCard(measure, input, fonts)

        var y = layout.startY

        // Step 9: Arabic.
        if (layout.arabicBlock != null) {
            drawBlock(canvas, paint, assets, layout.arabicBlock, RIGHT, y, palette.text, rtl = true)
            y += layout.arabicBlock.height + layout.arabicGap * 0.45f
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.3f
            paint.color = palette.rule
            canvas.drawLine(LEFT, y, LEFT + 88f, y, paint)
            y += layout.arabicGap * 0.55f
        }

        // Step 10: English.
        drawBlock(canvas, paint, assets, layout.englishBlock, LEFT, y, palette.text, rtl = false)
        y += layout.englishBlock.height

        // Step 11: narrator.
        val narrator = input.narrator.jsTrim()
        if (narrator.isNotEmpty()) {
            val narratorFont = CardFont(FONT_INTER, 400, 25f)
            paint.typeface = AppTypefaces.get(assets, FONT_INTER, 400)
            paint.textSize = 25f
            paint.textSkewX = -0.25f
            paint.color = palette.muted
            paint.style = Paint.Style.FILL
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText(ellipsize(measure, narratorFont, narrator, INNER_WIDTH), LEFT, y + 38f, paint)
            paint.textSkewX = 0f
        }

        // Step 12: reference.
        val hasReference = input.book.isNotEmpty() || input.number.isNotEmpty()
        if (hasReference) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.3f
            paint.color = palette.rule
            canvas.drawLine(LEFT, 848f, RIGHT, 848f, paint)

            val statusText = input.status.jsTrim()
            val labels = mutableListOf<String>()
            val colors = mutableListOf<Int>()
            val dots = mutableListOf<Boolean>()
            if (statusText.isNotEmpty()) {
                labels.add(statusText.uppercase(Locale.ROOT))
                colors.add(statusPillColors(grading(statusText), theme == CardTheme.DARK).foreground.toArgb())
                dots.add(true)
            }
            if (input.excerpt || layout.englishBlock.truncated) {
                labels.add("EXCERPT")
                colors.add(palette.muted)
                dots.add(false)
            }
            val pills = labels.indices.map { labels[it] to colors[it] }
            val pillsWide = drawPills(canvas, paint, assets, measure, pills, dots, RIGHT, 894f)

            paint.color = palette.text
            paint.textAlign = Paint.Align.LEFT

            val refRoom = INNER_WIDTH - (if (pillsWide > 0f) pillsWide + 30f else 0f)
            val numberPart = if (input.number.isNotEmpty()) "  ·  Hadith ${input.number}" else ""
            val refText = input.book + numberPart

            val refSize = refSizeFor(measure, refText, refRoom)
            val refFont = CardFont(FONT_INTER, 600, refSize.toFloat())
            val bookPart = ellipsize(measure, refFont, input.book, refRoom - measure.width(numberPart, refFont))

            paint.typeface = AppTypefaces.get(assets, FONT_INTER, 600)
            paint.textSize = refSize.toFloat()
            canvas.drawText(bookPart + numberPart, LEFT, 928f, paint)
        }

        return bitmap
    }
}
