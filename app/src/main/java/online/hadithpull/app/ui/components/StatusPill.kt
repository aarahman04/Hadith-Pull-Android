package online.hadithpull.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import online.hadithpull.app.domain.PrimaryGrade
import online.hadithpull.app.ui.theme.HadithShapes
import online.hadithpull.app.ui.theme.statusPillColors

/** §4/H8: text = primary.grade verbatim; null means no pill (the "Unclassified" fallback is deleted). */
@Composable
fun StatusPill(primary: PrimaryGrade?, darkTheme: Boolean, modifier: Modifier = Modifier) {
    if (primary == null) return
    val colors = statusPillColors(primary.cat, darkTheme)
    Row(
        modifier = modifier
            .background(colors.background, HadithShapes.pill)
            .border(1.dp, colors.border, HadithShapes.pill)
            .padding(vertical = 6.dp, horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(colors.foreground, CircleShape),
        )
        Text(
            text = primary.grade,
            modifier = Modifier.padding(start = 6.dp),
            color = colors.foreground,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.5.sp,
        )
    }
}
