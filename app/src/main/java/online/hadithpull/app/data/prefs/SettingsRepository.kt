package online.hadithpull.app.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private object Keys {
    val THEME = stringPreferencesKey("theme")
    val ARABIC_SCRIPT = stringPreferencesKey("arabicScript")
    val TEXT_SIZE = stringPreferencesKey("textSize")
    val CARD_ARABIC = booleanPreferencesKey("cardArabic")
    val HADITH_GRADE_FILTER = stringPreferencesKey("hadithGradeFilter")
}

/** §1.7: unknown or legacy stored values fall back to the field's default. */
fun settingsFrom(prefs: Preferences): Settings {
    val default = Settings()
    return Settings(
        theme = prefs[Keys.THEME]?.let { stored -> runCatching { Theme.valueOf(stored) }.getOrNull() } ?: default.theme,
        arabicScript = prefs[Keys.ARABIC_SCRIPT]?.let { stored ->
            runCatching { ArabicScript.valueOf(stored) }.getOrNull()
        } ?: default.arabicScript,
        textSize = prefs[Keys.TEXT_SIZE]?.let { stored -> runCatching { TextSize.valueOf(stored) }.getOrNull() }
            ?: default.textSize,
        cardArabic = prefs[Keys.CARD_ARABIC] ?: default.cardArabic,
        hadithGradeFilter = prefs[Keys.HADITH_GRADE_FILTER]?.let { stored ->
            runCatching { HadithGradeFilter.valueOf(stored) }.getOrNull()
        } ?: default.hadithGradeFilter,
    )
}

/** §1.7 preferences: theme, arabicScript, textSize, cardArabic. The card theme itself is not persisted (parity). */
class SettingsRepository(private val dataStore: DataStore<Preferences>) {
    val settings: Flow<Settings> = dataStore.data.map(::settingsFrom)

    suspend fun setTheme(theme: Theme) {
        dataStore.edit { it[Keys.THEME] = theme.name }
    }

    suspend fun setArabicScript(script: ArabicScript) {
        dataStore.edit { it[Keys.ARABIC_SCRIPT] = script.name }
    }

    suspend fun setTextSize(size: TextSize) {
        dataStore.edit { it[Keys.TEXT_SIZE] = size.name }
    }

    suspend fun setCardArabic(value: Boolean) {
        dataStore.edit { it[Keys.CARD_ARABIC] = value }
    }

    suspend fun setHadithGradeFilter(filter: HadithGradeFilter) {
        dataStore.edit { it[Keys.HADITH_GRADE_FILTER] = filter.name }
    }
}
