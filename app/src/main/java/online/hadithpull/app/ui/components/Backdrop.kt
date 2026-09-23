package online.hadithpull.app.ui.components

import android.graphics.BitmapShader
import android.graphics.Shader
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import online.hadithpull.app.ui.theme.LocalHadithColors
import kotlin.random.Random

private data class Star(val xFraction: Float, val yFraction: Float, val alpha: Float, val scale: Float)

private val stars: List<Star> = Random(42).let { random ->
    List(70) {
        Star(
            xFraction = random.nextFloat(),
            yFraction = random.nextFloat(),
            alpha = 0.15f + random.nextFloat() * (0.85f - 0.15f),
            scale = 0.6f + random.nextFloat() * (2.0f - 0.6f),
        )
    }
}

/** A 4dp tile with one 1dp dot (style.css:158-164), tiled across the canvas via a BitmapShader. */
private fun grainTileShader(density: Density, dotColor: Color): Shader {
    val tilePx = with(density) { 4.dp.toPx() }.toInt().coerceAtLeast(1)
    val dotRadiusPx = with(density) { 0.5.dp.toPx() }
    val bitmap = ImageBitmap(tilePx, tilePx)
    val canvas = android.graphics.Canvas(bitmap.asAndroidBitmap())
    val paint = android.graphics.Paint().apply {
        color = dotColor.toArgb()
        isAntiAlias = true
    }
    canvas.drawCircle(tilePx / 2f, tilePx / 2f, dotRadiusPx, paint)
    return BitmapShader(bitmap.asAndroidBitmap(), Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
}

private class TiledShaderBrush(private val shader: Shader) : ShaderBrush() {
    override fun createShader(size: Size): Shader = shader
}

/**
 * U3: static ambient backdrop behind all three tab roots. Two radial glow blobs, a paper-grain
 * dot grid, and (dark theme only) a fixed star field. No drift, no twinkle (U3).
 */
@Composable
fun Backdrop(darkTheme: Boolean, modifier: Modifier = Modifier) {
    val colors = LocalHadithColors.current
    val density = LocalDensity.current
    val grainBrush = remember(density, colors.text) {
        TiledShaderBrush(grainTileShader(density, colors.text.copy(alpha = 0.035f)))
    }

    Canvas(modifier = modifier.background(colors.bg)) {
        val maxDim = maxOf(size.width, size.height)

        // §2.6: blob A, diameter 0.92 × max(w,h).
        val radiusA = 0.46f * maxDim
        val centerA = Offset(size.width + 0.12f * maxDim - radiusA, -0.16f * maxDim + radiusA)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(colors.bgTintA, colors.bgTintA.copy(alpha = 0f)),
                center = centerA,
                radius = radiusA,
            ),
            radius = radiusA,
            center = centerA,
        )

        // Blob B, diameter 0.80 × max(w,h), placed bottom-left symmetrically (style.css:174-189).
        val radiusB = 0.40f * maxDim
        val centerB = Offset(-0.12f * maxDim + radiusB, size.height + 0.18f * maxDim - radiusB)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(colors.bgTintB, colors.bgTintB.copy(alpha = 0f)),
                center = centerB,
                radius = radiusB,
            ),
            radius = radiusB,
            center = centerB,
        )

        drawRect(brush = grainBrush)

        if (darkTheme) {
            for (star in stars) {
                drawCircle(
                    color = Color.White.copy(alpha = star.alpha),
                    radius = 1.dp.toPx() * star.scale,
                    center = Offset(star.xFraction * size.width, star.yFraction * size.height),
                )
            }
        }
    }
}
