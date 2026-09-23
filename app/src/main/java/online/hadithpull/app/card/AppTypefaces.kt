package online.hadithpull.app.card

import android.content.res.AssetManager
import android.graphics.Typeface

/**
 * android.graphics.Typeface lookups for the card renderer — a separate cache from the
 * Compose FontFamily objects in ui/theme/Fonts.kt, but the same bundled files (D7):
 * variable-font weights via Typeface.Builder.setFontVariationSettings (API 26+).
 */
object AppTypefaces {
    private val cache = mutableMapOf<Pair<String, Int>, Typeface>()

    fun get(assets: AssetManager, family: String, weight: Int): Typeface =
        cache.getOrPut(family to weight) { load(assets, family, weight) }

    private fun load(assets: AssetManager, family: String, weight: Int): Typeface = when (family) {
        FONT_INTER -> variable(assets, "fonts/inter_var.ttf", weight, opsz = 14f)
        FONT_CORMORANT -> variable(assets, "fonts/cormorant_garamond_var.ttf", weight)
        FONT_AMIRI -> Typeface.createFromAsset(assets, "fonts/amiri_regular.ttf")
        FONT_SCHEHERAZADE -> Typeface.createFromAsset(assets, "fonts/scheherazade_regular.ttf")
        FONT_NOTO_NASKH -> variable(assets, "fonts/noto_naskh_arabic_var.ttf", weight)
        else -> throw IllegalArgumentException("Unknown card font family: $family")
    }

    private fun variable(assets: AssetManager, path: String, weight: Int, opsz: Float? = null): Typeface {
        val settings = buildString {
            append("'wght' $weight")
            if (opsz != null) append(", 'opsz' $opsz")
        }
        return Typeface.Builder(assets, path).setFontVariationSettings(settings).build()
    }
}
