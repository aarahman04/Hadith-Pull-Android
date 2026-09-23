package online.hadithpull.app.ui.bookmarks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.launch
import online.hadithpull.app.data.CreateFolderResult
import online.hadithpull.app.data.FolderSummary
import online.hadithpull.app.data.RenameFolderResult
import online.hadithpull.app.di.AppContainer
import online.hadithpull.app.ui.components.HadithIcons
import online.hadithpull.app.ui.components.LocalToastState
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
    val viewModel: BookmarksViewModel = viewModel(
        factory = viewModelFactory { initializer { BookmarksViewModel(container.bookmarkRepository) } },
    )
    val folders by viewModel.folders.collectAsState()
    val toastState = LocalToastState.current
    val scope = rememberCoroutineScope()

    var newFolderName by remember { mutableStateOf("") }
    var renameTarget by remember { mutableStateOf<FolderSummary?>(null) }
    var deleteTarget by remember { mutableStateOf<FolderSummary?>(null) }

    fun submitCreate() {
        val name = newFolderName
        scope.launch {
            when (val result = viewModel.createFolder(name)) {
                is CreateFolderResult.Created -> {
                    toastState.show("Folder \"${result.folder.name}\" created")
                    newFolderName = ""
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
        onNewFolderNameChange = { if (it.length <= MAX_FOLDER_NAME_LENGTH) newFolderName = it },
        onCreateFolder = ::submitCreate,
        onOpenFolder = onOpenFolder,
        onGoToReader = onGoToReader,
        onRename = { renameTarget = it },
        onDelete = { deleteTarget = it },
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

@Composable
private fun FoldersScreen(
    folders: List<FolderSummary>,
    newFolderName: String,
    onNewFolderNameChange: (String) -> Unit,
    onCreateFolder: () -> Unit,
    onOpenFolder: (Long) -> Unit,
    onGoToReader: () -> Unit,
    onRename: (FolderSummary) -> Unit,
    onDelete: (FolderSummary) -> Unit,
) {
    val colors = LocalHadithColors.current
    val typography = LocalHadithTypography.current
    val windowWidthDp = LocalConfiguration.current.screenWidthDp
    val sidePadding = if (windowWidthDp < 720) 16.dp else 20.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = sidePadding),
    ) {
        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(28.dp).height(1.dp).background(colors.borderStrong))
            Text(
                text = "YOUR LIBRARY",
                style = typography.eyebrow,
                color = colors.muted,
                modifier = Modifier.padding(horizontal = 10.dp),
            )
            Box(Modifier.width(28.dp).height(1.dp).background(colors.borderStrong))
        }
        Spacer(Modifier.height(12.dp))
        Text(text = "Bookmarks", style = typography.pageTitle, color = colors.text)
        if (windowWidthDp >= 720) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Hadiths you've saved, organized into folders you name — kept on this device only.",
                style = typography.body,
                color = colors.textSoft,
            )
        }
        Spacer(Modifier.height(20.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth(),
        ) {
            OutlinedTextField(
                value = newFolderName,
                onValueChange = onNewFolderNameChange,
                placeholder = { Text("New folder name…") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(10.dp))
            TextButton(onClick = onCreateFolder) {
                Text("Create folder", color = colors.accent, fontWeight = FontWeight.Medium)
            }
        }
        Spacer(Modifier.height(24.dp))

        if (folders.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface, HadithShapes.lg)
                    .border(1.dp, colors.border, HadithShapes.lg)
                    .padding(24.dp),
            ) {
                Text(
                    text = "No folders yet. Create one above, or open a Hadith and tap Save to start your first one.",
                    color = colors.textSoft,
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = onGoToReader, shape = HadithShapes.pill) {
                    Text("Read a Hadith")
                }
            }
        } else {
            LazyVerticalGrid(
                columns = if (windowWidthDp < 720) GridCells.Fixed(1) else GridCells.Adaptive(220.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(folders, key = { it.id }) { folder ->
                    FolderCard(folder = folder, onOpen = { onOpenFolder(folder.id) }, onRename = { onRename(folder) }, onDelete = { onDelete(folder) })
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun FolderCard(folder: FolderSummary, onOpen: () -> Unit, onRename: () -> Unit, onDelete: () -> Unit) {
    val colors = LocalHadithColors.current
    val typography = LocalHadithTypography.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface, RoundedCornerShape(26.dp))
            .border(1.dp, colors.border, RoundedCornerShape(26.dp))
            .clickable(onClick = onOpen)
            .padding(vertical = 22.dp, horizontal = 21.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            IconButton(onClick = onRename, modifier = Modifier.size(48.dp)) {
                Icon(painter = painterResource(HadithIcons.pencil), contentDescription = "Rename", tint = colors.textSoft, modifier = Modifier.size(40.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(48.dp)) {
                Icon(painter = painterResource(HadithIcons.bin), contentDescription = "Delete", tint = colors.textSoft, modifier = Modifier.size(40.dp))
            }
        }
        Text(text = folder.name, style = typography.panelTitle, color = colors.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (folder.count == 1) "1 Hadith" else "${folder.count} Hadiths",
            color = colors.muted,
            fontSize = 13.6.sp,
        )
    }
}
