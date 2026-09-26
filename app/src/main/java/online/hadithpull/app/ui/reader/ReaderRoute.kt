package online.hadithpull.app.ui.reader

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import online.hadithpull.app.data.prefs.Settings
import online.hadithpull.app.di.AppContainer
import online.hadithpull.app.domain.Hadith
import online.hadithpull.app.domain.text.plainText
import online.hadithpull.app.ui.components.LocalToastState
import online.hadithpull.app.ui.share.ShareRoute

/** Wires ReaderViewModel + AppContainer's repositories into ReaderScreen. */
@Composable
fun ReaderRoute(container: AppContainer, darkTheme: Boolean, settings: Settings, onNavigateToBookmarks: () -> Unit) {
    val viewModel: ReaderViewModel = viewModel(
        factory = viewModelFactory {
            initializer { ReaderViewModel(container.hadithRepository, createSavedStateHandle()) }
        },
    )
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.start(null) }

    val scope = rememberCoroutineScope()
    val toastState = LocalToastState.current
    val context = LocalContext.current

    val currentHadith = (uiState as? ReaderUiState.Loaded)?.hadith
    val isSaved by remember(currentHadith?.key) {
        currentHadith?.let { container.bookmarkRepository.isSaved(it.key) } ?: flowOf(false)
    }.collectAsState(initial = false)

    var saveSheetHadith by remember { mutableStateOf<Hadith?>(null) }
    var shareSheetHadith by remember { mutableStateOf<Hadith?>(null) }

    ReaderScreen(
        uiState = uiState,
        darkTheme = darkTheme,
        arabicScript = settings.arabicScript,
        isSaved = isSaved,
        onDraw = viewModel::draw,
        onToggleExpand = viewModel::toggleExpand,
        onSetArabicScript = { script ->
            scope.launch { container.settingsRepository.setArabicScript(script) }
        },
        onCopy = { hadith -> copyToClipboard(context, hadith) },
        onOpenSave = { hadith -> saveSheetHadith = hadith },
        onOpenShare = { hadith -> shareSheetHadith = hadith },
        onOpenSunnah = { url ->
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        },
    )

    saveSheetHadith?.let { hadith ->
        SaveSheet(
            hadith = hadith,
            bookmarkRepository = container.bookmarkRepository,
            toastState = toastState,
            onDismiss = { saveSheetHadith = null },
            onManageBookmarks = onNavigateToBookmarks,
        )
    }

    shareSheetHadith?.let { hadith ->
        ShareRoute(container = container, hadith = hadith, onDismiss = { shareSheetHadith = null })
    }
}

// R3-Q3: the Copy action confirms inline (label -> "Copied") on every API level, so no toast or
// API-level branch is needed here any more.
private fun copyToClipboard(context: android.content.Context, hadith: Hadith) {
    val clipboard = context.getSystemService(ClipboardManager::class.java)
    clipboard.setPrimaryClip(ClipData.newPlainText("Hadith", plainText(hadith)))
}
