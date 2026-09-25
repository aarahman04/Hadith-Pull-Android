package online.hadithpull.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import online.hadithpull.app.ui.theme.HadithShapes
import online.hadithpull.app.ui.theme.LocalHadithColors
import online.hadithpull.app.ui.theme.LocalHadithTypography

/** Round 3 §0.4: the one action-hierarchy convention used on every screen that has a primary and
 * a secondary/tertiary action. */
enum class ActionTone { Neutral, Accent, Destructive }

/** Primary: a filled pill, at most one per screen or sheet, always the last constructive block
 * in its area. */
@Composable
fun HadithPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    compact: Boolean = false,
) {
    val colors = LocalHadithColors.current
    val typography = LocalHadithTypography.current
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = HadithShapes.pill,
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.text,
            contentColor = colors.bg,
            disabledContainerColor = colors.text.copy(alpha = 0.55f),
            disabledContentColor = colors.bg,
        ),
        contentPadding = PaddingValues(horizontal = 24.dp),
        modifier = modifier.fillMaxWidth().height(if (compact) 44.dp else 50.dp),
    ) {
        if (leadingIcon != null) {
            Box(
                modifier = Modifier.size(20.dp),
                contentAlignment = Alignment.Center,
            ) {
                leadingIcon()
            }
            Spacer(Modifier.width(8.dp))
        }
        Text(text = text, style = typography.secondaryAction, color = colors.bg)
    }
}

/** Secondary: no fill, no border -- an icon + label row, grouped under or beside the primary.
 * Never styled as a button. `liveLabel` marks the label as a polite live region, for feedback
 * text that changes in place (e.g. Copy -> Copied). `iconRotation` rotates the icon in place
 * (e.g. the Show full/less chevron). */
@Composable
fun SecondaryAction(
    label: String,
    icon: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tone: ActionTone = ActionTone.Neutral,
    liveLabel: Boolean = false,
    iconRotation: Float = 0f,
    compact: Boolean = false,
) {
    val colors = LocalHadithColors.current
    val typography = LocalHadithTypography.current
    val color = when {
        !enabled -> colors.muted.copy(alpha = 0.45f)
        tone == ActionTone.Accent -> colors.accent
        tone == ActionTone.Destructive -> colors.error
        else -> colors.textSoft
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .heightIn(min = 44.dp)
            .clip(HadithShapes.pill)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = if (compact) 8.dp else 12.dp),
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(if (compact) 16.dp else 18.dp).rotate(iconRotation),
        )
        Spacer(Modifier.width(if (compact) 5.dp else 6.dp))
        Text(
            text = label,
            color = color,
            style = typography.secondaryAction.copy(fontSize = if (compact) 12.sp else typography.secondaryAction.fontSize),
            modifier = if (liveLabel) {
                Modifier.semantics { liveRegion = LiveRegionMode.Polite }
            } else {
                Modifier
            },
        )
    }
}

/** Tertiary: a plain text link, for navigation-away or "more" actions (Sunnah.com, Manage all
 * bookmarks, + New folder). */
@Composable
fun TertiaryLink(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: Painter? = null,
    trailingIcon: Painter? = null,
    compact: Boolean = false,
    labelFontSize: TextUnit = 14.sp,
) {
    val colors = LocalHadithColors.current
    val typography = LocalHadithTypography.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .heightIn(min = 44.dp)
            .clip(HadithShapes.pill)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = if (compact) 2.dp else 4.dp),
    ) {
        if (leadingIcon != null) {
            Icon(painter = leadingIcon, contentDescription = null, tint = colors.accentInk, modifier = Modifier.size(if (compact) 14.dp else 16.dp))
            Spacer(Modifier.width(if (compact) 3.dp else 4.dp))
        }
        Text(text = label, color = colors.accentInk, style = typography.secondaryAction.copy(fontSize = if (compact) 12.sp else labelFontSize))
        if (trailingIcon != null) {
            Spacer(Modifier.width(if (compact) 3.dp else 4.dp))
            Icon(painter = trailingIcon, contentDescription = null, tint = colors.accentInk, modifier = Modifier.size(if (compact) 12.dp else 14.dp))
        }
    }
}
