package online.hadithpull.app.ui.bookmarks

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import online.hadithpull.app.ui.theme.LocalHadithColors

private const val MAX_FOLDER_NAME_LENGTH = 40

/** §2.4: rename dialog, field prefilled and selected, disabled while trimmed text is empty. */
@Composable
fun RenameFolderDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var text by remember {
        mutableStateOf(TextFieldValue(currentName, selection = androidx.compose.ui.text.TextRange(0, currentName.length)))
    }
    val colors = LocalHadithColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename folder") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { if (it.text.length <= MAX_FOLDER_NAME_LENGTH) text = it },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(text.text) },
                enabled = text.text.trim().isNotEmpty(),
            ) {
                Text("Rename", color = colors.accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

/** §2.4: delete confirmation, "Delete" in the error colour. */
@Composable
fun DeleteFolderDialog(
    folderName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val colors = LocalHadithColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { Text("Delete \"$folderName\" and everything saved inside it?") },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Delete", color = colors.error) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
