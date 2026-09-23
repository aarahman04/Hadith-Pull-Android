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
import online.hadithpull.app.domain.grading
import online.hadithpull.app.ui.theme.HadithShapes
import online.hadithpull.app.ui.theme.statusPillColors

/** §2.2: text = status verbatim, or "Unclassified" if empty. A 6dp dot in the foreground colour before it. */
@Composable
fun StatusPill(status: String, darkTheme: Boolean, modifier: Modifier = Modifier) {
    val colors = statusPillColors(grading(status), darkTheme)
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
            text = status.ifEmpty { "Unclassified" },
            modifier = Modifier.padding(start = 6.dp),
            color = colors.foreground,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.5.sp,
        )
    }
}
