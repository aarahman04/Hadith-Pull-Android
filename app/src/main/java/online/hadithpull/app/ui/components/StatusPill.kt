package online.hadithpull.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import online.hadithpull.app.data.prefs.HadithGradeFilter
import online.hadithpull.app.domain.PrimaryGrade
import online.hadithpull.app.ui.theme.HadithShapes
import online.hadithpull.app.ui.theme.LocalHadithTypography
import online.hadithpull.app.ui.theme.statusPillColors

/** The label always shows the current narration's stored grade; the menu controls future draws. */
@Composable
fun StatusPill(
    primary: PrimaryGrade?,
    darkTheme: Boolean,
    modifier: Modifier = Modifier,
    gradeFilter: HadithGradeFilter? = null,
    onSetGradeFilter: ((HadithGradeFilter) -> Unit)? = null,
) {
    if (primary == null) return
    val colors = statusPillColors(primary.cat, darkTheme)
    val typography = LocalHadithTypography.current
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        Row(
            modifier = Modifier
                .clip(HadithShapes.pill)
                .then(if (onSetGradeFilter != null) Modifier.clickable { expanded = true } else Modifier)
                .background(colors.background, HadithShapes.pill)
                .border(1.dp, colors.border, HadithShapes.pill)
                .padding(vertical = 6.dp, horizontal = if (onSetGradeFilter != null) 8.dp else 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onSetGradeFilter != null) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowUp,
                    contentDescription = "Choose grade filter",
                    tint = colors.foreground,
                    modifier = Modifier.size(14.dp),
                )
            }
            Box(
                modifier = Modifier
                    .padding(start = if (onSetGradeFilter != null) 1.dp else 0.dp)
                    .size(6.dp)
                    .background(colors.foreground, CircleShape),
            )
            Text(
                text = primary.grade,
                modifier = Modifier.padding(start = 6.dp),
                color = colors.foreground,
                style = typography.statusLabel,
            )
        }
        if (onSetGradeFilter != null && gradeFilter != null) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                offset = DpOffset(0.dp, (-142).dp),
                modifier = Modifier.width(176.dp),
            ) {
                HadithGradeFilter.entries.forEach { option ->
                    val label = when (option) {
                        HadithGradeFilter.SAHIH_ONLY -> "Sahih only"
                        HadithGradeFilter.OTHER_GRADES -> "Other grades"
                        HadithGradeFilter.ALL_GRADES -> "All grades"
                    }
                    DropdownMenuItem(
                        text = { Text(label, style = typography.statusLabel) },
                        onClick = {
                            expanded = false
                            onSetGradeFilter(option)
                        },
                        modifier = Modifier.height(38.dp),
                        trailingIcon = if (gradeFilter == option) {
                            { Icon(Icons.Filled.Check, contentDescription = "Selected", modifier = Modifier.size(14.dp)) }
                        } else null,
                        contentPadding = PaddingValues(horizontal = 12.dp),
                    )
                }
            }
        }
    }
}
