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
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

/** §2.5 About tab (About + Contact + Appearance). §R1.6: centred hero, an editorial prose
 * column instead of boxed panels, and grouped-list surfaces for the interactive sections. */
@Composable
fun AboutRoute(container: AppContainer, theme: Theme, onOpenLicenses: () -> Unit, onOpenPrivacy: () -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val toastState = LocalToastState.current

    AboutScreen(
        theme = theme,
        onSetTheme = { newTheme -> scope.launch { container.settingsRepository.setTheme(newTheme) } },
        onOpenLicenses = onOpenLicenses,
        onOpenPrivacy = onOpenPrivacy,
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
    onOpenPrivacy: () -> Unit,
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
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(28.dp).height(1.dp).background(colors.borderStrong))
            Text(
                text = "HADITH PULL",
                style = typography.eyebrow,
                color = colors.muted,
                modifier = Modifier.padding(horizontal = 10.dp),
            )
            Box(Modifier.width(28.dp).height(1.dp).background(colors.borderStrong))
        }
        Spacer(Modifier.height(12.dp))
        Text(text = "About", style = typography.pageTitle, color = colors.text, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(
            text = "A quiet place to read one narration at a time.",
            color = colors.muted,
            fontSize = 16.3.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(28.dp))

        ProseSection(eyebrow = "ABOUT THE APP", title = "Read. Reflect. Remember.") {
            Text(
                text = "This project was built in my free time as a simple way to read and reflect on Hadith. " +
                    "The goal is to keep it clean, minimal, and focused on the message: no clutter, " +
                    "no noise, just the text.",
                style = typography.body,
                color = colors.textSoft,
            )
            Spacer(Modifier.height(19.dp))
            Text(
                text = "Every narration is shown together with its full reference: the collection, the hadith " +
                    "number, the chapter, and the grading reported by the source. That way anyone reading " +
                    "it, or receiving a card you shared, can go back and cross-check it for themselves.",
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
        SectionDivider()

        ProseSection(eyebrow = "TYPEFACES", title = "A note on the Arabic") {
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
                    "version, so this changes the letterforms only. None of them is a conversion to the " +
                    "Indo-Pak orthography used in the printed Mushaf, which differs from standard Arabic in " +
                    "its spelling and diacritic conventions and would need both a differently encoded source " +
                    "text and a font licensed for that specific purpose. The community fonts that do " +
                    "reproduce it (Al Qalam, PDMS Saleem and similar) are freeware without clear terms for " +
                    "redistribution, so none is bundled here.",
                style = typography.body,
                color = colors.textSoft,
            )
        }
        SectionDivider()

        ProseSection(eyebrow = "SOURCE", title = "Where the texts come from") {
            Text(
                text = buildAnnotatedString {
                    append("Narrations come from the open-source ")
                    withStyle(SpanStyle(color = colors.accent, fontWeight = FontWeight.Medium)) { append("Hadith API") }
                    append(
                        " collection by Fawaz Ahmed, stored inside the app so it works entirely offline. " +
                            "It covers these collections:",
                    )
                },
                style = typography.body,
                color = colors.textSoft,
                modifier = Modifier.clickable { onOpenLink("https://github.com/fawazahmed0/hadith-api") },
            )
            Spacer(Modifier.height(14.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                sourceCollections.forEach { name -> SourceChip(name) }
            }
            Spacer(Modifier.height(19.dp))
            Text(
                text = "Each narration shows the gradings recorded for it, with the scholar who gave each " +
                    "one; where scholars differ, Al-Albani's grading is shown first. Sahih al-Bukhari and " +
                    "Sahih Muslim carry no individual gradings. Their contents are accepted as authentic " +
                    "by scholarly consensus, so they are marked Sahih. For anything you intend to act on " +
                    "or pass along, please verify with the printed collection or a qualified scholar.",
                style = typography.body,
                color = colors.textSoft,
            )
            Spacer(Modifier.height(19.dp))
            Text(
                text = "Collections are sequences, and some entries carry no text of their own, only " +
                    "“the same as above”, or a second chain of narrators for the hadith before it. " +
                    "Pulled out on their own they point at nothing, so they are skipped and another " +
                    "narration is drawn instead.",
                style = typography.body,
                color = colors.textSoft,
            )
            Spacer(Modifier.height(19.dp))
            Text(
                text = "The \"View on Sunnah.com\" link is built from each narration's reference, following " +
                    "Sunnah.com's own web address format. Hadith Pull does not pull any text or data from " +
                    "Sunnah.com: every narration, translation and grading shown here comes from the Hadith " +
                    "API dataset by Fawaz Ahmed, bundled with the app.",
                style = typography.body,
                color = colors.textSoft,
            )
        }
        Spacer(Modifier.height(28.dp))

        GroupPanel {
            PanelTitle("Appearance")
            Spacer(Modifier.height(14.dp))
            ThemeSegmentedControl(theme, onSetTheme)
        }
        Spacer(Modifier.height(19.dp))

        GroupPanel {
            PanelTitle("Say salam")
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Corrections, ideas, or just salam. All welcome.",
                style = typography.body,
                color = colors.textSoft,
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onSendMessage, shape = HadithShapes.pill, modifier = Modifier.fillMaxWidth()) {
                Text("Send a message")
            }
            Spacer(Modifier.height(16.dp))
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(colors.surfaceSolid, RoundedCornerShape(16.dp))
                    .border(1.dp, colors.border, RoundedCornerShape(16.dp)),
            ) {
                LinkRow("Instagram", trailingIconRes = HadithIcons.openInNew) { onOpenLink("https://www.instagram.com/aarahmans/") }
                RowDivider()
                LinkRow("LinkedIn", trailingIconRes = HadithIcons.openInNew) { onOpenLink("https://www.linkedin.com/in/aarahman04/") }
                RowDivider()
                LinkRow("GitHub", trailingIconRes = HadithIcons.openInNew) { onOpenLink("https://github.com/aarahman04") }
            }
        }
        Spacer(Modifier.height(19.dp))

        GroupPanel {
            PanelTitle("Legal")
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(colors.surfaceSolid, RoundedCornerShape(16.dp))
                    .border(1.dp, colors.border, RoundedCornerShape(16.dp)),
            ) {
                LinkRow("Privacy policy", chevron = true, onClick = onOpenPrivacy)
                RowDivider()
                LinkRow("Open-source licenses", chevron = true, onClick = onOpenLicenses)
            }
        }
        Spacer(Modifier.height(24.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Hadith Pull, built for quiet reading.", color = colors.muted, fontSize = 13.4.sp)
            Text(text = "Texts via Hadith API", color = colors.muted, fontSize = 13.4.sp)
            Text(text = "Version ${BuildConfig.VERSION_NAME}", color = colors.muted, fontSize = 13.4.sp)
        }
        Spacer(Modifier.height(24.dp))
    }
}

private val sourceCollections = listOf(
    "Sahih al-Bukhari", "Sahih Muslim", "Sunan Abi Dawud", "Jami` at-Tirmidhi", "Sunan an-Nasa'i",
    "Sunan Ibn Majah", "Muwatta Malik", "The Forty Hadith of al-Nawawi", "The Forty Hadith Qudsi",
    "The Forty Hadith of Shah Waliullah",
)

/** §R1.6: the free-text sections read as an editorial column -- a section eyebrow, a Cormorant
 * title, then body copy -- with no surrounding box or border. */
@Composable
private fun ProseSection(eyebrow: String, title: String, content: @Composable ColumnScope.() -> Unit) {
    val colors = LocalHadithColors.current
    val typography = LocalHadithTypography.current
    Column(Modifier.fillMaxWidth()) {
        Text(text = eyebrow.uppercase(), style = typography.sectionLabel, color = colors.accentInk)
        Spacer(Modifier.height(6.dp))
        Text(text = title, style = typography.panelTitle, color = colors.text)
        Spacer(Modifier.height(14.dp))
        content()
    }
}

@Composable
private fun SectionDivider() {
    val colors = LocalHadithColors.current
    Spacer(Modifier.height(20.dp))
    Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
    Spacer(Modifier.height(28.dp))
}

/** The three interactive groups (Appearance, Say salam, Legal) keep a bordered surface, unlike
 * the prose sections above -- they hold controls, not text, so the boundary still earns its
 * keep. */
@Composable
private fun GroupPanel(content: @Composable ColumnScope.() -> Unit) {
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
            .background(colors.surfaceSolid, HadithShapes.pill)
            .border(1.dp, colors.border, HadithShapes.pill)
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
                    .background(if (selected) colors.accentSoft else Color.Transparent, HadithShapes.pill)
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

/** §R1.6: replaces the old stacked SocialButton/plain-Row destinations -- one row in a
 * grouped-list surface, label + trailing icon, the whole row clickable. `chevron = true` (the
 * in-app destinations, Privacy/Licenses) draws a chevron instead of the open-in-new glyph the
 * external links (Instagram/LinkedIn/GitHub, via `trailingIconRes`) use. */
@Composable
private fun LinkRow(label: String, trailingIconRes: Int? = null, chevron: Boolean = false, onClick: () -> Unit) {
    val colors = LocalHadithColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, color = colors.text, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        if (chevron) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.muted,
                modifier = Modifier.size(16.dp),
            )
        } else if (trailingIconRes != null) {
            Icon(
                painter = painterResource(trailingIconRes),
                contentDescription = null,
                tint = colors.muted,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun RowDivider() {
    val colors = LocalHadithColors.current
    Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
}
