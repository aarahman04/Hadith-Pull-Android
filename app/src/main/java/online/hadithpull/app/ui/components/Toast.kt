package online.hadithpull.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import online.hadithpull.app.ui.theme.HadithShapes
import online.hadithpull.app.ui.theme.LocalHadithColors
import online.hadithpull.app.ui.theme.LocalHadithTypography

private const val TOAST_VISIBLE_MS = 2600L
private const val TOAST_TRANSITION_MS = 300

/** §2.6: a custom pill toast. A new toast replaces the current one. */
class ToastState(private val scope: CoroutineScope) {
    var message by mutableStateOf<String?>(null)
        private set
    private var hideJob: Job? = null

    fun show(text: String) {
        hideJob?.cancel()
        message = text
        hideJob = scope.launch {
            delay(TOAST_VISIBLE_MS)
            message = null
        }
    }
}

@Composable
fun rememberToastState(): ToastState {
    val scope = rememberCoroutineScope()
    return remember { ToastState(scope) }
}

val LocalToastState = staticCompositionLocalOf<ToastState> { error("No ToastState provided") }

/** Bottom-centre host, above the nav bar with a 24dp offset (placement is the caller's job). */
@Composable
fun ToastHost(state: ToastState, modifier: Modifier = Modifier) {
    val colors = LocalHadithColors.current
    val typography = LocalHadithTypography.current
    val density = LocalDensity.current
    AnimatedVisibility(
        visible = state.message != null,
        enter = fadeIn(tween(TOAST_TRANSITION_MS)) +
            slideInVertically(tween(TOAST_TRANSITION_MS)) { with(density) { 20.dp.roundToPx() } },
        exit = fadeOut(tween(TOAST_TRANSITION_MS)),
        modifier = modifier.padding(horizontal = 16.dp),
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(colors.surfaceSolid, HadithShapes.pill)
                .border(1.dp, colors.borderStrong, HadithShapes.pill)
                .padding(vertical = 6.dp, horizontal = 12.dp),
        ) {
            Text(
                text = state.message.orEmpty(),
                color = colors.text,
                style = typography.secondaryScaled.copy(lineHeight = 18.sp),
                textAlign = TextAlign.Center,
            )
        }
    }
}
