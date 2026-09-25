package online.hadithpull.app.ui.about

import androidx.activity.compose.BackHandler
import androidx.annotation.RawRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import online.hadithpull.app.R
import online.hadithpull.app.ui.components.HadithBackTopBar
import online.hadithpull.app.ui.theme.HadithShapes
import online.hadithpull.app.ui.theme.LocalHadithColors

private data class LicenseEntry(val title: String, val subtitle: String, @RawRes val textRes: Int)
private sealed interface LicenseRow {
    data class Single(val entry: LicenseEntry) : LicenseRow
    data class Group(val title: String, val subtitle: String, val entries: List<LicenseEntry>) : LicenseRow
}

/** §R0.2: the Unlicense-covered dataset stays prominent; Apache/OFL entries are grouped rather
 * than deleted, keeping every required notice reachable without ten individual rows. */
private val dataLicense = LicenseEntry(
    "Hadith API dataset (Fawaz Ahmed)",
    "Hadith texts, translations and gradings, compiled by Fawaz Ahmed and released into the public domain (The Unlicense).",
    R.raw.unlicense,
)
private val fontLicenses = listOf(
    LicenseEntry("Amiri", "OFL 1.1", R.raw.ofl_amiri),
    LicenseEntry("Scheherazade New", "OFL 1.1", R.raw.ofl_scheherazade_new),
    LicenseEntry("Noto Naskh Arabic", "OFL 1.1", R.raw.ofl_noto_naskh_arabic),
    LicenseEntry("Cormorant Garamond", "OFL 1.1", R.raw.ofl_cormorant_garamond),
    LicenseEntry("Inter", "OFL 1.1", R.raw.ofl_inter),
)
private val libraryNames = listOf(
    "Kotlin", "AndroidX Core KTX", "AndroidX Activity Compose", "Jetpack Compose", "AndroidX Lifecycle",
    "AndroidX Navigation Compose", "AndroidX Room", "AndroidX DataStore", "AndroidX Core SplashScreen",
    "kotlinx.serialization", "kotlinx.coroutines",
)
private val libraryLicenses = libraryNames.map { LicenseEntry(it, "Apache License 2.0", R.raw.apache_2_0) }

private val rows: List<LicenseRow> = listOf(
    LicenseRow.Single(dataLicense),
    LicenseRow.Group(
        "Fonts",
        "Amiri, Scheherazade New, Noto Naskh Arabic, Cormorant Garamond, Inter · SIL Open Font License 1.1",
        fontLicenses,
    ),
    LicenseRow.Group(
        "Third-party libraries",
        "Kotlin, AndroidX and Jetpack Compose libraries · Apache License 2.0",
        libraryLicenses,
    ),
)

/** §2.5/§R1.6: a static list; tapping a group drills into its own entries, and tapping any
 * entry shows its full text from res/raw/. No GMS oss-licenses plugin. */
@Composable
fun LicensesRoute(onBack: () -> Unit) {
    var expandedGroup by remember { mutableStateOf<LicenseRow.Group?>(null) }
    var selected by remember { mutableStateOf<LicenseEntry?>(null) }

    BackHandler(enabled = selected != null || expandedGroup != null) {
        if (selected != null) selected = null else expandedGroup = null
    }

    val title = selected?.title ?: expandedGroup?.title ?: "Open-source licenses"
    Column(Modifier.fillMaxSize()) {
        HadithBackTopBar(
            title = title,
            onBack = {
                when {
                    selected != null -> selected = null
                    expandedGroup != null -> expandedGroup = null
                    else -> onBack()
                }
            },
        )
        val current = selected
        val group = expandedGroup
        when {
            current != null -> LicenseText(current)
            group != null -> LicensesList(entries = group.entries, onSelect = { selected = it })
            else -> LicensesRowList(
                rows = rows,
                onSelectEntry = { selected = it },
                onSelectGroup = { expandedGroup = it },
            )
        }
    }
}

@Composable
private fun LicensesRowList(
    rows: List<LicenseRow>,
    onSelectEntry: (LicenseEntry) -> Unit,
    onSelectGroup: (LicenseRow.Group) -> Unit,
) {
    val colors = LocalHadithColors.current
    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        items(rows) { row ->
            val entry = when (row) {
                is LicenseRow.Single -> row.entry
                is LicenseRow.Group -> null
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface, HadithShapes.md)
                    .border(1.dp, colors.border, HadithShapes.md)
                    .clickable {
                        when (row) {
                            is LicenseRow.Single -> onSelectEntry(row.entry)
                            is LicenseRow.Group -> onSelectGroup(row)
                        }
                    }
                    .padding(16.dp),
            ) {
                val title = entry?.title ?: (row as LicenseRow.Group).title
                val subtitle = entry?.subtitle ?: (row as LicenseRow.Group).subtitle
                Text(text = title, color = colors.text, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                Text(text = subtitle, color = colors.muted, fontSize = 12.8.sp)
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun LicensesList(entries: List<LicenseEntry>, onSelect: (LicenseEntry) -> Unit) {
    val colors = LocalHadithColors.current
    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        items(entries) { entry ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface, HadithShapes.md)
                    .border(1.dp, colors.border, HadithShapes.md)
                    .clickable { onSelect(entry) }
                    .padding(16.dp),
            ) {
                Text(text = entry.title, color = colors.text, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                Text(text = entry.subtitle, color = colors.muted, fontSize = 12.8.sp)
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun LicenseText(entry: LicenseEntry) {
    val colors = LocalHadithColors.current
    val context = LocalContext.current
    val text = remember(entry.textRes) { readRawText(context, entry.textRes) }
    Text(
        text = text,
        color = colors.textSoft,
        fontSize = 12.8.sp,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    )
}

internal fun readRawText(context: android.content.Context, @RawRes id: Int): String =
    context.resources.openRawResource(id).bufferedReader().use { it.readText() }
