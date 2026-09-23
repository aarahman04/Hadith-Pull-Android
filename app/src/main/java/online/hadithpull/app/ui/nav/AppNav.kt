package online.hadithpull.app.ui.nav

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import online.hadithpull.app.ui.components.HadithIcons
import online.hadithpull.app.ui.components.HadithTopBar

/** §2.1: the three bottom-nav tab roots. Folder detail and Licenses (later steps) push on top of these. */
sealed interface TabRoute {
    @Serializable data object Reader : TabRoute
    @Serializable data object Folders : TabRoute
    @Serializable data object About : TabRoute
}

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
fun AppNav(darkTheme: Boolean, onToggleTheme: () -> Unit) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    val currentTab = when {
        currentDestination?.hierarchy?.any { it.hasRoute<TabRoute.Reader>() } == true -> Tab.READER
        currentDestination?.hierarchy?.any { it.hasRoute<TabRoute.Folders>() } == true -> Tab.FOLDERS
        currentDestination?.hierarchy?.any { it.hasRoute<TabRoute.About>() } == true -> Tab.ABOUT
        else -> null
    }

    if (currentTab != null && currentTab != Tab.READER) {
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
        topBar = { HadithTopBar(darkTheme = darkTheme, onToggleTheme = onToggleTheme) },
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
        NavHost(
            navController = navController,
            startDestination = TabRoute.Reader,
            modifier = Modifier.padding(contentPadding),
        ) {
            composable<TabRoute.Reader> { PlaceholderTabBody("Reader") }
            composable<TabRoute.Folders> { PlaceholderTabBody("Bookmarks") }
            composable<TabRoute.About> { PlaceholderTabBody("About") }
        }
    }
}

@Composable
private fun PlaceholderTabBody(label: String) {
    Text(text = label, modifier = Modifier.padding(16.dp))
}
