package online.hadithpull.app.ui.about

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import online.hadithpull.app.BuildConfig
import online.hadithpull.app.di.AppContainer
import online.hadithpull.app.ui.components.ChevronTrailing
import online.hadithpull.app.ui.components.HadithIcons
import online.hadithpull.app.ui.components.LocalToastState
import online.hadithpull.app.ui.components.RowDivider
import online.hadithpull.app.ui.components.ScreenHero
import online.hadithpull.app.ui.theme.HadithShapes
import online.hadithpull.app.ui.theme.HadithSpacing
import online.hadithpull.app.ui.theme.LocalHadithColors
import online.hadithpull.app.ui.theme.LocalHadithTypography

private const val CONTACT_EMAIL = "aarahman803@gmail.com"

/** §2.5 About tab (About + Contact). Round 3 §Step 33: sections read through SectionHeader,
 * grouped lists replace boxed panels, and a quiet footer replaces the old stacked lines. */
@Composable
fun AboutRoute(container: AppContainer, onOpenLicenses: () -> Unit, onOpenPrivacy: () -> Unit) {
    val context = LocalContext.current
    val toastState = LocalToastState.current

    AboutScreen(
        onOpenLicenses = onOpenLicenses,
        onOpenPrivacy = onOpenPrivacy,
        onOpenLink = { url -> context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) },
        onSendMessage = {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$CONTACT_EMAIL")
                putExtra(Intent.EXTRA_SUBJECT, "Hadith Pull Feedback")
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
        Spacer(Modifier.height(HadithSpacing.xxl))

        Box(Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
            AboutSectionHeading(label = "The Idea", title = "Read, Reflect, Remember.")
        }
        Spacer(Modifier.height(HadithSpacing.md))
        Box(Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
            Column {
                Text(
                    text = "This project was built in my free time as a simple way to read and reflect on Hadith. " +
                        "The goal is to keep it clean, minimal, and focused on the message: no clutter, " +
                        "no noise, just the text.",
                    style = typography.helper,
                    color = colors.textSoft,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Every narration is shown together with its full reference: the collection, the hadith " +
                        "number, the chapter, and the grading reported by the source. That way anyone reading " +
                        "it, or receiving a card you shared, can go back and cross-check it for themselves.",
                    style = typography.helper,
                    color = colors.textSoft,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = buildAnnotatedString {
                        append("The translation is shown first, on its own. Tap ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Show full Hadith") }
                        append(" for the complete text together with the Arabic.")
                    },
                    style = typography.helper,
                    color = colors.textSoft,
                )
            }
        }

        Spacer(Modifier.height(HadithSpacing.section))
        Box(Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
            AboutSectionHeading(label = "Typography", title = "A Note on the Type")
        }
        Spacer(Modifier.height(HadithSpacing.md))
        Box(Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
            Column {
                Text(
                    text = "English narrations are set in Cormorant Garamond, and the interface in Inter. " +
                        "The Arabic comes in three typefaces: Naskh (Amiri), a classical book hand; Clear " +
                        "(Scheherazade New), rounder, with fuller vowel marks; and Bold (Noto Naskh Arabic), " +
                        "a denser cut close to South Asian print.",
                    style = typography.helper,
                    color = colors.textSoft,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "All three show exactly the same text; only the letterforms change. The Indo-Pak " +
                        "style of the printed Mushaf isn't included, because it needs a differently encoded " +
                        "source text and a font with clear terms for redistribution.",
                    style = typography.helper,
                    color = colors.textSoft,
                )
            }
        }

        Spacer(Modifier.height(HadithSpacing.section))
        Box(Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
            AboutSectionHeading(label = "Sources", title = "Where the Texts Come From")
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
                    style = typography.helper,
                    color = colors.textSoft,
                    modifier = Modifier.clickable { onOpenLink("https://hadithapi.com/") },
                )
                Spacer(Modifier.height(12.dp))
                Column {
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
                    style = typography.helper,
                    color = colors.textSoft,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Collections are sequences, and some entries carry no text of their own, only " +
                        "“the same as above”, or a second chain of narrators for the hadith before it. " +
                        "Pulled out on their own they point at nothing, so they are skipped and another " +
                        "narration is drawn instead.",
                    style = typography.helper,
                    color = colors.textSoft,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "The \"View on Sunnah.com\" link is built from each narration's reference, following " +
                        "Sunnah.com's own web address format. Hadith Pull does not pull any text or data from " +
                        "Sunnah.com: every narration, translation and grading shown here comes from the Hadith " +
                        "API dataset by Fawaz Ahmed, bundled with the app.",
                    style = typography.body.copy(fontSize = 15.sp, lineHeight = 24.sp),
                    color = colors.textSoft,
                )
            }
        }

        Spacer(Modifier.height(HadithSpacing.section))
        Box(Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
            ContactSection(onSendMessage = onSendMessage, onOpenLink = onOpenLink)
        }

        Spacer(Modifier.height(HadithSpacing.section))
        Box(Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
            AboutSectionHeading(label = "Legal")
        }
        Spacer(Modifier.height(HadithSpacing.md))
        Box(Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
            LegalLinks(onOpenPrivacy = onOpenPrivacy, onOpenLicenses = onOpenLicenses)
        }

        Spacer(Modifier.height(HadithSpacing.section))
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 8.dp)) {
            Text(text = "Hadith Pull", color = colors.textSoft.copy(alpha = 0.82f), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(Modifier.height(2.dp))
            Text(text = "Version ${BuildConfig.VERSION_NAME}", color = colors.muted.copy(alpha = 0.78f), fontSize = 12.5.sp)
            Spacer(Modifier.height(10.dp))
        }
        Spacer(Modifier.height(HadithSpacing.xxl))
    }
}

@Composable
private fun AboutSectionHeading(label: String, title: String? = null) {
    val colors = LocalHadithColors.current
    val typography = LocalHadithTypography.current
    Column(Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = typography.sectionTitle,
            color = colors.accentInk,
        )
        if (title != null) {
            Spacer(Modifier.height(9.dp))
            Text(
                text = title,
                style = typography.sectionTitle.copy(
                    fontSize = 18.sp,
                    lineHeight = 1.25.em,
                    fontWeight = FontWeight.Medium,
                ),
                color = colors.text,
            )
        }
    }
}

@Composable
private fun LegalLinks(
    onOpenPrivacy: () -> Unit,
    onOpenLicenses: () -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        LegalLinkRow(title = "Privacy policy", onClick = onOpenPrivacy)
        RowDivider()
        LegalLinkRow(title = "Open-source licenses", onClick = onOpenLicenses)
    }
}

@Composable
private fun LegalLinkRow(title: String, onClick: () -> Unit) {
    val colors = LocalHadithColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = title, color = colors.textSoft, fontSize = 14.sp, modifier = Modifier.weight(1f))
        ChevronTrailing()
    }
}

@Composable
private fun ContactSection(
    onSendMessage: () -> Unit,
    onOpenLink: (String) -> Unit,
) {
    val colors = LocalHadithColors.current
    val typography = LocalHadithTypography.current
    Column(Modifier.fillMaxWidth()) {
        AboutSectionHeading(label = "Contact")
        Spacer(Modifier.height(HadithSpacing.md))
        Text(
            text = "Questions, corrections, ideas, or just salam.",
            style = typography.helper,
            color = colors.textSoft,
        )
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clickable(onClick = onSendMessage)
                .semantics { role = Role.Button },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "Send a message", style = typography.helper, color = colors.accentInk)
            Spacer(Modifier.width(6.dp))
            Text(text = "→", style = typography.helper, color = colors.accentInk)
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SocialIcon(
                icon = HadithIcons.github,
                label = "GitHub",
                onClick = { onOpenLink("https://github.com/aarahman04") },
            )
            SocialIcon(
                icon = HadithIcons.linkedin,
                label = "LinkedIn",
                onClick = { onOpenLink("https://www.linkedin.com/in/aarahman04/") },
            )
            SocialIcon(
                icon = HadithIcons.instagram,
                label = "Instagram",
                onClick = { onOpenLink("https://www.instagram.com/aarahmans/") },
            )
        }
    }
}

@Composable
private fun SocialIcon(
    @androidx.annotation.DrawableRes icon: Int,
    label: String,
    onClick: () -> Unit,
) {
    val colors = LocalHadithColors.current
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = androidx.compose.animation.core.tween(120),
        label = "socialIconPress",
    )
    IconButton(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier.size(48.dp).scale(scale),
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = label,
            tint = colors.textSoft,
            modifier = Modifier.size(20.dp),
        )
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
