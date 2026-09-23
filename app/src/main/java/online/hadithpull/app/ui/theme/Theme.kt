package online.hadithpull.app.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import online.hadithpull.app.data.prefs.ArabicScript
import online.hadithpull.app.data.prefs.TextSize

val LocalHadithTypography = staticCompositionLocalOf<HadithTypography> {
    error("HadithTheme not applied")
}

/** The theme swap cross-fades every token over 500ms (§2.6), snapped by reduced-motion. */
@Composable
private fun animatedHadithColors(target: HadithColors): HadithColors {
    val duration = motionDurationMs(500)
    val spec = tween<Color>(duration, easing = HadithEase)
    @Composable fun animate(color: Color) = animateColorAsState(color, spec).value
    return HadithColors(
        bg = animate(target.bg),
        bgTintA = animate(target.bgTintA),
        bgTintB = animate(target.bgTintB),
        surface = animate(target.surface),
        surfaceSolid = animate(target.surfaceSolid),
        border = animate(target.border),
        borderStrong = animate(target.borderStrong),
        text = animate(target.text),
        textSoft = animate(target.textSoft),
        muted = animate(target.muted),
        accent = animate(target.accent),
        accentInk = animate(target.accentInk),
        accentSoft = animate(target.accentSoft),
        refBg = animate(target.refBg),
        gold = animate(target.gold),
        goldSoft = animate(target.goldSoft),
        error = animate(target.error),
        scrim = animate(target.scrim),
    )
}

@Composable
fun HadithTheme(
    darkTheme: Boolean,
    arabicScript: ArabicScript = ArabicScript.NASKH,
    textSize: TextSize = TextSize.COMFORTABLE,
    content: @Composable () -> Unit,
) {
    val targetColors = if (darkTheme) DarkHadithColors else LightHadithColors
    val hadithColors = animatedHadithColors(targetColors)

    val assets = LocalContext.current.assets
    val windowWidthDp = LocalConfiguration.current.screenWidthDp.toFloat()
    val arabicMetrics = arabicScriptMetrics(arabicScript)

    val inter = remember(assets) { interFamily(assets) }
    val cormorant = remember(assets) { cormorantFamily(assets) }
    val arabicFamily = remember(assets, arabicScript) { arabicFontFamily(assets, arabicScript) }

    val typography = remember(windowWidthDp, inter, cormorant, arabicFamily, textSize, arabicMetrics) {
        hadithTypography(
            windowWidthDp = windowWidthDp,
            inter = inter,
            cormorant = cormorant,
            arabicFamily = arabicFamily,
            readingScale = textSize.scale,
            arabicScale = arabicMetrics.scale,
            arabicLineHeight = arabicMetrics.lineHeight,
        )
    }

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            background = hadithColors.bg,
            surface = hadithColors.surfaceSolid,
            onBackground = hadithColors.text,
            onSurface = hadithColors.text,
            primary = hadithColors.accent,
            outline = hadithColors.borderStrong,
            error = hadithColors.error,
        )
    } else {
        lightColorScheme(
            background = hadithColors.bg,
            surface = hadithColors.surfaceSolid,
            onBackground = hadithColors.text,
            onSurface = hadithColors.text,
            primary = hadithColors.accent,
            outline = hadithColors.borderStrong,
            error = hadithColors.error,
        )
    }

    CompositionLocalProvider(
        LocalHadithColors provides hadithColors,
        LocalHadithTypography provides typography,
    ) {
        MaterialTheme(colorScheme = colorScheme, shapes = hadithMaterialShapes, content = content)
    }
}
