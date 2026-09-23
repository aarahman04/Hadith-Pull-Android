package online.hadithpull.app.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * A DataStore<Preferences> backed by memory instead of a file. File-based DataStore's
 * tmp-file-then-rename write path fails on Windows once a second write targets the same
 * already-existing file (a known cross-platform DataStore unit-test limitation), so JVM
 * tests use this instead.
 */
private class InMemoryPreferencesDataStore(initial: Preferences = emptyPreferences()) : DataStore<Preferences> {
    private val state = MutableStateFlow(initial)
    override val data: Flow<Preferences> = state

    override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences {
        val updated = transform(state.value)
        state.value = updated
        return updated
    }
}

class SettingsRepositoryTest {
    private fun repository(): SettingsRepository = SettingsRepository(InMemoryPreferencesDataStore())

    @Test
    fun `defaults are SYSTEM, NASKH, COMFORTABLE and cardArabic true`() = runBlocking {
        val settings = repository().settings.first()
        assertEquals(Settings(), settings)
    }

    @Test
    fun `each setter round-trips through the flow`() = runBlocking {
        val repo = repository()
        repo.setTheme(Theme.DARK)
        repo.setArabicScript(ArabicScript.BOLD)
        repo.setTextSize(TextSize.LARGEST)
        repo.setCardArabic(false)

        val settings = repo.settings.first()
        assertEquals(Theme.DARK, settings.theme)
        assertEquals(ArabicScript.BOLD, settings.arabicScript)
        assertEquals(TextSize.LARGEST, settings.textSize)
        assertEquals(false, settings.cardArabic)
    }

    @Test
    fun `an unknown or legacy stored value falls back to the default`() = runBlocking {
        val dataStore = InMemoryPreferencesDataStore()
        dataStore.edit { prefs ->
            prefs[stringPreferencesKey("theme")] = "SYSTEM_DEFAULT" // legacy/unknown value
            prefs[stringPreferencesKey("arabicScript")] = "TRADITIONAL" // never a valid enum value
        }
        val settings = settingsFrom(dataStore.data.first())
        assertEquals(Theme.SYSTEM, settings.theme)
        assertEquals(ArabicScript.NASKH, settings.arabicScript)
    }
}
