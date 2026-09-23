package online.hadithpull.app.ui.theme

import android.content.res.AssetManager
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import online.hadithpull.app.data.prefs.ArabicScript

/**
 * §2.6/D7: bundled variable + static fonts in assets/fonts/, loaded with
 * FontVariation.Settings so Compose renders the right weight offline on the first frame.
 */
private fun interWeight(assets: AssetManager, weight: Int, fontWeight: FontWeight) = Font(
    path = "fonts/inter_var.ttf",
    assetManager = assets,
    weight = fontWeight,
    variationSettings = FontVariation.Settings(
        FontVariation.weight(weight),
        FontVariation.Setting("opsz", 14f),
    ),
)

fun interFamily(assets: AssetManager): FontFamily = FontFamily(
    interWeight(assets, 400, FontWeight.Normal),
    interWeight(assets, 500, FontWeight.Medium),
    interWeight(assets, 600, FontWeight.SemiBold),
    interWeight(assets, 700, FontWeight.Bold),
)

private fun cormorantWeight(assets: AssetManager, weight: Int, fontWeight: FontWeight) = Font(
    path = "fonts/cormorant_garamond_var.ttf",
    assetManager = assets,
    weight = fontWeight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

fun cormorantFamily(assets: AssetManager): FontFamily = FontFamily(
    cormorantWeight(assets, 400, FontWeight.Normal),
    cormorantWeight(assets, 500, FontWeight.Medium),
    cormorantWeight(assets, 600, FontWeight.SemiBold),
)

fun amiriFamily(assets: AssetManager): FontFamily = FontFamily(
    Font(path = "fonts/amiri_regular.ttf", assetManager = assets, weight = FontWeight.Normal),
)

fun scheherazadeFamily(assets: AssetManager): FontFamily = FontFamily(
    Font(path = "fonts/scheherazade_regular.ttf", assetManager = assets, weight = FontWeight.Normal),
)

fun notoNaskhArabicFamily(assets: AssetManager): FontFamily = FontFamily(
    Font(
        path = "fonts/noto_naskh_arabic_var.ttf",
        assetManager = assets,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700)),
    ),
)

/** Arabic per script table (§2.6): NASKH=Amiri, CLEAR=Scheherazade New, BOLD=Noto Naskh Arabic. */
fun arabicFontFamily(assets: AssetManager, script: ArabicScript): FontFamily = when (script) {
    ArabicScript.NASKH -> amiriFamily(assets)
    ArabicScript.CLEAR -> scheherazadeFamily(assets)
    ArabicScript.BOLD -> notoNaskhArabicFamily(assets)
}

/** arabicScale × line-height per script (§2.6). */
data class ArabicScriptMetrics(val scale: Float, val lineHeight: Float)

fun arabicScriptMetrics(script: ArabicScript): ArabicScriptMetrics = when (script) {
    ArabicScript.NASKH -> ArabicScriptMetrics(scale = 1.0f, lineHeight = 2.15f)
    ArabicScript.CLEAR -> ArabicScriptMetrics(scale = 1.16f, lineHeight = 2.35f)
    ArabicScript.BOLD -> ArabicScriptMetrics(scale = 1.0f, lineHeight = 2.3f)
}
