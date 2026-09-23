package online.hadithpull.app.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import online.hadithpull.app.data.BookmarkRepository
import online.hadithpull.app.data.CreateFolderResult
import online.hadithpull.app.data.FolderSummary
import online.hadithpull.app.domain.Hadith
import online.hadithpull.app.ui.components.HadithIcons
import online.hadithpull.app.ui.components.ToastState
import online.hadithpull.app.ui.theme.HadithShapes
import online.hadithpull.app.ui.theme.LocalHadithColors
import online.hadithpull.app.ui.theme.LocalHadithTypography

private const val MAX_FOLDER_NAME_LENGTH = 40

/** §2.3: Reader-only Save sheet, toggling the hadith in and out of folders. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveSheet(
    hadith: Hadith,
    bookmarkRepository: BookmarkRepository,
    toastState: ToastState,
    onDismiss: () -> Unit,
    onManageBookmarks: () -> Unit,
) {
    val colors = LocalHadithColors.current
    val typography = LocalHadithTypography.current
    val scope = rememberCoroutineScope()

    val folders by remember(bookmarkRepository) { bookmarkRepository.folders() }.collectAsState(initial = emptyList())
    val savedFolderIds by remember(bookmarkRepository, hadith.key) {
        bookmarkRepository.savedFolderIds(hadith.key)
    }.collectAsState(initial = emptySet())

    var newFolderName by remember { mutableStateOf("") }

    fun submitCreate() {
        val name = newFolderName
        scope.launch {
            when (val result = bookmarkRepository.createFolder(name)) {
                is CreateFolderResult.Created -> {
                    bookmarkRepository.ensureSaved(result.folder.id, hadith)
                    toastState.show("Folder \"${result.folder.name}\" created")
                    newFolderName = ""
                }
                is CreateFolderResult.Existing -> {
                    bookmarkRepository.ensureSaved(result.folder.id, hadith)
                    toastState.show("Saved to ${result.folder.name}")
                    newFolderName = ""
                }
                CreateFolderResult.Invalid -> Unit
            }
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 28.dp)) {
            Text(text = "Save to a folder", style = typography.sheetTitle, color = colors.text)
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Keep this Hadith somewhere you'll find it again.",
                color = colors.muted,
                fontSize = 13.6.sp,
            )
            Spacer(Modifier.height(18.dp))

            if (folders.isEmpty()) {
                Text(text = "No folders yet — create one below.", color = colors.muted, fontSize = 14.sp)
            } else {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = 260.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    folders.forEachIndexed { index, folder ->
                        if (index > 0) Spacer(Modifier.height(8.dp))
                        SaveSheetFolderRow(
                            folder = folder,
                            checked = folder.id in savedFolderIds,
                            onClick = {
                                scope.launch {
                                    val nowSaved = bookmarkRepository.toggle(folder.id, hadith)
                                    toastState.show(
                                        if (nowSaved) "Saved to ${folder.name}" else "Removed from ${folder.name}",
                                    )
                                }
                            },
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { if (it.length <= MAX_FOLDER_NAME_LENGTH) newFolderName = it },
                    placeholder = { Text("New folder name…") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { submitCreate() }),
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(10.dp))
                TextButton(onClick = { submitCreate() }) {
                    Text("Create", color = colors.accent, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(18.dp))
            Text(
                text = "Manage all bookmarks →",
                color = colors.accent,
                fontSize = 13.6.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onDismiss()
                        onManageBookmarks()
                    },
            )
        }
    }
}

@Composable
private fun SaveSheetFolderRow(folder: FolderSummary, checked: Boolean, onClick: () -> Unit) {
    val colors = LocalHadithColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (checked) colors.accentSoft else colors.bg, HadithShapes.md)
            .border(1.dp, colors.border, HadithShapes.md)
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp, horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(if (checked) colors.accent else Color.Transparent, CircleShape)
                .border(1.5.dp, if (checked) colors.accent else colors.borderStrong, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) {
                Icon(
                    painter = painterResource(HadithIcons.check),
                    contentDescription = null,
                    tint = colors.bg,
                    modifier = Modifier.size(12.dp),
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = folder.name,
            color = colors.text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        Text(text = folder.count.toString(), color = colors.muted, fontSize = 12.8.sp)
    }
}
