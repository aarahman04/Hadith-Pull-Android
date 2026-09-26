package online.hadithpull.app.data

import online.hadithpull.app.domain.DrawEngine
import online.hadithpull.app.domain.DrawResult
import online.hadithpull.app.data.prefs.HadithGradeFilter

/** H9: no network, so there is nothing to fall back from — P6/recent_hadiths is removed. */
class HadithRepository(private val drawEngine: DrawEngine) {
    suspend fun draw(
        currentKey: String?,
        filter: HadithGradeFilter = HadithGradeFilter.ALL_GRADES,
    ): DrawResult = drawEngine.draw(currentKey, filter)
}
