package online.hadithpull.app.ui.bookmarks

import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import online.hadithpull.app.data.FolderSummary
import online.hadithpull.app.data.RenameFolderResult
import online.hadithpull.app.data.library.LibraryExport
import online.hadithpull.app.data.local.BookmarkEntity
import online.hadithpull.app.data.local.FolderEntity
import online.hadithpull.app.data.toHadith
import online.hadithpull.app.di.AppContainer
import online.hadithpull.app.domain.Hadith
import online.hadithpull.app.share.writeLibraryToCache
import online.hadithpull.app.ui.share.ShareRoute
import online.hadithpull.app.domain.text.bookmarkExcerpt
import online.hadithpull.app.domain.text.plainText
import online.hadithpull.app.ui.components.ActionTone
import online.hadithpull.app.ui.components.HadithBackTopBar
import online.hadithpull.app.ui.components.HadithCard
import online.hadithpull.app.ui.components.HadithIcons
import online.hadithpull.app.ui.components.LocalToastState
import online.hadithpull.app.ui.components.ReferenceSummary
import online.hadithpull.app.ui.components.SecondaryAction
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
    var menuOpen by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        HadithBackTopBar(title = "", onBack = onBack) {
            IconButton(onClick = {
                scope.launch {
                    val html = LibraryExport.buildDocument(container.bookmarkRepository, folderId)
                    val fileName = "hadith-pull-library-${currentFolder.name.take(40)}.html"
                    val uri = writeLibraryToCache(context, html, fileName)
                    val send = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/html"
                        putExtra(android.content.Intent.EXTRA_STREAM, uri)
                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(android.content.Intent.createChooser(send, "Share folder"))
                }
            }) {
                Icon(painterResource(HadithIcons.upload), contentDescription = "Share folder", tint = LocalHadithColors.current.textSoft)
            }
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Folder options", tint = LocalHadithColors.current.textSoft)
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(text = { Text("Rename") }, onClick = { menuOpen = false; renaming = true })
                    DropdownMenuItem(text = { Text("Delete", color = LocalHadithColors.current.error) }, onClick = { menuOpen = false; deleting = true })
                }
            }
        }
        FolderDetailBody(
            folderName = currentFolder.name,
            darkTheme = darkTheme,
            countLabel = if (items.size == 1) "1 Hadith" else "${items.size} Hadiths",
            items = items,
            otherFolders = allFolders.filter { it.id != folderId },
            expandedIds = expandedIds,
            onToggleExpanded = { id ->
                expandedIds = if (id in expandedIds) expandedIds - id else expandedIds + id
            },
            onCopy = { item -> copyToClipboard(context, item) },
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
    folderName: String,
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
    val typography = LocalHadithTypography.current
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Spacer(Modifier.height(8.dp))
            Text(text = "FOLDER", style = typography.sectionLabel, color = colors.accentInk)
            Spacer(Modifier.height(4.dp))
            Text(text = folderName, style = typography.pageTitle, color = colors.text, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Text(text = countLabel, color = colors.muted, fontSize = 14.sp)
            Spacer(Modifier.height(12.dp))
        }
        if (items.isEmpty()) {
            item {
                HadithCard(contentPadding = PaddingValues(24.dp)) {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Nothing saved here yet.", style = typography.panelTitle, color = colors.text, fontSize = 20.sp)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Tap Save under any Hadith to add it here.",
                            color = colors.muted,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        } else {
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
        }
        item { Spacer(Modifier.height(32.dp)) }
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
    val hadith = remember(item.hadithJson) { item.toHadith() }
    val short = remember(hadith.english) { bookmarkExcerpt(hadith.english, 320) }
    val canExpand = short != hadith.english || hadith.arabic.isNotEmpty()
    var overflowOpen by remember { mutableStateOf(false) }
    var moveMenuOpen by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) {
        if (copied) {
            delay(2000)
            copied = false
        }
    }
    val expandRotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f, animationSpec = tween(350), label = "expandChevron")

    HadithCard {
        ReferenceSummary(
            hadith = hadith,
            loading = false,
            darkTheme = darkTheme,
            onOpenSunnah = { url -> context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))) },
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (expanded) hadith.english else short,
            style = typography.english,
            color = colors.text,
            fontSize = 18.4.sp,
            lineHeight = (18.4 * 1.65).sp,
        )
        if (expanded && hadith.arabic.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = hadith.arabic,
                style = typography.arabic.copy(textAlign = TextAlign.Right, textDirection = TextDirection.Rtl),
                fontSize = 22.4.sp,
                color = colors.text,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (hadith.narrator.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Text(text = hadith.narrator, color = colors.muted, fontStyle = FontStyle.Italic, fontSize = 14.sp)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Saved ${dateFormatter.format(Instant.ofEpochMilli(item.savedAt).atZone(ZoneId.systemDefault()).toLocalDate())}",
            color = colors.muted,
            fontSize = 12.5.sp,
        )
        Spacer(Modifier.height(12.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
        Spacer(Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (canExpand) {
                SecondaryAction(
                    label = if (expanded) "Show less" else "Show full",
                    icon = rememberVectorPainter(Icons.Filled.KeyboardArrowDown),
                    onClick = onToggleExpanded,
                    iconRotation = expandRotation,
                )
            }
            Spacer(Modifier.weight(1f))
            SecondaryAction(
                label = if (copied) "Copied" else "Copy",
                icon = painterResource(if (copied) HadithIcons.check else HadithIcons.copy),
                onClick = { onCopy(); copied = true },
                tone = if (copied) ActionTone.Accent else ActionTone.Neutral,
                liveLabel = true,
            )
            SecondaryAction(label = "Share", icon = painterResource(HadithIcons.upload), onClick = onShare)
            Box {
                IconButton(onClick = { overflowOpen = true }, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "More actions", tint = colors.textSoft, modifier = Modifier.size(20.dp))
                }
                DropdownMenu(expanded = overflowOpen, onDismissRequest = { overflowOpen = false }) {
                    if (otherFolders.isNotEmpty()) {
                        DropdownMenuItem(text = { Text("Move to…") }, onClick = { overflowOpen = false; moveMenuOpen = true })
                    }
                    DropdownMenuItem(
                        text = { Text("Remove from folder", color = colors.error) },
                        onClick = { overflowOpen = false; onRemove() },
                    )
                }
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
    }
}

// R3-Q3: Copy confirms inline ("Copied", above) on every API level, so no toast or Build check.
private fun copyToClipboard(context: android.content.Context, item: BookmarkEntity) {
    val clipboard = context.getSystemService(ClipboardManager::class.java)
    clipboard.setPrimaryClip(ClipData.newPlainText("Hadith", plainText(item.toHadith())))
}
