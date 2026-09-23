package online.hadithpull.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import online.hadithpull.app.domain.Grading

/** §2.6 design tokens. Read from LocalHadithColors, except the six roles HadithTheme maps onto MaterialTheme.colorScheme. */
@Immutable
data class HadithColors(
    val bg: Color,
    val bgTintA: Color,
    val bgTintB: Color,
    val surface: Color,
    val surfaceSolid: Color,
    val border: Color,
    val borderStrong: Color,
    val text: Color,
    val textSoft: Color,
    val muted: Color,
    val accent: Color,
    val accentInk: Color,
    val accentSoft: Color,
    val refBg: Color,
    val gold: Color,
    val goldSoft: Color,
    val error: Color,
    val scrim: Color,
)

val LightHadithColors = HadithColors(
    bg = Color(0xFFF6F2EA),
    bgTintA = Color(0xFFD4AF7A).copy(alpha = 0.28f),
    bgTintB = Color(0xFF0F766E).copy(alpha = 0.14f),
    surface = Color(0xFFFFFDF9).copy(alpha = 0.82f),
    surfaceSolid = Color(0xFFFFFDF9),
    border = Color(0xFF1C1917).copy(alpha = 0.09f),
    borderStrong = Color(0xFF1C1917).copy(alpha = 0.16f),
    text = Color(0xFF1C1917),
    textSoft = Color(0xFF44403C),
    muted = Color(0xFF7A716A),
    accent = Color(0xFF0F766E),
    accentInk = Color(0xFF0F766E),
    accentSoft = Color(0xFF0F766E).copy(alpha = 0.10f),
    refBg = Color(0xFF0F766E).copy(alpha = 0.045f),
    gold = Color(0xFFA97E3C),
    goldSoft = Color(0xFFA97E3C).copy(alpha = 0.14f),
    error = Color(0xFFDC2626),
    scrim = Color(0xFF0A0F14).copy(alpha = 0.62f),
)

val DarkHadithColors = HadithColors(
    bg = Color(0xFF0A0F14),
    bgTintA = Color(0xFFD4AF7A).copy(alpha = 0.16f),
    bgTintB = Color(0xFF2DD4BF).copy(alpha = 0.12f),
    surface = Color(0xFF172027).copy(alpha = 0.72f),
    surfaceSolid = Color(0xFF131B21),
    border = Color(0xFFE7E5E4).copy(alpha = 0.10f),
    borderStrong = Color(0xFFE7E5E4).copy(alpha = 0.20f),
    text = Color(0xFFECE9E4),
    textSoft = Color(0xFFCFCAC3),
    muted = Color(0xFF9AA3A8),
    accent = Color(0xFF5EEAD4),
    accentInk = Color(0xFF7FE7D6),
    accentSoft = Color(0xFF5EEAD4).copy(alpha = 0.12f),
    refBg = Color(0xFF5EEAD4).copy(alpha = 0.05f),
    gold = Color(0xFFE0BD85),
    goldSoft = Color(0xFFE0BD85).copy(alpha = 0.14f),
    error = Color(0xFFFCA5A5),
    scrim = Color(0xFF0A0F14).copy(alpha = 0.62f),
)

val LocalHadithColors = staticCompositionLocalOf { LightHadithColors }

/** style.css:783-791: background and border are the same in both themes, only the foreground changes. */
@Immutable
data class StatusPillColors(val background: Color, val border: Color, val foreground: Color)

fun statusPillColors(grading: Grading, darkTheme: Boolean): StatusPillColors = when (grading) {
    Grading.SAHIH -> StatusPillColors(
        background = Color(0xFF22C55E).copy(alpha = 0.13f),
        border = Color(0xFF22C55E).copy(alpha = 0.28f),
        foreground = if (darkTheme) Color(0xFF6EE7A8) else Color(0xFF15803D),
    )
    Grading.HASAN -> StatusPillColors(
        background = Color(0xFFEAB308).copy(alpha = 0.15f),
        border = Color(0xFFEAB308).copy(alpha = 0.30f),
        foreground = if (darkTheme) Color(0xFFFCD34D) else Color(0xFFA16207),
    )
    Grading.DAIF -> StatusPillColors(
        background = Color(0xFFEF4444).copy(alpha = 0.12f),
        border = Color(0xFFEF4444).copy(alpha = 0.26f),
        foreground = if (darkTheme) Color(0xFFFCA5A5) else Color(0xFFB91C1C),
    )
    Grading.UNKNOWN -> StatusPillColors(
        background = Color(0xFF94A3B8).copy(alpha = 0.16f),
        border = Color(0xFF94A3B8).copy(alpha = 0.30f),
        foreground = if (darkTheme) Color(0xFFA5B1BD) else Color(0xFF64748B),
    )
}
