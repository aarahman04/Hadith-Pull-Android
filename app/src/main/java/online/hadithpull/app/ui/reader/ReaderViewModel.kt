package online.hadithpull.app.ui.reader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import online.hadithpull.app.data.HadithRepository
import online.hadithpull.app.domain.DrawResult
import online.hadithpull.app.domain.Hadith

sealed interface ReaderUiState {
    data object Loading : ReaderUiState
    data class Loaded(val hadith: Hadith, val expanded: Boolean) : ReaderUiState
    data object Failure : ReaderUiState
}

private const val KEY_HADITH_JSON = "hadith_json"
private const val KEY_EXPANDED = "expanded"

/** §4.1 seam: a single entry point, start(initial). null means draw; v1 always passes null. */
class ReaderViewModel(
    private val hadithRepository: HadithRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ReaderUiState>(ReaderUiState.Loading)
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    private var started = false

    fun start(initial: Hadith?) {
        if (started) return
        started = true

        val restoredHadith = initial ?: savedStateHandle.get<String>(KEY_HADITH_JSON)?.let {
            Json.decodeFromString<Hadith>(it)
        }
        if (restoredHadith != null) {
            val restoredExpanded = savedStateHandle.get<Boolean>(KEY_EXPANDED) ?: false
            _uiState.value = ReaderUiState.Loaded(restoredHadith, restoredExpanded)
        } else {
            draw()
        }
    }

    fun draw() {
        val currentKey = (_uiState.value as? ReaderUiState.Loaded)?.hadith?.key
        _uiState.value = ReaderUiState.Loading
        viewModelScope.launch {
            when (val result = hadithRepository.draw(currentKey)) {
                is DrawResult.Success -> setLoaded(result.hadith, expanded = false)
                is DrawResult.Failure -> _uiState.value = ReaderUiState.Failure
            }
        }
    }

    fun toggleExpand() {
        val state = _uiState.value
        if (state is ReaderUiState.Loaded) {
            setLoaded(state.hadith, !state.expanded)
        }
    }

    private fun setLoaded(hadith: Hadith, expanded: Boolean) {
        _uiState.value = ReaderUiState.Loaded(hadith, expanded)
        savedStateHandle[KEY_HADITH_JSON] = Json.encodeToString(hadith)
        savedStateHandle[KEY_EXPANDED] = expanded
    }
}
