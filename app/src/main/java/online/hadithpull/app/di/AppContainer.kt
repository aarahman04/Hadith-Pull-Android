package online.hadithpull.app.di

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import java.io.File
import online.hadithpull.app.data.BookmarkRepository
import online.hadithpull.app.data.HadithRepository
import online.hadithpull.app.data.HadithStore
import online.hadithpull.app.data.local.HadithPullDatabase
import online.hadithpull.app.data.prefs.SettingsRepository
import online.hadithpull.app.domain.DrawEngine

/** §0.4: manual DI, one AppContainer owned by the Application. No Hilt. */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val hadithStore = HadithStore(appContext.assets)
    private val drawEngine = DrawEngine(hadithStore)

    val database: HadithPullDatabase = Room.databaseBuilder(
        appContext,
        HadithPullDatabase::class.java,
        "hadithpull.db",
    ).build()

    val bookmarkRepository = BookmarkRepository(database)
    val hadithRepository = HadithRepository(drawEngine)

    val settingsRepository = SettingsRepository(
        PreferenceDataStoreFactory.create(
            produceFile = { File(appContext.filesDir, "datastore/settings.preferences_pb") },
        ),
    )
}
