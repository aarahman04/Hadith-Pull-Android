package online.hadithpull.app.ui.bookmarks

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import java.time.LocalDate
import kotlinx.coroutines.launch
import online.hadithpull.app.data.CreateFolderResult
import online.hadithpull.app.data.FolderSummary
import online.hadithpull.app.data.RenameFolderResult
import online.hadithpull.app.data.library.LibraryExport
import online.hadithpull.app.data.library.LibraryExportDocument
import online.hadithpull.app.data.library.LibraryImport
import online.hadithpull.app.data.library.ParseResult
import online.hadithpull.app.data.library.applyImport
import online.hadithpull.app.data.library.previewImport
import online.hadithpull.app.di.AppContainer
import online.hadithpull.app.share.writeLibraryToCache
import online.hadithpull.app.ui.components.ChevronTrailing
import online.hadithpull.app.ui.components.HadithIcons
import online.hadithpull.app.ui.components.ListRow
import online.hadithpull.app.ui.components.LocalToastState
import online.hadithpull.app.ui.components.NewFolderField
import online.hadithpull.app.ui.components.RowDivider
import online.hadithpull.app.ui.components.ScreenHero
import online.hadithpull.app.ui.theme.HadithSpacing
import online.hadithpull.app.ui.theme.HadithShapes
import online.hadithpull.app.ui.theme.LocalHadithColors
import online.hadithpull.app.ui.theme.LocalHadithTypography

private const val MAX_FOLDER_NAME_LENGTH = 40

/** §2.4 Folder list (Bookmarks tab root). */
@Composable
fun FoldersRoute(
    container: AppContainer,
    onOpenFolder: (Long) -> Unit,
    onGoToReader: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel: BookmarksViewModel = viewModel(
        factory = viewModelFactory { initializer { BookmarksViewModel(container.bookmarkRepository) } },
    )
    val folders by viewModel.folders.collectAsState()
    val toastState = LocalToastState.current
    val scope = rememberCoroutineScope()

    var newFolderName by remember { mutableStateOf("") }
    var creatingFolder by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<FolderSummary?>(null) }
    var deleteTarget by remember { mutableStateOf<FolderSummary?>(null) }

    fun shareFolder(folder: FolderSummary) {
        scope.launch {
            shareLibrary(context, container, setOf(folder.id), "Share ${folder.name}")
        }
    }

    fun submitCreate() {
        val name = newFolderName
        scope.launch {
            when (val result = viewModel.createFolder(name)) {
                is CreateFolderResult.Created -> {
                    toastState.show("Folder \"${result.folder.name}\" created")
                    newFolderName = ""
                    creatingFolder = false
                }
                is CreateFolderResult.Existing -> {
                    toastState.show("A folder with that name already exists")
                }
                CreateFolderResult.Invalid -> Unit
            }
        }
    }

    FoldersScreen(
        folders = folders,
        newFolderName = newFolderName,
        creatingFolder = creatingFolder,
        onCreatingFolderChange = { creatingFolder = it },
        onNewFolderNameChange = { if (it.length <= MAX_FOLDER_NAME_LENGTH) newFolderName = it },
        onCreateFolder = ::submitCreate,
        onOpenFolder = onOpenFolder,
        onGoToReader = onGoToReader,
        onRename = { renameTarget = it },
        onDelete = { deleteTarget = it },
        onShareFolder = ::shareFolder,
        libraryContent = { LibrarySection(container, folders) },
    )

    renameTarget?.let { target ->
        RenameFolderDialog(
            currentName = target.name,
            onDismiss = { renameTarget = null },
            onConfirm = { name ->
                scope.launch {
                    when (viewModel.renameFolder(target.id, name)) {
                        is RenameFolderResult.Duplicate -> toastState.show("A folder with that name already exists")
                        is RenameFolderResult.Renamed -> renameTarget = null
                        is RenameFolderResult.Invalid -> Unit
                    }
                }
            },
        )
    }

    deleteTarget?.let { target ->
        DeleteFolderDialog(
            folderName = target.name,
            onDismiss = { deleteTarget = null },
            onConfirm = {
                scope.launch {
                    viewModel.deleteFolder(target.id)
                    toastState.show("Folder deleted")
                    deleteTarget = null
                }
            },
        )
    }
}

/** Round 3 §Step 31: the root is a LazyColumn (fixing a real bug -- this screen had no scroll at
 * all, which is the likely root cause of the "export/import renders poorly" complaint once a
 * folder list grew past a few entries), capped at 720dp wide. */
@Composable
private fun FoldersScreen(
    folders: List<FolderSummary>,
    newFolderName: String,
    creatingFolder: Boolean,
    onCreatingFolderChange: (Boolean) -> Unit,
    onNewFolderNameChange: (String) -> Unit,
    onCreateFolder: () -> Unit,
    onOpenFolder: (Long) -> Unit,
    onGoToReader: () -> Unit,
    onRename: (FolderSummary) -> Unit,
    onDelete: (FolderSummary) -> Unit,
    onShareFolder: (FolderSummary) -> Unit,
    libraryContent: @Composable () -> Unit,
) {
    val typography = LocalHadithTypography.current
    val windowWidthDp = LocalConfiguration.current.screenWidthDp
    val sidePadding = if (windowWidthDp < 720) 16.dp else 20.dp

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = sidePadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            Spacer(Modifier.height(HadithSpacing.xl))
            Box(Modifier.widthIn(max = 720.dp)) {
                ScreenHero(
                    eyebrow = "YOUR LIBRARY",
                    title = "Bookmarks",
                    titleStyle = typography.pageTitle,
                    subline = if (windowWidthDp >= 720) {
                        "Hadiths you've saved, organized into folders you name. Kept on this device only."
                    } else {
                        null
                    },
                )
            }
            Spacer(Modifier.height(HadithSpacing.xxl))
        }
        item {
            Box(Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
                EditorialSectionHeader(
                    title = "Folders",
                    action = if (!creatingFolder) {
                        { NewFolderField(expanded = false, onExpandedChange = onCreatingFolderChange, value = newFolderName, onValueChange = onNewFolderNameChange, onSubmit = onCreateFolder) }
                    } else {
                        null
                    },
                )
            }
            if (creatingFolder) {
                Spacer(Modifier.height(HadithSpacing.md))
                Box(Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
                    NewFolderField(
                        expanded = true,
                        onExpandedChange = onCreatingFolderChange,
                        value = newFolderName,
                        onValueChange = onNewFolderNameChange,
                        onSubmit = onCreateFolder,
                    )
                }
            }
            Spacer(Modifier.height(HadithSpacing.md))
        }
        item {
            Box(Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
                if (folders.isEmpty()) {
                    EmptyFoldersState()
                } else {
                    Column {
                        folders.forEachIndexed { index, folder ->
                            FolderRow(
                                folder = folder,
                                onOpen = { onOpenFolder(folder.id) },
                                onRename = { onRename(folder) },
                                onDelete = { onDelete(folder) },
                                onShare = { onShareFolder(folder) },
                            )
                            if (index < folders.lastIndex) RowDivider()
                        }
                    }
                }
            }
        }
        item {
            Spacer(Modifier.height(HadithSpacing.section))
            Box(Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
                EditorialSectionHeader(
                    title = "Backup",
                    lede = "Keep a copy of your bookmarks, or move them to another device.",
                )
            }
            Spacer(Modifier.height(HadithSpacing.md))
            Box(Modifier.widthIn(max = 720.dp).fillMaxWidth()) { libraryContent() }
            Spacer(Modifier.height(HadithSpacing.xxl))
        }
    }
}

@Composable
private fun EditorialSectionHeader(
    title: String,
    lede: String? = null,
    action: (@Composable () -> Unit)? = null,
) {
    val colors = LocalHadithColors.current
    val typography = LocalHadithTypography.current
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = typography.sectionTitle,
                color = colors.text,
                modifier = Modifier.weight(1f),
            )
            if (action != null) action()
        }
        if (lede != null) {
            Spacer(Modifier.height(6.dp))
            Text(text = lede, style = typography.helper, color = colors.muted)
        }
    }
}

@Composable
private fun EmptyFoldersState() {
    val colors = LocalHadithColors.current
    val typography = LocalHadithTypography.current
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            text = "No folders yet",
            style = typography.helper.copy(fontWeight = FontWeight.Medium),
            color = colors.textSoft,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Folders are created when you save a Hadith or tap New Folder.",
            style = typography.helper,
            color = colors.muted,
        )
    }
}

/** §R1.5: replaces the old folder-card grid with one grouped-list row. */
@Composable
private fun FolderRow(
    folder: FolderSummary,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
) {
    val colors = LocalHadithColors.current
    val typography = LocalHadithTypography.current
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .clickable(onClick = onOpen)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(colors.accentSoft, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(HadithIcons.bookmarkFilled),
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = folder.name,
                style = typography.optionLabel.copy(fontWeight = FontWeight.Medium),
                color = colors.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = if (folder.count == 1) "1 Hadith" else "${folder.count} Hadiths",
                style = typography.helper,
                color = colors.muted,
            )
        }
        Box {
            IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Folder options",
                    tint = colors.textSoft,
                    modifier = Modifier.size(20.dp),
                )
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(text = { Text("Share") }, onClick = { menuOpen = false; onShare() })
                DropdownMenuItem(text = { Text("Rename") }, onClick = { menuOpen = false; onRename() })
                DropdownMenuItem(text = { Text("Delete", color = colors.error) }, onClick = { menuOpen = false; onDelete() })
            }
        }
        ChevronTrailing()
    }
}

private fun libraryFileName(): String = "hadith-pull-library-${LocalDate.now()}.html"

/** R1.8/R0.6/Round 3 §Step 31: the "Library" entry point -- Export/Share/Import as three rows in
 * a grouped list, moved out of primary visual weight so it doesn't compete with folder browsing
 * (the "shown poorly" bug). */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun LibrarySection(container: AppContainer, folders: List<FolderSummary>) {
    val context = LocalContext.current
    val toastState = LocalToastState.current
    val scope = rememberCoroutineScope()

    var pendingImport by remember { mutableStateOf<LibraryExportDocument?>(null) }
    var emptyImport by remember { mutableStateOf(false) }
    var importSummary by remember { mutableStateOf<String?>(null) }
    var shareChoiceOpen by remember { mutableStateOf(false) }
    var folderPickerOpen by remember { mutableStateOf(false) }
    var selectedFolderIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    val typography = LocalHadithTypography.current
    val colors = LocalHadithColors.current
    val pickerState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/html")) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val html = LibraryExport.buildDocument(container.bookmarkRepository)
            context.contentResolver.openOutputStream(uri)?.use { it.write(html.toByteArray()) }
            toastState.show("Library exported")
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            when (val result = bytes?.let { LibraryImport.parse(it) }) {
                is ParseResult.Ok -> {
                    if (previewImport(result.document).itemCount == 0) {
                        emptyImport = true
                    } else {
                        pendingImport = result.document
                    }
                }
                ParseResult.NotALibraryFile, null -> toastState.show("This isn't a Hadith Pull library file.")
                ParseResult.TooLarge -> toastState.show("This file is too large to import.")
            }
        }
    }

    fun requireFolders(action: () -> Unit) {
        if (folders.isNotEmpty()) action() else toastState.show("No bookmarks to export yet.")
    }

    Column {
        ListRow(
            title = "Export Library",
            subtitle = "Save a readable copy you can import later",
            onClick = { requireFolders { exportLauncher.launch(libraryFileName()) } },
            trailing = { ChevronTrailing() },
            compact = true,
        )
        RowDivider()
        ListRow(
            title = "Share Library",
            subtitle = "Share your full library or selected folders",
            onClick = { requireFolders { shareChoiceOpen = true } },
            trailing = { ChevronTrailing() },
            compact = true,
        )
        RowDivider()
        ListRow(
            title = "Import Library",
            subtitle = "Add folders from a Hadith Pull file",
            onClick = { importLauncher.launch(arrayOf("text/html")) },
            trailing = { ChevronTrailing() },
            compact = true,
        )
    }

    if (shareChoiceOpen) {
        AlertDialog(
            onDismissRequest = { shareChoiceOpen = false },
            title = { Text("Share Library", style = typography.sectionTitle.copy(fontSize = 20.sp)) },
            text = { Text("Share your entire library, or choose specific folders.", style = typography.helper) },
            confirmButton = {
                TextButton(onClick = {
                    shareChoiceOpen = false
                    scope.launch { shareLibrary(context, container, null, "Share your full library") }
                }) { Text("Share full library", style = typography.secondaryAction) }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { shareChoiceOpen = false }) { Text("Cancel", style = typography.secondaryAction) }
                    TextButton(onClick = {
                        selectedFolderIds = emptySet()
                        shareChoiceOpen = false
                        folderPickerOpen = true
                    }) { Text("Choose folders", style = typography.secondaryAction, color = colors.accent) }
                }
            },
        )
    }

    if (folderPickerOpen) {
        ModalBottomSheet(
            onDismissRequest = { folderPickerOpen = false },
            sheetState = pickerState,
            containerColor = colors.bg,
            dragHandle = { BottomSheetDefaults.DragHandle(width = 36.dp, height = 4.dp, color = colors.borderStrong) },
        ) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 20.dp)) {
                Text("Choose folders", style = typography.sectionTitle.copy(fontSize = 20.sp), color = colors.text)
                Spacer(Modifier.height(4.dp))
                Text("Only selected folders will be included.", style = typography.helper, color = colors.muted)
                Spacer(Modifier.height(12.dp))
                Column(Modifier.heightIn(max = 340.dp).verticalScroll(rememberScrollState())) {
                    folders.forEach { folder ->
                        Row(
                            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).clickable {
                                selectedFolderIds = if (folder.id in selectedFolderIds) {
                                    selectedFolderIds - folder.id
                                } else {
                                    selectedFolderIds + folder.id
                                }
                            }.padding(horizontal = 4.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(folder.name, style = typography.optionLabel, color = colors.text)
                                Text(
                                    if (folder.count == 1) "1 Hadith" else "${folder.count} Hadiths",
                                    style = typography.helper.copy(fontSize = 12.5.sp),
                                    color = colors.muted,
                                )
                            }
                            Checkbox(
                                checked = folder.id in selectedFolderIds,
                                onCheckedChange = { checked ->
                                    selectedFolderIds = if (checked) selectedFolderIds + folder.id else selectedFolderIds - folder.id
                                },
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                androidx.compose.material3.Button(
                    onClick = {
                        val chosen = selectedFolderIds
                        folderPickerOpen = false
                        scope.launch { shareLibrary(context, container, chosen, "Share selected folders") }
                    },
                    enabled = selectedFolderIds.isNotEmpty(),
                    shape = HadithShapes.pill,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                ) {
                    Text("Share ${selectedFolderIds.size} folder${if (selectedFolderIds.size == 1) "" else "s"}", style = typography.secondaryAction)
                }
            }
        }
    }

    pendingImport?.let { doc ->
        val preview = previewImport(doc)
        AlertDialog(
            onDismissRequest = { pendingImport = null },
            title = { Text("Import this library?") },
            text = { Text("${preview.folderCount} folders, ${preview.itemCount} Hadiths.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        val summary = container.bookmarkRepository.applyImport(doc, container.hadithStore)
                        pendingImport = null
                        importSummary = buildString {
                            append("Added ${summary.added} Hadith${if (summary.added == 1) "" else "s"} ")
                            append("to ${summary.foldersTouched} folder${if (summary.foldersTouched == 1) "" else "s"}.")
                            if (summary.alreadySaved > 0) {
                                append(" ${summary.alreadySaved} ${if (summary.alreadySaved == 1) "was" else "were"} already saved.")
                            }
                            if (summary.notFound > 0) {
                                append(" ${summary.notFound} couldn't be found in this version of the app.")
                            }
                        }
                    }
                }) { Text("Import") }
            },
            dismissButton = { TextButton(onClick = { pendingImport = null }) { Text("Cancel") } },
        )
    }

    if (emptyImport) {
        AlertDialog(
            onDismissRequest = { emptyImport = false },
            title = { Text("Nothing to import") },
            text = { Text("This file doesn't contain any saved Hadiths.") },
            confirmButton = { TextButton(onClick = { emptyImport = false }) { Text("OK") } },
        )
    }

    importSummary?.let { message ->
        AlertDialog(
            onDismissRequest = { importSummary = null },
            title = { Text("Import complete") },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = { importSummary = null }) { Text("OK") } },
        )
    }
}

private suspend fun shareLibrary(
    context: android.content.Context,
    container: AppContainer,
    folderIds: Set<Long>?,
    chooserTitle: String,
) {
    val html = if (folderIds == null) {
        LibraryExport.buildDocument(container.bookmarkRepository)
    } else {
        LibraryExport.buildDocument(container.bookmarkRepository, folderIds)
    }
    val uri = writeLibraryToCache(context, html, libraryFileName())
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/html"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(send, chooserTitle))
}
