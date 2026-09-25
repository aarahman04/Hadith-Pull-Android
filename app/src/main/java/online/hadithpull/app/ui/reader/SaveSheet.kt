package online.hadithpull.app.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import kotlinx.coroutines.launch
import online.hadithpull.app.data.BookmarkRepository
import online.hadithpull.app.data.CreateFolderResult
import online.hadithpull.app.data.FolderSummary
import online.hadithpull.app.domain.Hadith
import online.hadithpull.app.ui.components.HadithCard
import online.hadithpull.app.ui.components.HadithIcons
import online.hadithpull.app.ui.components.HadithPrimaryButton
import online.hadithpull.app.ui.components.NewFolderField
import online.hadithpull.app.ui.components.TertiaryLink
import online.hadithpull.app.ui.components.ToastHost
import online.hadithpull.app.ui.components.ToastState
import online.hadithpull.app.ui.theme.LocalHadithColors
import online.hadithpull.app.ui.theme.LocalHadithTypography

private const val MAX_FOLDER_NAME_LENGTH = 40

/** §2.3/Round 3 §Step 30: Reader-only Save sheet, toggling the hadith in and out of folders
 * (tap-to-save, R3-Q2), with a primary "Done" that only closes the sheet. */
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
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val folders by remember(bookmarkRepository) { bookmarkRepository.folders() }.collectAsState(initial = emptyList())
    val savedFolderIds by remember(bookmarkRepository, hadith.key) {
        bookmarkRepository.savedFolderIds(hadith.key)
    }.collectAsState(initial = emptySet())

    var newFolderName by remember { mutableStateOf("") }
    var creatingFolder by remember { mutableStateOf(false) }

    fun close(then: () -> Unit = {}) {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss(); then() }
    }

    fun submitCreate() {
        val name = newFolderName
        scope.launch {
            when (val result = bookmarkRepository.createFolder(name)) {
                is CreateFolderResult.Created -> {
                    bookmarkRepository.ensureSaved(result.folder.id, hadith)
                    toastState.show("Folder \"${result.folder.name}\" created")
                    newFolderName = ""
                    creatingFolder = false
                }
                is CreateFolderResult.Existing -> {
                    bookmarkRepository.ensureSaved(result.folder.id, hadith)
                    toastState.show("Saved to ${result.folder.name}")
                    newFolderName = ""
                    creatingFolder = false
                }
                CreateFolderResult.Invalid -> Unit
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.bg,
        dragHandle = { BottomSheetDefaults.DragHandle(width = 36.dp, height = 4.dp, color = colors.borderStrong) },
    ) {
        Box(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
            Text(text = "Save to a folder", style = typography.sheetTitle, color = colors.text)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Keep this Hadith somewhere you'll find it again.",
                color = colors.muted,
                fontSize = 13.6.sp,
            )
            Spacer(Modifier.height(20.dp))

            if (folders.isEmpty()) {
                Text(text = "No folders yet. Create one below.", color = colors.muted, fontSize = 14.sp)
            } else {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    folders.forEach { folder ->
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

            Spacer(Modifier.height(12.dp))
            NewFolderField(
                expanded = creatingFolder,
                onExpandedChange = { creatingFolder = it },
                value = newFolderName,
                onValueChange = { if (it.length <= MAX_FOLDER_NAME_LENGTH) newFolderName = it },
                onSubmit = { submitCreate() },
            )

            Spacer(Modifier.height(20.dp))
            HadithPrimaryButton(text = "Done", onClick = { close() })

            Spacer(Modifier.height(4.dp))
            TertiaryLink(
                label = "Manage all bookmarks",
                onClick = { close(onManageBookmarks) },
                trailingIcon = rememberVectorPainter(Icons.AutoMirrored.Filled.KeyboardArrowRight),
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
        ToastHost(
            state = toastState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp),
        )
        }
    }
}

@Composable
private fun SaveSheetFolderRow(folder: FolderSummary, checked: Boolean, onClick: () -> Unit) {
    val colors = LocalHadithColors.current
    HadithCard(
        selected = checked,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Row(Modifier.fillMaxWidth().heightIn(min = 40.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(if (checked) colors.accent else colors.accentSoft, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(if (checked) HadithIcons.bookmarkFilled else HadithIcons.bookmark),
                    contentDescription = null,
                    tint = if (checked) colors.bg else colors.accent,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = folder.name,
                    color = colors.text,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = if (folder.count == 1) "1 Hadith" else "${folder.count} Hadiths",
                    color = colors.muted,
                    fontSize = 13.sp,
                )
            }
            Spacer(Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(if (checked) colors.accent else Color.Transparent, CircleShape)
                    .border(1.5.dp, if (checked) colors.accent else colors.borderStrong, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (checked) {
                    Icon(
                        painter = painterResource(HadithIcons.check),
                        contentDescription = null,
                        tint = colors.bg,
                        modifier = Modifier.size(13.dp),
                    )
                }
            }
        }
    }
}
