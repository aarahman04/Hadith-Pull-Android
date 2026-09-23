package online.hadithpull.app.ui.share

import android.content.res.AssetManager
import android.graphics.Bitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import online.hadithpull.app.card.CardInput
import online.hadithpull.app.card.CardRenderer
import online.hadithpull.app.card.CardTheme
import online.hadithpull.app.data.prefs.ArabicScript
import online.hadithpull.app.data.prefs.SettingsRepository
import online.hadithpull.app.domain.Hadith

/**
 * §3.3: renders run via collectLatest on (theme, includeArabic), so a new option cancels the
 * stale render. The script is captured once at sheet-open (not one of the sheet's own options).
 *
 * Deliberately not an androidx.lifecycle.ViewModel: those scope to the ambient
 * ViewModelStoreOwner (the enclosing screen's NavBackStackEntry), which outlives this sheet being
 * dismissed and reopened -- "starts on Light on every open" would break the second time the
 * sheet opens for the same screen. `scope` is the caller's `rememberCoroutineScope()`, tied to
 * ShareRoute's own composition, so a fresh instance (and a fresh Light default) is guaranteed by
 * constructing this with `remember(hadith)` at the call site.
 */
class ShareViewModel(
    private val assets: AssetManager,
    private val hadith: Hadith,
    private val script: ArabicScript,
    private val settingsRepository: SettingsRepository,
    private val scope: CoroutineScope,
) {
    private val _theme = MutableStateFlow(CardTheme.LIGHT) // starts on Light every open (not persisted, parity)
    val theme: StateFlow<CardTheme> = _theme.asStateFlow()

    val includeArabic: StateFlow<Boolean> = settingsRepository.settings
        .map { it.cardArabic }
        .stateIn(scope, SharingStarted.WhileSubscribed(5_000), true)

    private val _bitmap = MutableStateFlow<Bitmap?>(null)
    val bitmap: StateFlow<Bitmap?> = _bitmap.asStateFlow()

    private val _rendering = MutableStateFlow(true)
    val rendering: StateFlow<Boolean> = _rendering.asStateFlow()

    private val _renderFailed = MutableSharedFlow<Unit>()
    val renderFailed: SharedFlow<Unit> = _renderFailed.asSharedFlow()

    init {
        scope.launch {
            theme.combine(includeArabic) { t, includeAr -> t to includeAr }
                .collectLatest { (currentTheme, includeAr) ->
                    _rendering.value = true
                    try {
                        val input = CardInput.from(hadith, includeAr, script)
                        val rendered = withContext(Dispatchers.Default) { CardRenderer.render(assets, input, currentTheme) }
                        _bitmap.value = rendered
                    } catch (e: Exception) {
                        _renderFailed.emit(Unit)
                    } finally {
                        _rendering.value = false
                    }
                }
        }
    }

    fun setTheme(newTheme: CardTheme) {
        _theme.value = newTheme
    }

    fun setIncludeArabic(value: Boolean) {
        scope.launch { settingsRepository.setCardArabic(value) }
    }
}
