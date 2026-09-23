package online.hadithpull.app.data.prefs

/** D4: 3-state theme model, default SYSTEM. */
enum class Theme { SYSTEM, LIGHT, DARK }

enum class ArabicScript { NASKH, CLEAR, BOLD }

enum class TextSize(val scale: Float) {
    COMFORTABLE(1.0f),
    LARGE(1.12f),
    LARGEST(1.26f),
}

data class Settings(
    val theme: Theme = Theme.SYSTEM,
    val arabicScript: ArabicScript = ArabicScript.NASKH,
    val textSize: TextSize = TextSize.COMFORTABLE,
    val cardArabic: Boolean = true,
)
