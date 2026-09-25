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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import online.hadithpull.app.BuildConfig
import online.hadithpull.app.di.AppContainer
import online.hadithpull.app.ui.components.ChevronTrailing
import online.hadithpull.app.ui.components.ExternalTrailing
import online.hadithpull.app.ui.components.GroupedList
import online.hadithpull.app.ui.components.HadithCard
import online.hadithpull.app.ui.components.HadithPrimaryButton
import online.hadithpull.app.ui.components.ListRow
import online.hadithpull.app.ui.components.LocalToastState
import online.hadithpull.app.ui.components.RowDivider
import online.hadithpull.app.ui.components.ScreenHero
import online.hadithpull.app.ui.components.SectionHeader
import online.hadithpull.app.ui.theme.HadithShapes
import online.hadithpull.app.ui.theme.HadithSpacing
import online.hadithpull.app.ui.theme.LocalHadithColors
import online.hadithpull.app.ui.theme.LocalHadithTypography

private const val CONTACT_EMAIL = "aarahman803@gmail.com"

/** §2.5 About tab (About + Contact). Round 3 §Step 33: sections read through SectionHeader,
 * grouped lists replace boxed panels, and a quiet footer replaces the old stacked lines. */
@Composable
fun AboutRoute(container: AppContainer, onOpenLicenses: () -> Unit, onOpenPrivacy: () -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val toastState = LocalToastState.current

    AboutScreen(
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
    onOpenLicenses: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenLink: (String) -> Unit,
    onSendMessage: () -> Unit,
) {
    val colors = LocalHadithColors.current
    val typography = LocalHadithTypography.current
    val windowWidthDp = LocalConfiguration.current.screenWidthDp
    val sidePadding = if (windowWidthDp < 720) 16.dp else 20.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = sidePadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(HadithSpacing.xl))
        ScreenHero(eyebrow = "HADITH PULL", title = "About", titleStyle = typography.pageTitle, subline = null)
        Spacer(Modifier.height(6.dp))
        Text(
            text = "A quiet place to read one narration at a time.",
            color = colors.muted,
            fontSize = 16.3.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(HadithSpacing.xxl))

        Box(Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
            SectionHeader(eyebrow = "About the app", title = "Read. Reflect. Remember.")
        }
        Spacer(Modifier.height(HadithSpacing.md))
        Box(Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
            Column {
                Text(
                    text = "This project was built in my free time as a simple way to read and reflect on Hadith. " +
                        "The goal is to keep it clean, minimal, and focused on the message: no clutter, " +
                        "no noise, just the text.",
                    style = typography.body,
                    color = colors.textSoft,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Every narration is shown together with its full reference: the collection, the hadith " +
                        "number, the chapter, and the grading reported by the source. That way anyone reading " +
                        "it, or receiving a card you shared, can go back and cross-check it for themselves.",
                    style = typography.body,
                    color = colors.textSoft,
                )
                Spacer(Modifier.height(16.dp))
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
        }

        Spacer(Modifier.height(HadithSpacing.section))
        Box(Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
            SectionHeader(eyebrow = "Typography", title = "A note on the type")
        }
        Spacer(Modifier.height(HadithSpacing.md))
        Box(Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
            Column {
                Text(
                    text = "English narrations are set in Cormorant Garamond, and the interface in Inter. " +
                        "The Arabic comes in three typefaces: Naskh (Amiri), a classical book hand; Clear " +
                        "(Scheherazade New), rounder, with fuller vowel marks; and Bold (Noto Naskh Arabic), " +
                        "a denser cut close to South Asian print.",
                    style = typography.body,
                    color = colors.textSoft,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "All three show exactly the same text; only the letterforms change. The Indo-Pak " +
                        "style of the printed Mushaf isn't included, because it needs a differently encoded " +
                        "source text and a font with clear terms for redistribution.",
                    style = typography.body,
                    color = colors.textSoft,
                )
            }
        }

        Spacer(Modifier.height(HadithSpacing.section))
        Box(Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
            SectionHeader(eyebrow = "Sources", title = "Where the texts come from")
        }
        Spacer(Modifier.height(HadithSpacing.md))
        Box(Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
            Column {
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
                Spacer(Modifier.height(12.dp))
                HadithCard {
                    Text(text = "COLLECTIONS", style = typography.label, color = colors.muted)
                    Spacer(Modifier.height(10.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        sourceCollections.forEach { name -> SourceChip(name) }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Each narration shows the gradings recorded for it, with the scholar who gave each " +
                        "one; where scholars differ, Al-Albani's grading is shown first. Sahih al-Bukhari and " +
                        "Sahih Muslim carry no individual gradings. Their contents are accepted as authentic " +
                        "by scholarly consensus, so they are marked Sahih. For anything you intend to act on " +
                        "or pass along, please verify with the printed collection or a qualified scholar.",
                    style = typography.body,
                    color = colors.textSoft,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Collections are sequences, and some entries carry no text of their own, only " +
                        "“the same as above”, or a second chain of narrators for the hadith before it. " +
                        "Pulled out on their own they point at nothing, so they are skipped and another " +
                        "narration is drawn instead.",
                    style = typography.body,
                    color = colors.textSoft,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "The \"View on Sunnah.com\" link is built from each narration's reference, following " +
                        "Sunnah.com's own web address format. Hadith Pull does not pull any text or data from " +
                        "Sunnah.com: every narration, translation and grading shown here comes from the Hadith " +
                        "API dataset by Fawaz Ahmed, bundled with the app.",
                    style = typography.body,
                    color = colors.textSoft,
                )
            }
        }

        Spacer(Modifier.height(HadithSpacing.section))
        Box(Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
            SectionHeader(eyebrow = "Contact", title = "Say salam", lede = "Corrections, ideas, or just salam.")
        }
        Spacer(Modifier.height(HadithSpacing.md))
        Box(Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
            Column {
                HadithPrimaryButton(text = "Send a message", onClick = onSendMessage)
                Spacer(Modifier.height(12.dp))
                GroupedList {
                    ListRow(title = "Instagram", onClick = { onOpenLink("https://www.instagram.com/aarahmans/") }, trailing = { ExternalTrailing() })
                    RowDivider()
                    ListRow(title = "LinkedIn", onClick = { onOpenLink("https://www.linkedin.com/in/aarahman04/") }, trailing = { ExternalTrailing() })
                    RowDivider()
                    ListRow(title = "GitHub", onClick = { onOpenLink("https://github.com/aarahman04") }, trailing = { ExternalTrailing() })
                }
            }
        }

        Spacer(Modifier.height(HadithSpacing.section))
        Box(Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
            SectionHeader(eyebrow = "Legal")
        }
        Spacer(Modifier.height(HadithSpacing.md))
        Box(Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
            GroupedList {
                ListRow(title = "Privacy policy", onClick = onOpenPrivacy, trailing = { ChevronTrailing() })
                RowDivider()
                ListRow(title = "Open-source licenses", onClick = onOpenLicenses, trailing = { ChevronTrailing() })
            }
        }

        Spacer(Modifier.height(HadithSpacing.section))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Hadith Pull", color = colors.textSoft, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(Modifier.height(2.dp))
            Text(text = "Version ${BuildConfig.VERSION_NAME}", color = colors.muted, fontSize = 12.5.sp)
            Spacer(Modifier.height(6.dp))
            Text(text = "No accounts, no ads, no tracking.", color = colors.muted, fontSize = 12.5.sp)
        }
        Spacer(Modifier.height(HadithSpacing.xxl))
    }
}

private val sourceCollections = listOf(
    "Sahih al-Bukhari", "Sahih Muslim", "Sunan Abi Dawud", "Jami` at-Tirmidhi", "Sunan an-Nasa'i",
    "Sunan Ibn Majah", "Muwatta Malik", "The Forty Hadith of al-Nawawi", "The Forty Hadith Qudsi",
    "The Forty Hadith of Shah Waliullah",
)

@Composable
private fun SourceChip(label: String) {
    val colors = LocalHadithColors.current
    Box(
        modifier = Modifier
            .background(colors.bg, HadithShapes.pill)
            .border(1.dp, colors.border, HadithShapes.pill)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(text = label, color = colors.textSoft, fontSize = 13.sp)
    }
}
