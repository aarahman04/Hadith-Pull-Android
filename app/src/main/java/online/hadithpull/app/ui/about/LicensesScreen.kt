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

private val fontLicenses = listOf(
    LicenseEntry("Amiri", "OFL 1.1 — Copyright 2010-2022 The Amiri Project Authors", R.raw.ofl_amiri),
    LicenseEntry("Scheherazade New", "OFL 1.1 — Copyright (c) 1994-2026, SIL Global", R.raw.ofl_scheherazade_new),
    LicenseEntry("Noto Naskh Arabic", "OFL 1.1 — Copyright 2022 The Noto Project Authors", R.raw.ofl_noto_naskh_arabic),
    LicenseEntry("Cormorant Garamond", "OFL 1.1 — Copyright 2015 the Cormorant Project Authors", R.raw.ofl_cormorant_garamond),
    LicenseEntry("Inter", "OFL 1.1 — Copyright 2020 The Inter Project Authors", R.raw.ofl_inter),
)

private val libraryLicenses = listOf(
    "Kotlin", "AndroidX Core KTX", "AndroidX Activity Compose", "Jetpack Compose", "AndroidX Lifecycle",
    "AndroidX Navigation Compose", "AndroidX Room", "AndroidX DataStore", "AndroidX Core SplashScreen",
    "kotlinx.serialization",
).map { LicenseEntry(it, "Apache License 2.0", R.raw.apache_2_0) }

private val dataLicenses = listOf(
    LicenseEntry("Hadith API dataset — Fawaz Ahmed", "The Unlicense", R.raw.unlicense),
)

private val allLicenses = fontLicenses + libraryLicenses + dataLicenses

/** §2.5: a static list; tapping an entry shows its full text from res/raw/. No GMS oss-licenses plugin. */
@Composable
fun LicensesRoute(onBack: () -> Unit) {
    var selected by remember { mutableStateOf<LicenseEntry?>(null) }

    BackHandler(enabled = selected != null) { selected = null }

    val current = selected
    Column(Modifier.fillMaxSize()) {
        HadithBackTopBar(
            title = current?.title ?: "Open-source licenses",
            onBack = { if (current != null) selected = null else onBack() },
        )
        if (current == null) {
            LicensesList(onSelect = { selected = it })
        } else {
            LicenseText(current)
        }
    }
}

@Composable
private fun LicensesList(onSelect: (LicenseEntry) -> Unit) {
    val colors = LocalHadithColors.current
    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        items(allLicenses) { entry ->
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
