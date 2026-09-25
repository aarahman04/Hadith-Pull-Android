package online.hadithpull.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import online.hadithpull.app.ui.theme.LocalHadithColors

/** Round 3 §0.6: shared shimmer colour for loading placeholders (the Reader's skeleton and the
 * dock's/`ReferenceSummary`'s loading bars), moved out of `ReaderScreen.kt` so it isn't
 * duplicated. */
@Composable
internal fun shimmerColor(): Color {
    val colors = LocalHadithColors.current
    val transition = rememberInfiniteTransition(label = "skeleton")
    val shimmer by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "shimmer",
    )
    return lerp(colors.border, colors.borderStrong, shimmer)
}
