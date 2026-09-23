package online.hadithpull.app.ui.bookmarks

import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlinx.coroutines.launch
import online.hadithpull.app.data.FolderSummary
import online.hadithpull.app.data.RenameFolderResult
import online.hadithpull.app.data.local.BookmarkEntity
import online.hadithpull.app.data.local.FolderEntity
import online.hadithpull.app.data.toHadith
import online.hadithpull.app.di.AppContainer
import online.hadithpull.app.domain.Hadith
import online.hadithpull.app.ui.share.ShareRoute
import online.hadithpull.app.domain.text.bookmarkExcerpt
import online.hadithpull.app.domain.text.plainText
import online.hadithpull.app.ui.components.HadithBackTopBar
import online.hadithpull.app.ui.components.HadithIcons
import online.hadithpull.app.ui.components.LocalToastState
import online.hadithpull.app.ui.components.StatusPill
import online.hadithpull.app.ui.components.ToastState
import online.hadithpull.app.ui.theme.HadithShapes
import online.hadithpull.app.ui.theme.LocalHadithColors
import online.hadithpull.app.ui.theme.LocalHadithTypography

private val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

/** §2.4 Folder detail. Pops back to Folders if the folder stops existing (e.g. deleted elsewhere). */
@Composable
fun FolderRoute(container: AppContainer, darkTheme: Boolean, folderId: Long, onBack: () -> Unit) {
    val viewModel: BookmarksViewModel = viewModel(
        factory = viewModelFactory { initializer { BookmarksViewModel(container.bookmarkRepository) } },
    )
    val toastState = LocalToastState.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var folder by remember { mutableStateOf<FolderEntity?>(null) }
    var folderLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(folderId) {
        viewModel.folder(folderId).collect {
            folder = it
            folderLoaded = true
        }
    }
    LaunchedEffect(folderLoaded, folder) {
        if (folderLoaded && folder == null) onBack()
    }

    val items by viewModel.items(folderId).collectAsState(initial = emptyList())
    val allFolders by viewModel.folders.collectAsState()

    var renaming by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }
    var expandedIds by remember { mutableStateOf(setOf<Long>()) }
    var shareSheetHadith by remember { mutableStateOf<Hadith?>(null) }

    val currentFolder = folder ?: return

    Column(Modifier.fillMaxSize()) {
        HadithBackTopBar(title = currentFolder.name, onBack = onBack) {
            IconButton(onClick = { renaming = true }) {
                Icon(painterResource(HadithIcons.pencil), contentDescription = "Rename")
            }
            IconButton(onClick = { deleting = true }) {
                Icon(painterResource(HadithIcons.bin), contentDescription = "Delete")
            }
        }
        FolderDetailBody(
            darkTheme = darkTheme,
            countLabel = if (items.size == 1) "1 Hadith" else "${items.size} Hadiths",
            items = items,
            otherFolders = allFolders.filter { it.id != folderId },
            expandedIds = expandedIds,
            onToggleExpanded = { id ->
                expandedIds = if (id in expandedIds) expandedIds - id else expandedIds + id
            },
            onCopy = { item -> copyToClipboard(context, item, toastState) },
            onShare = { item -> shareSheetHadith = item.toHadith() },
            onMove = { item, targetId, targetName ->
                scope.launch {
                    viewModel.moveItem(item.id, targetId)
                    toastState.show("Moved to $targetName")
                }
            },
            onRemove = { item ->
                scope.launch {
                    viewModel.removeItem(item.id)
                    toastState.show("Removed from ${currentFolder.name}")
                }
            },
        )
    }

    if (renaming) {
        RenameFolderDialog(
            currentName = currentFolder.name,
            onDismiss = { renaming = false },
            onConfirm = { name ->
                scope.launch {
                    when (viewModel.renameFolder(folderId, name)) {
                        is RenameFolderResult.Duplicate -> toastState.show("A folder with that name already exists")
                        is RenameFolderResult.Renamed -> renaming = false
                        is RenameFolderResult.Invalid -> Unit
                    }
                }
            },
        )
    }

    if (deleting) {
        DeleteFolderDialog(
            folderName = currentFolder.name,
            onDismiss = { deleting = false },
            onConfirm = {
                scope.launch {
                    viewModel.deleteFolder(folderId)
                    toastState.show("Folder deleted")
                    deleting = false
                    onBack()
                }
            },
        )
    }

    shareSheetHadith?.let { hadith ->
        ShareRoute(container = container, hadith = hadith, onDismiss = { shareSheetHadith = null })
    }
}

@Composable
private fun FolderDetailBody(
    darkTheme: Boolean,
    countLabel: String,
    items: List<BookmarkEntity>,
    otherFolders: List<FolderSummary>,
    expandedIds: Set<Long>,
    onToggleExpanded: (Long) -> Unit,
    onCopy: (BookmarkEntity) -> Unit,
    onShare: (BookmarkEntity) -> Unit,
    onMove: (BookmarkEntity, Long, String) -> Unit,
    onRemove: (BookmarkEntity) -> Unit,
) {
    val colors = LocalHadithColors.current
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(12.dp))
        Text(text = countLabel, color = colors.muted, fontSize = 14.4.sp)
        Spacer(Modifier.height(12.dp))
        if (items.isEmpty()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(colors.surface, HadithShapes.lg)
                    .border(1.dp, colors.border, HadithShapes.lg)
                    .padding(24.dp),
            ) {
                Text(text = "Nothing saved here yet.", color = colors.textSoft)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                items(items, key = { it.id }) { item ->
                    BookmarkItemCard(
                        item = item,
                        darkTheme = darkTheme,
                        expanded = item.id in expandedIds,
                        otherFolders = otherFolders,
                        onToggleExpanded = { onToggleExpanded(item.id) },
                        onCopy = { onCopy(item) },
                        onShare = { onShare(item) },
                        onMove = { targetId, targetName -> onMove(item, targetId, targetName) },
                        onRemove = { onRemove(item) },
                    )
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
private fun BookmarkItemCard(
    item: BookmarkEntity,
    darkTheme: Boolean,
    expanded: Boolean,
    otherFolders: List<FolderSummary>,
    onToggleExpanded: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onMove: (Long, String) -> Unit,
    onRemove: () -> Unit,
) {
    val colors = LocalHadithColors.current
    val typography = LocalHadithTypography.current
    val short = remember(item.english) { bookmarkExcerpt(item.english, 320) }
    val canExpand = short != item.english || item.arabic.isNotEmpty()
    var moveMenuOpen by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxWidth()
            .background(colors.surface, RoundedCornerShape(26.dp))
            .border(1.dp, colors.border, RoundedCornerShape(26.dp))
            .padding(18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            val refText = buildString {
                append(item.book)
                if (item.number.isNotEmpty()) append("  ·  Hadith ${item.number}")
                if (item.chapter.isNotEmpty()) append("  ·  ${item.chapter}")
            }
            Text(
                text = refText,
                color = colors.textSoft,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.4.sp,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            StatusPill(status = item.status, darkTheme = darkTheme)
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = if (expanded) item.english else short,
            style = typography.english,
            color = colors.text,
            fontSize = 18.4.sp,
            lineHeight = (18.4 * 1.65).sp,
        )
        if (expanded && item.arabic.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = item.arabic,
                style = typography.arabic.copy(textAlign = TextAlign.Right, textDirection = TextDirection.Rtl),
                fontSize = 22.4.sp,
                color = colors.text,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (item.narrator.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Text(text = item.narrator, color = colors.muted, fontStyle = FontStyle.Italic, fontSize = 14.sp)
        }
        Spacer(Modifier.height(14.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Saved ${dateFormatter.format(Instant.ofEpochMilli(item.savedAt).atZone(ZoneId.systemDefault()).toLocalDate())}",
                color = colors.muted,
                fontSize = 12.5.sp,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(17.dp)) {
                if (canExpand) {
                    FooterAction(if (expanded) "Show less" else "Show full", onToggleExpanded)
                }
                FooterAction("Copy", onCopy)
                FooterAction("Share", onShare)
                Box {
                    if (otherFolders.isNotEmpty()) {
                        FooterAction("Move to…") { moveMenuOpen = true }
                        DropdownMenu(expanded = moveMenuOpen, onDismissRequest = { moveMenuOpen = false }) {
                            otherFolders.forEach { target ->
                                DropdownMenuItem(
                                    text = { Text(target.name) },
                                    onClick = {
                                        moveMenuOpen = false
                                        onMove(target.id, target.name)
                                    },
                                )
                            }
                        }
                    }
                }
                FooterAction("Remove", onRemove)
            }
        }
    }
}

@Composable
private fun FooterAction(label: String, onClick: () -> Unit) {
    val colors = LocalHadithColors.current
    Text(
        text = label,
        color = colors.muted,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        modifier = Modifier.clickable(onClick = onClick),
    )
}

private fun copyToClipboard(context: android.content.Context, item: BookmarkEntity, toastState: ToastState) {
    val clipboard = context.getSystemService(ClipboardManager::class.java)
    clipboard.setPrimaryClip(ClipData.newPlainText("Hadith", plainText(item.toHadith())))
    if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.S_V2) {
        toastState.show("Hadith copied to clipboard")
    }
}
