package online.hadithpull.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import online.hadithpull.app.ui.theme.LocalHadithColors
import online.hadithpull.app.ui.theme.LocalHadithTypography

/** Round 3 §0.3: the one section-header pattern (eyebrow + title + optional lede) used
 * identically wherever a screen introduces a new grouping. Start-aligned, always. */
@Composable
fun SectionHeader(
    eyebrow: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    lede: String? = null,
    action: (@Composable () -> Unit)? = null,
) {
    val colors = LocalHadithColors.current
    val typography = LocalHadithTypography.current
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        Column(Modifier.weight(1f)) {
            Text(text = eyebrow.uppercase(), style = typography.sectionLabel, color = colors.accentInk)
            if (title != null) {
                Spacer(Modifier.height(4.dp))
                Text(text = title, style = typography.panelTitle, color = colors.text)
            }
            if (lede != null) {
                Spacer(Modifier.height(6.dp))
                Text(text = lede, fontSize = 14.sp, lineHeight = 21.sp, color = colors.muted)
            }
        }
        if (action != null) action()
    }
}

/** Round 3 §0.3: the eyebrow-with-hairlines block shared by every screen's top hero, replacing
 * the copies that used to live in `ReaderScreen.Hero`, `FoldersScreen` and `AboutScreen`. */
@Composable
fun ScreenHero(eyebrow: String, title: String, titleStyle: TextStyle, subline: String?) {
    val colors = LocalHadithColors.current
    val typography = LocalHadithTypography.current
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(28.dp).height(1.dp).background(colors.borderStrong))
            Text(
                text = eyebrow,
                style = typography.eyebrow,
                color = colors.muted,
                modifier = Modifier.padding(horizontal = 10.dp),
            )
            Box(Modifier.width(28.dp).height(1.dp).background(colors.borderStrong))
        }
        Spacer(Modifier.height(12.dp))
        Text(text = title, style = titleStyle, color = colors.text, textAlign = TextAlign.Center)
        if (subline != null) {
            Spacer(Modifier.height(10.dp))
            Text(text = subline, style = typography.body, color = colors.textSoft, textAlign = TextAlign.Center)
        }
    }
}
