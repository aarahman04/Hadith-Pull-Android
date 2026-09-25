package online.hadithpull.app.ui.theme

import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

private val noFontPadding = PlatformTextStyle(includeFontPadding = false)

private fun clamp(min: Float, preferred: Float, max: Float): Float = preferred.coerceIn(min, max)

/** §2.6 typography roles. Colour is applied at the call site from LocalHadithColors, not baked in here. */
data class HadithTypography(
    val heroTitle: TextStyle,
    val eyebrow: TextStyle,
    val english: TextStyle,
    val arabic: TextStyle,
    val placeholder: TextStyle,
    val sectionLabel: TextStyle,
    val sheetTitle: TextStyle,
    val pageTitle: TextStyle,
    val sectionTitle: TextStyle,
    val helper: TextStyle,
    val optionLabel: TextStyle,
    val secondaryAction: TextStyle,
    val statusLabel: TextStyle,
    val panelTitle: TextStyle,
    val body: TextStyle,
    val contentMeta: TextStyle,
    val contentMetaItalic: TextStyle,
    val secondaryScaled: TextStyle,
    val label: TextStyle,
)

/**
 * @param windowWidthDp the window width in dp ("w" in the spec's clamp formulas).
 * @param readingScale the user's text-size preference (COMFORTABLE/LARGE/LARGEST).
 * @param arabicScale the current Arabic script's arabicScale (§2.6 Arabic-per-script table).
 * @param arabicLineHeight the current Arabic script's line height (same table).
 */
fun hadithTypography(
    windowWidthDp: Float,
    inter: FontFamily,
    cormorant: FontFamily,
    arabicFamily: FontFamily,
    readingScale: Float,
    arabicScale: Float,
    arabicLineHeight: Float,
): HadithTypography {
    val w = windowWidthDp
    val heroSize = if (w < 720) clamp(28f, 0.08f * w, 36.8f) else clamp(33.6f, 0.06f * w, 54.4f)
    val englishSize = (if (w < 400) 18.9f else clamp(20.8f, 0.033f * w, 27.2f)) * readingScale
    val englishLineHeight = if (w < 400) 1.75f else 1.78f
    // R-Q3: Arabic equals the English size times the script's arabicScale factor -- no separate base.
    val arabicSize = englishSize * arabicScale

    return HadithTypography(
        heroTitle = TextStyle(
            fontFamily = cormorant,
            fontWeight = FontWeight.Medium,
            fontSize = heroSize.sp,
            lineHeight = 1.15.em,
            letterSpacing = (-0.01).em,
            platformStyle = noFontPadding,
        ),
        eyebrow = TextStyle(
            fontFamily = inter,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.5.sp,
            letterSpacing = 0.18.em,
            platformStyle = noFontPadding,
        ),
        english = TextStyle(
            fontFamily = cormorant,
            fontWeight = FontWeight.Normal,
            fontSize = englishSize.sp,
            lineHeight = englishLineHeight.em,
            letterSpacing = 0.005.em,
            platformStyle = noFontPadding,
        ),
        arabic = TextStyle(
            fontFamily = arabicFamily,
            fontSize = arabicSize.sp,
            lineHeight = arabicLineHeight.em,
            platformStyle = noFontPadding,
        ),
        placeholder = TextStyle(
            fontFamily = cormorant,
            fontStyle = FontStyle.Italic,
            fontSize = 20.8.sp,
            platformStyle = noFontPadding,
        ),
        sectionLabel = TextStyle(
            fontFamily = inter,
            fontWeight = FontWeight.Bold,
            fontSize = 11.5.sp,
            letterSpacing = 0.16.em,
            platformStyle = noFontPadding,
        ),
        sheetTitle = TextStyle(
            fontFamily = cormorant,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            lineHeight = 1.2.em,
            platformStyle = noFontPadding,
        ),
        pageTitle = TextStyle(
            fontFamily = cormorant,
            fontWeight = FontWeight.Medium,
            fontSize = 32.sp,
            platformStyle = noFontPadding,
        ),
        sectionTitle = TextStyle(
            fontFamily = cormorant,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            lineHeight = 1.2.em,
            platformStyle = noFontPadding,
        ),
        helper = TextStyle(
            fontFamily = inter,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 1.55.em,
            platformStyle = noFontPadding,
        ),
        optionLabel = TextStyle(
            fontFamily = inter,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 1.35.em,
            platformStyle = noFontPadding,
        ),
        secondaryAction = TextStyle(
            fontFamily = inter,
            fontWeight = FontWeight.Medium,
            fontSize = 13.5.sp,
            lineHeight = 1.35.em,
            platformStyle = noFontPadding,
        ),
        statusLabel = TextStyle(
            fontFamily = inter,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.5.sp,
            lineHeight = 1.2.em,
            platformStyle = noFontPadding,
        ),
        panelTitle = TextStyle(
            fontFamily = cormorant,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.4.sp,
            platformStyle = noFontPadding,
        ),
        body = TextStyle(
            fontFamily = inter,
            fontWeight = FontWeight.Normal,
            fontSize = 16.3.sp,
            lineHeight = 1.75.em,
            platformStyle = noFontPadding,
        ),
        contentMeta = TextStyle(
            fontFamily = inter,
            fontWeight = FontWeight.SemiBold,
            // Metadata stays fixed when the reader changes size; the reading control is
            // reserved for the English and Arabic text only.
            fontSize = 13.5.sp,
            lineHeight = 1.4.em,
            platformStyle = noFontPadding,
        ),
        contentMetaItalic = TextStyle(
            fontFamily = inter,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Italic,
            fontSize = 13.5.sp,
            lineHeight = 1.5.em,
            platformStyle = noFontPadding,
        ),
        secondaryScaled = TextStyle(
            fontFamily = inter,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            platformStyle = noFontPadding,
        ),
        label = TextStyle(
            fontFamily = inter,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.5.sp,
            letterSpacing = 0.07.em,
            platformStyle = noFontPadding,
        ),
    )
}
