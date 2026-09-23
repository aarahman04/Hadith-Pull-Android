package online.hadithpull.app.ui.nav

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import online.hadithpull.app.data.prefs.Settings
import online.hadithpull.app.di.AppContainer
import online.hadithpull.app.ui.about.AboutRoute
import online.hadithpull.app.ui.about.LicensesRoute
import online.hadithpull.app.ui.bookmarks.FolderRoute
import online.hadithpull.app.ui.bookmarks.FoldersRoute
import online.hadithpull.app.ui.components.HadithIcons
import online.hadithpull.app.ui.components.HadithTopBar
import online.hadithpull.app.ui.components.LocalToastState
import online.hadithpull.app.ui.components.ToastHost
import online.hadithpull.app.ui.components.rememberToastState
import online.hadithpull.app.ui.reader.ReaderRoute

/** §2.1: the three bottom-nav tab roots. Folder detail and Licenses (later steps) push on top of these. */
sealed interface TabRoute {
    @Serializable data object Reader : TabRoute
    @Serializable data object Folders : TabRoute
    @Serializable data object About : TabRoute
}

/** Pushed inside the Bookmarks tab's back stack (§2.1). Not a TabRoute: it isn't a tab root. */
@Serializable
data class FolderDetailRoute(val id: Long)

/** Pushed inside the About tab's back stack (§2.1). Not a TabRoute: it isn't a tab root. */
@Serializable
data object LicensesDetailRoute

private enum class Tab(val label: String, val icon: Int) {
    READER("Read", HadithIcons.openBook),
    FOLDERS("Bookmarks", HadithIcons.bookmark),
    ABOUT("About", HadithIcons.infoCircle),
}

/**
 * U1 bottom nav shell. §2.1 back rules:
 * 1) a sheet/dialog closing first isn't handled here — there are none yet.
 * 2) popping within a tab is NavController's default back behaviour.
 * 3) on a non-Reader tab root, back goes to Reader (BackHandler below).
 * 4) on Reader, back exits — no handler is installed, so the system default applies.
 */
@Composable
fun AppNav(container: AppContainer, darkTheme: Boolean, settings: Settings, onToggleTheme: () -> Unit) {
    val navController = rememberNavController()
    val toastState = rememberToastState()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    val currentTab = when {
        currentDestination?.hasRoute<TabRoute.Reader>() == true -> Tab.READER
        currentDestination?.hasRoute<TabRoute.Folders>() == true -> Tab.FOLDERS
        currentDestination?.hasRoute<FolderDetailRoute>() == true -> Tab.FOLDERS
        currentDestination?.hasRoute<TabRoute.About>() == true -> Tab.ABOUT
        currentDestination?.hasRoute<LicensesDetailRoute>() == true -> Tab.ABOUT
        else -> null
    }
    // Only a tab ROOT jumps straight to Reader on back (rule 3); a pushed screen like
    // Folder detail pops within its tab instead (rule 2), via the default back behaviour.
    val isTabRoot = currentDestination?.hasRoute<TabRoute.Reader>() == true ||
        currentDestination?.hasRoute<TabRoute.Folders>() == true ||
        currentDestination?.hasRoute<TabRoute.About>() == true

    if (currentTab != null && currentTab != Tab.READER && isTabRoot) {
        BackHandler {
            navController.navigate(TabRoute.Reader) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    fun navigateToTab(tab: Tab, route: TabRoute) {
        if (currentTab == tab) {
            navController.popBackStack(route = route, inclusive = false)
        } else {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    Scaffold(
        // Pushed screens (Folder detail, Licenses) draw their own back-arrow top bar instead.
        topBar = { if (isTabRoot) HadithTopBar(darkTheme = darkTheme, onToggleTheme = onToggleTheme) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentTab == Tab.READER,
                    onClick = { navigateToTab(Tab.READER, TabRoute.Reader) },
                    icon = { Icon(painterResource(Tab.READER.icon), contentDescription = Tab.READER.label) },
                    label = { Text(Tab.READER.label) },
                )
                NavigationBarItem(
                    selected = currentTab == Tab.FOLDERS,
                    onClick = { navigateToTab(Tab.FOLDERS, TabRoute.Folders) },
                    icon = { Icon(painterResource(Tab.FOLDERS.icon), contentDescription = Tab.FOLDERS.label) },
                    label = { Text(Tab.FOLDERS.label) },
                )
                NavigationBarItem(
                    selected = currentTab == Tab.ABOUT,
                    onClick = { navigateToTab(Tab.ABOUT, TabRoute.About) },
                    icon = { Icon(painterResource(Tab.ABOUT.icon), contentDescription = Tab.ABOUT.label) },
                    label = { Text(Tab.ABOUT.label) },
                )
            }
        },
    ) { contentPadding ->
        CompositionLocalProvider(LocalToastState provides toastState) {
            Box(Modifier.padding(contentPadding)) {
                NavHost(navController = navController, startDestination = TabRoute.Reader) {
                    composable<TabRoute.Reader> {
                        ReaderRoute(
                            container = container,
                            darkTheme = darkTheme,
                            settings = settings,
                            onNavigateToBookmarks = { navigateToTab(Tab.FOLDERS, TabRoute.Folders) },
                        )
                    }
                    composable<TabRoute.Folders> {
                        FoldersRoute(
                            container = container,
                            onOpenFolder = { id -> navController.navigate(FolderDetailRoute(id)) },
                            onGoToReader = { navigateToTab(Tab.READER, TabRoute.Reader) },
                        )
                    }
                    composable<FolderDetailRoute> { entry ->
                        val route = entry.toRoute<FolderDetailRoute>()
                        FolderRoute(
                            container = container,
                            darkTheme = darkTheme,
                            folderId = route.id,
                            onBack = { navController.popBackStack() },
                        )
                    }
                    composable<TabRoute.About> {
                        AboutRoute(
                            container = container,
                            theme = settings.theme,
                            onOpenLicenses = { navController.navigate(LicensesDetailRoute) },
                        )
                    }
                    composable<LicensesDetailRoute> {
                        LicensesRoute(onBack = { navController.popBackStack() })
                    }
                }
                ToastHost(
                    state = toastState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp),
                )
            }
        }
    }
}
