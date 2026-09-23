package online.hadithpull.app

import android.app.Application
import online.hadithpull.app.di.AppContainer

class HadithPullApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
