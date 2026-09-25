package online.hadithpull.app.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import online.hadithpull.app.ui.theme.LocalHadithColors

/** Round 3 §0.6: the one "+ New folder" pattern, used by the Save sheet and the Bookmarks root
 * instead of an always-visible create row. */
@Composable
fun NewFolderField(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!expanded) {
        TertiaryLink(
            label = "New folder",
            leadingIcon = rememberVectorPainter(Icons.Filled.Add),
            onClick = { onExpandedChange(true) },
            modifier = modifier,
        )
        return
    }

    val colors = LocalHadithColors.current
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(expanded) {
        if (expanded) focusRequester.requestFocus()
    }

    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("New folder name…") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onSubmit() }),
            trailingIcon = {
                IconButton(onClick = {
                    onValueChange("")
                    onExpandedChange(false)
                }) {
                    Icon(Icons.Filled.Close, contentDescription = "Cancel", tint = colors.muted, modifier = Modifier.size(18.dp))
                }
            },
            modifier = Modifier.weight(1f).focusRequester(focusRequester),
        )
        Spacer(Modifier.width(8.dp))
        TextButton(onClick = onSubmit) {
            Text("Create", color = colors.accent, fontWeight = FontWeight.Medium)
        }
    }
}
