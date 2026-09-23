package online.hadithpull.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import kotlinx.coroutines.launch
import online.hadithpull.app.data.prefs.Theme
import online.hadithpull.app.ui.components.Backdrop
import online.hadithpull.app.ui.nav.AppNav
import online.hadithpull.app.ui.theme.HadithTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        var settingsLoaded = false
        splashScreen.setKeepOnScreenCondition { !settingsLoaded }

        enableEdgeToEdge()

        setContent {
            val container = (application as HadithPullApp).container
            val settings by container.settingsRepository.settings.collectAsState(initial = null)
            val current = settings
            if (current != null) {
                settingsLoaded = true
                val systemDark = isSystemInDarkTheme()
                val darkTheme = when (current.theme) {
                    Theme.SYSTEM -> systemDark
                    Theme.LIGHT -> false
                    Theme.DARK -> true
                }
                val scope = rememberCoroutineScope()

                // Edge-to-edge: system-bar icon contrast follows the effective app theme.
                SideEffect {
                    val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                    insetsController.isAppearanceLightStatusBars = !darkTheme
                    insetsController.isAppearanceLightNavigationBars = !darkTheme
                }

                HadithTheme(
                    darkTheme = darkTheme,
                    arabicScript = current.arabicScript,
                    textSize = current.textSize,
                ) {
                    Box(Modifier.fillMaxSize()) {
                        Backdrop(darkTheme = darkTheme, modifier = Modifier.fillMaxSize())
                        AppNav(
                            container = container,
                            darkTheme = darkTheme,
                            settings = current,
                            onToggleTheme = {
                                // §1.7: stores LIGHT if the effective theme is dark, else DARK.
                                scope.launch {
                                    container.settingsRepository.setTheme(
                                        if (darkTheme) Theme.LIGHT else Theme.DARK,
                                    )
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}
