package online.hadithpull.app.ui.reader

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Build
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
import online.hadithpull.app.data.prefs.TextSize
import online.hadithpull.app.di.AppContainer
import online.hadithpull.app.domain.Hadith
import online.hadithpull.app.domain.text.plainText
import online.hadithpull.app.ui.components.LocalToastState

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

    ReaderScreen(
        uiState = uiState,
        darkTheme = darkTheme,
        arabicScript = settings.arabicScript,
        textSize = settings.textSize,
        isSaved = isSaved,
        onDraw = viewModel::draw,
        onToggleExpand = viewModel::toggleExpand,
        onSetArabicScript = { script ->
            scope.launch { container.settingsRepository.setArabicScript(script) }
        },
        onCycleTextSize = {
            val next = cycleTextSize(settings.textSize)
            scope.launch { container.settingsRepository.setTextSize(next) }
            toastState.show("Text size: ${next.label}")
        },
        onCopy = { hadith -> copyToClipboard(context, hadith, toastState) },
        onOpenSave = { hadith -> saveSheetHadith = hadith },
        onOpenShare = { /* Share sheet: Step 12 */ },
        onOpenAttribution = {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://hadithapi.com")))
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
}

private val TextSize.label: String
    get() = name.lowercase().replaceFirstChar { it.uppercase() }

private fun cycleTextSize(current: TextSize): TextSize {
    val values = TextSize.entries
    return values[(values.indexOf(current) + 1) % values.size]
}

private fun copyToClipboard(context: android.content.Context, hadith: Hadith, toastState: online.hadithpull.app.ui.components.ToastState) {
    val clipboard = context.getSystemService(ClipboardManager::class.java)
    clipboard.setPrimaryClip(ClipData.newPlainText("Hadith", plainText(hadith)))
    // §2.2: Android 13+ shows its own clipboard confirmation; a second toast would duplicate it.
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
        toastState.show("Hadith copied to clipboard")
    }
}
