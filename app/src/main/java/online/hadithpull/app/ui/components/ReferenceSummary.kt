package online.hadithpull.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import online.hadithpull.app.domain.Hadith
import online.hadithpull.app.ui.theme.LocalHadithColors
import online.hadithpull.app.ui.theme.LocalHadithTypography

/** Round 3 §0.6: the unified reference block -- collection · number, chapter, grade pill and the
 * Sunnah link -- shared by the Reader dock and saved-hadith cards, instead of each screen
 * assembling its own reference layout. */
@Composable
fun ReferenceSummary(
    hadith: Hadith?,
    loading: Boolean,
    darkTheme: Boolean,
    onOpenSunnah: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalHadithColors.current
    val typography = LocalHadithTypography.current
    Column(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().heightIn(min = 28.dp), verticalAlignment = Alignment.CenterVertically) {
            when {
                hadith != null -> Text(
                    text = "${hadith.collectionTitle}  ·  Hadith ${hadith.ref}",
                    style = typography.contentMeta,
                    color = colors.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                loading -> Box(Modifier.weight(1f)) {
                    Box(
                        Modifier.fillMaxWidth(0.6f).height(14.dp)
                            .background(shimmerColor(), RoundedCornerShape(6.dp)),
                    )
                }
                else -> Spacer(Modifier.weight(1f))
            }
            if (hadith != null) {
                Spacer(Modifier.width(12.dp))
                StatusPill(primary = hadith.primary, darkTheme = darkTheme)
            }
        }
        Row(Modifier.fillMaxWidth().heightIn(min = 44.dp), verticalAlignment = Alignment.CenterVertically) {
            when {
                hadith != null && hadith.chapter.isNotEmpty() -> Text(
                    text = hadith.chapter,
                    style = typography.secondaryScaled,
                    color = colors.muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                loading -> Box(Modifier.weight(1f)) {
                    Box(
                        Modifier.fillMaxWidth(0.35f).height(12.dp)
                            .background(shimmerColor(), RoundedCornerShape(6.dp)),
                    )
                }
                else -> Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.width(12.dp))
            val sunnahUrl = hadith?.sunnahUrl
            TertiaryLink(
                label = "View on Sunnah.com",
                onClick = { sunnahUrl?.let(onOpenSunnah) },
                trailingIcon = painterResource(HadithIcons.openInNew),
                enabled = sunnahUrl != null,
                modifier = Modifier.alpha(if (sunnahUrl != null) 1f else 0f),
            )
        }
    }
}
