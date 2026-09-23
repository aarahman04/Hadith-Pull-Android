package online.hadithpull.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import online.hadithpull.app.R
import online.hadithpull.app.ui.theme.LocalHadithColors

private val brandTileFill = Color(0xFFF6F2EA)

/** U4: 30dp tile, 9dp radius, 1dp borderStrong border, same fill in both themes; brand_logo at 24dp. */
@Composable
private fun BrandMark() {
    val colors = LocalHadithColors.current
    Box(
        modifier = Modifier
            .size(30.dp)
            .background(brandTileFill, RoundedCornerShape(9.dp))
            .border(1.dp, colors.borderStrong, RoundedCornerShape(9.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.brand_logo),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            contentScale = ContentScale.Fit,
        )
    }
}

/**
 * The web header, ported: brand mark + title on the left, theme toggle on the right.
 * bg at 78% alpha over content, 1dp border bottom divider, no blur (API 31+ only; skipped).
 */
@Composable
fun HadithTopBar(
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalHadithColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.bg.copy(alpha = 0.78f))
            .drawBehind {
                drawLine(
                    color = colors.border,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BrandMark()
        Text(
            text = "Hadith Pull",
            modifier = Modifier.padding(start = 10.dp),
            color = colors.text,
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
        )
        Spacer(Modifier.weight(1f))
        ThemeToggleButton(darkTheme = darkTheme, onToggle = onToggleTheme)
    }
}

@Composable
private fun ThemeToggleButton(darkTheme: Boolean, onToggle: () -> Unit) {
    val colors = LocalHadithColors.current
    val icon: Painter = painterResource(if (darkTheme) HadithIcons.moon else HadithIcons.sun)
    IconButton(onClick = onToggle) {
        Icon(painter = icon, contentDescription = "Toggle dark mode", tint = colors.text)
    }
}
