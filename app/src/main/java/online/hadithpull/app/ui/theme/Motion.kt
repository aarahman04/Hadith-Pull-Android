package online.hadithpull.app.ui.theme

import android.content.Context
import android.provider.Settings
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

val HadithEase = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

/** Reduced motion: Settings.Global.ANIMATOR_DURATION_SCALE == 0 snaps every animation above. */
fun isReducedMotion(context: Context): Boolean =
    Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f

/** A duration in ms, snapped to 0 when the system's animator duration scale is 0. */
@Composable
fun motionDurationMs(durationMs: Int): Int {
    val context = LocalContext.current
    return if (isReducedMotion(context)) 0 else durationMs
}
