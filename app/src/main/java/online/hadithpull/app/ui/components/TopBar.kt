package online.hadithpull.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import online.hadithpull.app.R
import online.hadithpull.app.data.prefs.Theme
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
 * bg at 78% alpha over content, no border divider (Round 3 §0.7 lightens the top bars), no blur
 * (API 31+ only; skipped).
 */
@Composable
fun HadithTopBar(
    darkTheme: Boolean,
    theme: Theme,
    onToggleTheme: () -> Unit,
    onSetTheme: (Theme) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalHadithColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.bg.copy(alpha = 0.78f))
            .statusBarsPadding()
            .heightIn(min = 56.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BrandMark()
        Text(
            text = "Hadith Pull",
            modifier = Modifier.padding(start = 10.dp),
            color = colors.text,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
        )
        Spacer(Modifier.weight(1f))
        ThemeToggleButton(darkTheme = darkTheme, theme = theme, onToggle = onToggleTheme, onSetTheme = onSetTheme)
    }
}

/** Pushed-screen top bar (Folder detail, Licenses): back arrow, title, trailing actions. */
@Composable
fun HadithBackTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val colors = LocalHadithColors.current
    val typography = online.hadithpull.app.ui.theme.LocalHadithTypography.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.bg.copy(alpha = 0.78f))
            .statusBarsPadding()
            .heightIn(min = 56.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.text)
        }
        Text(
            text = title,
            style = typography.sheetTitle,
            modifier = Modifier.weight(1f),
            color = colors.text,
            fontSize = 19.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        actions()
    }
}

/** Round 3 §0.7/R3-Q1: a tap flips light/dark (unchanged); a long-press opens a menu to return to
 * System, or to jump straight to Light/Dark, with a check on the current setting. This is the
 * only way back to "System" once the header toggle has been tapped, since About's Appearance
 * section is removed this round. */
@Composable
private fun ThemeToggleButton(darkTheme: Boolean, theme: Theme, onToggle: () -> Unit, onSetTheme: (Theme) -> Unit) {
    val colors = LocalHadithColors.current
    val icon: Painter = painterResource(if (darkTheme) HadithIcons.moon else HadithIcons.sun)
    var menuOpen by remember { mutableStateOf(false) }
    Box {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .combinedClickable(
                    onClick = onToggle,
                    onLongClick = { menuOpen = true },
                    onClickLabel = "Toggle dark mode",
                    onLongClickLabel = "Choose theme",
                    role = Role.Button,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painter = icon, contentDescription = "Toggle dark mode", tint = colors.textSoft, modifier = Modifier.size(20.dp))
        }
        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
            listOf(Theme.SYSTEM to "Match system", Theme.LIGHT to "Light", Theme.DARK to "Dark").forEach { (value, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = { menuOpen = false; onSetTheme(value) },
                    trailingIcon = {
                        if (value == theme) {
                            Icon(
                                painter = painterResource(HadithIcons.check),
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    },
                )
            }
        }
    }
}
