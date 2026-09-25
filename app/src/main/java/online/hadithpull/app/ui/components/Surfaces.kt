package online.hadithpull.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import online.hadithpull.app.ui.theme.HadithShapes
import online.hadithpull.app.ui.theme.LocalHadithColors

/** Round 3 §0.2: the one card/surface language, used for every grouped-content block across all
 * four screens. `selected` draws the accent-soft fill + accent border used by the Save sheet's
 * folder rows. */
@Composable
fun HadithCard(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalHadithColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surfaceSolid, HadithShapes.md)
            .then(if (selected) Modifier.background(colors.accentSoft, HadithShapes.md) else Modifier)
            .border(1.dp, if (selected) colors.accent.copy(alpha = 0.40f) else colors.border, HadithShapes.md)
            .padding(contentPadding),
        content = content,
    )
}

/** A `HadithCard` with no inner padding, for a stack of `ListRow`s separated by `RowDivider`. */
@Composable
fun GroupedList(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    HadithCard(modifier = modifier, contentPadding = PaddingValues(0.dp), content = content)
}

/** One row inside a `GroupedList`: an optional leading slot, a title (+ optional subtitle), and
 * an optional trailing slot (a `ChevronTrailing`/`ExternalTrailing` icon, typically). */
@Composable
fun ListRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = LocalHadithColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(14.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                color = colors.text,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = colors.muted,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(12.dp))
            trailing()
        }
    }
}

@Composable
fun RowDivider() {
    val colors = LocalHadithColors.current
    Box(
        Modifier
            .fillMaxWidth()
            .padding(start = 16.dp)
            .height(1.dp)
            .background(colors.border),
    )
}

@Composable
fun ChevronTrailing() {
    val colors = LocalHadithColors.current
    Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null,
        tint = colors.muted,
        modifier = Modifier.size(18.dp),
    )
}

@Composable
fun ExternalTrailing() {
    val colors = LocalHadithColors.current
    Icon(
        painter = painterResource(HadithIcons.openInNew),
        contentDescription = null,
        tint = colors.muted,
        modifier = Modifier.size(16.dp),
    )
}
