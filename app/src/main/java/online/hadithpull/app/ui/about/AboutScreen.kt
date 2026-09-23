package online.hadithpull.app.ui.about

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import online.hadithpull.app.BuildConfig
import online.hadithpull.app.data.prefs.Theme
import online.hadithpull.app.di.AppContainer
import online.hadithpull.app.ui.components.HadithIcons
import online.hadithpull.app.ui.components.LocalToastState
import online.hadithpull.app.ui.theme.HadithShapes
import online.hadithpull.app.ui.theme.LocalHadithColors
import online.hadithpull.app.ui.theme.LocalHadithTypography

private const val CONTACT_EMAIL = "aarahman803@gmail.com"

/** §2.5 About tab (About + Contact + Appearance). */
@Composable
fun AboutRoute(container: AppContainer, theme: Theme, onOpenLicenses: () -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val toastState = LocalToastState.current

    AboutScreen(
        theme = theme,
        onSetTheme = { newTheme -> scope.launch { container.settingsRepository.setTheme(newTheme) } },
        onOpenLicenses = onOpenLicenses,
        onOpenLink = { url -> context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) },
        onSendMessage = {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$CONTACT_EMAIL")
                putExtra(Intent.EXTRA_SUBJECT, "Message from Hadith Pull")
            }
            try {
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                toastState.show("No email app found")
            }
        },
    )
}

@Composable
private fun AboutScreen(
    theme: Theme,
    onSetTheme: (Theme) -> Unit,
    onOpenLicenses: () -> Unit,
    onOpenLink: (String) -> Unit,
    onSendMessage: () -> Unit,
) {
    val colors = LocalHadithColors.current
    val typography = LocalHadithTypography.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(24.dp))
        Text(text = "About", style = typography.pageTitle, color = colors.text)
        Spacer(Modifier.height(6.dp))
        Text(text = "A quiet place to read one narration at a time.", color = colors.muted, fontSize = 16.3.sp)
        Spacer(Modifier.height(20.dp))

        Panel {
            Text(
                text = "This project was built in my free time as a simple way to read and reflect on Hadith. " +
                    "The goal is to keep it clean, minimal, and focused on the message — no clutter, " +
                    "no noise, just the text.",
                style = typography.body,
                color = colors.textSoft,
            )
            Spacer(Modifier.height(19.dp))
            Text(
                text = "Every narration is shown together with its full reference: the collection, the hadith " +
                    "number, the chapter, and the grading reported by the source. That way anyone reading " +
                    "it — or receiving a card you shared — can go back and cross-check it for themselves.",
                style = typography.body,
                color = colors.textSoft,
            )
            Spacer(Modifier.height(19.dp))
            Text(
                text = buildAnnotatedString {
                    append("The translation is shown first, on its own. Tap ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Show full Hadith") }
                    append(" for the complete text together with the Arabic.")
                },
                style = typography.body,
                color = colors.textSoft,
            )
        }
        Spacer(Modifier.height(19.dp))

        Panel {
            PanelTitle("A note on the Arabic")
            Text(
                text = "The Arabic can be set in three typefaces: Naskh (Amiri), the classical book hand; " +
                    "Clear (Scheherazade New), larger and rounder with fuller vowel marks; and Bold " +
                    "(Noto Naskh Arabic), a denser cut closer to the block-print style common in South Asia.",
                style = typography.body,
                color = colors.textSoft,
            )
            Spacer(Modifier.height(19.dp))
            Text(
                text = "All three render exactly the same characters. The source provides a single Arabic " +
                    "version, so this changes the letterforms only — none of them is a conversion to the " +
                    "Indo-Pak orthography used in the printed Mushaf, which differs from standard Arabic in " +
                    "its spelling and diacritic conventions and would need both a differently encoded source " +
                    "text and a font licensed for that specific purpose. The community fonts that do " +
                    "reproduce it (Al Qalam, PDMS Saleem and similar) are freeware without clear terms for " +
                    "redistribution, so none is bundled here.",
                style = typography.body,
                color = colors.textSoft,
            )
        }
        Spacer(Modifier.height(19.dp))

        Panel {
            PanelTitle("Where the texts come from")
            Text(
                text = buildAnnotatedString {
                    append("Narrations are fetched live from ")
                    withStyle(SpanStyle(color = colors.accent, fontWeight = FontWeight.Medium)) { append("HadithAPI") }
                    append(", which draws on these collections:")
                },
                style = typography.body,
                color = colors.textSoft,
                modifier = Modifier.clickable { onOpenLink("https://hadithapi.com") },
            )
            Spacer(Modifier.height(14.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                sourceCollections.forEach { name -> SourceChip(name) }
            }
            Spacer(Modifier.height(19.dp))
            Text(
                text = "Gradings are reproduced as reported by the source. For anything you intend to act on " +
                    "or pass along, please verify with the printed collection or a qualified scholar.",
                style = typography.body,
                color = colors.textSoft,
            )
            Spacer(Modifier.height(19.dp))
            Text(
                text = "Collections are sequences, and some entries carry no text of their own — only " +
                    "“the same as above”, or a second chain of narrators for the hadith before it. " +
                    "Pulled out on their own they point at nothing, so they are skipped and another " +
                    "narration is drawn instead.",
                style = typography.body,
                color = colors.textSoft,
            )
        }
        Spacer(Modifier.height(19.dp))

        Panel {
            PanelTitle("Appearance")
            Spacer(Modifier.height(14.dp))
            ThemeSegmentedControl(theme, onSetTheme)
        }
        Spacer(Modifier.height(19.dp))

        Panel {
            PanelTitle("Say salam")
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Corrections, ideas, or just salam — all welcome.",
                style = typography.body,
                color = colors.textSoft,
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onSendMessage, shape = HadithShapes.pill, modifier = Modifier.fillMaxWidth()) {
                Text("Send a message")
            }
            Spacer(Modifier.height(10.dp))
            SocialButton("Instagram") { onOpenLink("https://www.instagram.com/aarahmans/") }
            Spacer(Modifier.height(10.dp))
            SocialButton("LinkedIn") { onOpenLink("https://www.linkedin.com/in/aarahman04/") }
            Spacer(Modifier.height(10.dp))
            SocialButton("GitHub") { onOpenLink("https://github.com/aarahman04") }
        }
        Spacer(Modifier.height(19.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface, HadithShapes.lg)
                .border(1.dp, colors.border, HadithShapes.lg)
                .clickable(onClick = onOpenLicenses)
                .padding(22.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "Open-source licenses", color = colors.text, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.height(24.dp))

        Column(horizontalAlignment = Alignment.Start) {
            Text(text = "Hadith Pull — built for quiet reading.", color = colors.muted, fontSize = 13.4.sp)
            Text(text = "Texts via HadithAPI", color = colors.muted, fontSize = 13.4.sp)
            Text(text = "Version ${BuildConfig.VERSION_NAME}", color = colors.muted, fontSize = 13.4.sp)
        }
        Spacer(Modifier.height(24.dp))
    }
}

private val sourceCollections = listOf(
    "Sahih Bukhari", "Sahih Muslim", "Jami' at-Tirmidhi", "Sunan Abu Dawood", "Sunan Ibn Majah",
    "Sunan an-Nasa'i", "Mishkat al-Masabih", "Musnad Ahmad", "Al-Silsila Sahiha",
)

@Composable
private fun Panel(content: @Composable () -> Unit) {
    val colors = LocalHadithColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface, HadithShapes.lg)
            .border(1.dp, colors.border, HadithShapes.lg)
            .padding(vertical = 26.dp, horizontal = 22.dp),
    ) {
        content()
    }
}

@Composable
private fun PanelTitle(text: String) {
    val colors = LocalHadithColors.current
    val typography = LocalHadithTypography.current
    Text(text = text, style = typography.panelTitle, color = colors.text)
    Spacer(Modifier.height(14.dp))
}

@Composable
private fun SourceChip(label: String) {
    val colors = LocalHadithColors.current
    Box(
        modifier = Modifier
            .background(colors.bg, HadithShapes.pill)
            .border(1.dp, colors.borderStrong, HadithShapes.pill)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(text = label, color = colors.textSoft, fontSize = 13.4.sp)
    }
}

@Composable
private fun ThemeSegmentedControl(current: Theme, onSelect: (Theme) -> Unit) {
    val colors = LocalHadithColors.current
    Row(
        modifier = Modifier
            .background(colors.bg, HadithShapes.pill)
            .border(1.dp, colors.border, HadithShapes.pill)
            .padding(3.dp),
    ) {
        Theme.entries.forEach { theme ->
            val selected = theme == current
            Box(
                modifier = Modifier
                    .clickable { onSelect(theme) }
                    .background(if (selected) colors.accentSoft else androidx.compose.ui.graphics.Color.Transparent, HadithShapes.pill)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text(
                    text = theme.name.lowercase().replaceFirstChar { it.uppercase() },
                    color = if (selected) colors.accent else colors.muted,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                )
            }
        }
    }
}

@Composable
private fun SocialButton(label: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = HadithShapes.pill,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = LocalHadithColors.current.text),
        border = androidx.compose.foundation.BorderStroke(1.dp, LocalHadithColors.current.border),
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Icon(painter = painterResource(HadithIcons.openInNew), contentDescription = null, modifier = Modifier.size(16.dp))
    }
}
