package online.hadithpull.app.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import online.hadithpull.app.data.prefs.TextSize
import online.hadithpull.app.ui.components.HadithPrimaryButton
import online.hadithpull.app.ui.theme.LocalHadithColors
import online.hadithpull.app.ui.theme.LocalHadithTypography

/** Uses the same sheet surface and dismiss animation as the Read screen's Save sheet. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingPreferencesSheet(
    textSize: TextSize,
    onSetTextSize: (TextSize) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalHadithColors.current
    val typography = LocalHadithTypography.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTextSize by remember { mutableStateOf(textSize) }

    fun close() {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.bg,
        scrimColor = colors.scrim.copy(alpha = 0.45f),
        dragHandle = { BottomSheetDefaults.DragHandle(width = 36.dp, height = 4.dp, color = colors.borderStrong) },
    ) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp).padding(bottom = 20.dp)) {
            Text("Reading appearance", style = typography.sectionTitle.copy(fontSize = 20.sp), color = colors.text)
            Spacer(Modifier.height(22.dp))
            Text("Text size", style = typography.optionLabel, color = colors.textSoft)
            Spacer(Modifier.height(10.dp))
            PreferenceChoices(TextSize.entries, selectedTextSize) { size ->
                selectedTextSize = size
                onSetTextSize(size)
            }

            Spacer(Modifier.height(26.dp))
            HadithPrimaryButton(text = "Done", onClick = ::close, compact = true)
        }
    }
}

@Composable
private fun <T : Enum<T>> PreferenceChoices(options: List<T>, selected: T, onSelect: (T) -> Unit) {
    val stacked = LocalDensity.current.fontScale >= 1.3f || LocalConfiguration.current.screenWidthDp < 360
    if (stacked) {
        Column(Modifier.fillMaxWidth().selectableGroup(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                PreferenceChoice(
                    label = option.name.lowercase().replaceFirstChar { it.uppercase() },
                    selected = selected == option,
                    onClick = { onSelect(option) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    } else {
        Row(Modifier.fillMaxWidth().selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                PreferenceChoice(
                    label = option.name.lowercase().replaceFirstChar { it.uppercase() },
                    selected = selected == option,
                    onClick = { onSelect(option) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun PreferenceChoice(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalHadithColors.current
    val shape = RoundedCornerShape(12.dp)
    Text(
        text = label,
        color = if (selected) colors.accentInk else colors.textSoft,
        fontSize = 13.sp,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .background(if (selected) colors.accentSoft else colors.surfaceSolid, shape)
            .border(1.dp, if (selected) colors.accent.copy(alpha = 0.35f) else colors.border, shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .heightIn(min = 48.dp)
            .padding(horizontal = 4.dp, vertical = 14.dp),
    )
}
