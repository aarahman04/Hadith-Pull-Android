package online.hadithpull.app.ui.share

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import online.hadithpull.app.card.CardTheme
import online.hadithpull.app.data.prefs.Settings
import online.hadithpull.app.di.AppContainer
import online.hadithpull.app.domain.Hadith
import online.hadithpull.app.domain.text.shareText
import online.hadithpull.app.share.InstagramShareResult
import online.hadithpull.app.share.SaveResult
import online.hadithpull.app.share.cardFileName
import online.hadithpull.app.share.copyCardToClipboard
import online.hadithpull.app.share.saveCardToDevice
import online.hadithpull.app.share.shareImageChooserIntent
import online.hadithpull.app.share.shareToFacebook
import online.hadithpull.app.share.shareToInstagram
import online.hadithpull.app.share.shareToWhatsApp
import online.hadithpull.app.share.writeCardToCache
import online.hadithpull.app.ui.components.HadithIcons
import online.hadithpull.app.ui.components.LocalToastState
import online.hadithpull.app.ui.components.ToastHost
import online.hadithpull.app.ui.components.ToastState
import online.hadithpull.app.ui.theme.HadithShapes
import online.hadithpull.app.ui.theme.HadithSpacing
import online.hadithpull.app.ui.theme.LocalHadithColors
import online.hadithpull.app.ui.theme.LocalHadithTypography

/** §3.3: one Share sheet, opened by both the Reader and each Bookmark item (S1). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareRoute(container: AppContainer, hadith: Hadith, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val toastState = LocalToastState.current
    val scope = rememberCoroutineScope()
    val settings by container.settingsRepository.settings.collectAsState(initial = Settings())

    val viewModel = remember(hadith) {
        ShareViewModel(context.assets, hadith, settings.arabicScript, container.settingsRepository, scope)
    }

    val theme by viewModel.theme.collectAsState()
    val includeArabic by viewModel.includeArabic.collectAsState()
    val bitmap by viewModel.bitmap.collectAsState()
    val rendering by viewModel.rendering.collectAsState()
    var imageCopied by remember { mutableStateOf(false) }

    LaunchedEffect(bitmap) { imageCopied = false }
    LaunchedEffect(imageCopied) {
        if (imageCopied) {
            delay(2000)
            imageCopied = false
        }
    }

    var pendingSaveFileName by remember { mutableStateOf<String?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val fileName = pendingSaveFileName
        pendingSaveFileName = null
        val bmp = bitmap
        if (granted && fileName != null && bmp != null) {
            scope.launch { finishSave(context, bmp, fileName, toastState) }
        } else if (!granted) {
            toastState.show("Storage permission is needed to save. Use Share instead.")
        }
    }

    LaunchedEffect(Unit) {
        viewModel.renderFailed.collect { toastState.show("Could not compose the card") }
    }

    fun fileName() = cardFileName(hadith.collectionTitle, hadith.ref)

    ShareSheet(
        bitmap = bitmap,
        rendering = rendering,
        theme = theme,
        includeArabic = includeArabic,
        imageCopied = imageCopied,
        showIncludeArabicOption = hadith.arabic.isNotEmpty(),
        onDismiss = onDismiss,
        onSetTheme = viewModel::setTheme,
        onSetIncludeArabic = viewModel::setIncludeArabic,
        onShareImage = {
            val bmp = bitmap ?: return@ShareSheet
            scope.launch {
                val uri = writeCardToCache(context, bmp, fileName())
                context.startActivity(shareImageChooserIntent(uri, shareText(hadith)))
            }
        },
        onSaveToDevice = {
            val bmp = bitmap ?: return@ShareSheet
            scope.launch {
                when (val result = saveCardToDevice(context, bmp, fileName())) {
                    SaveResult.Success -> toastState.show("Card saved")
                    SaveResult.Failure -> toastState.show("Could not save the card")
                    SaveResult.PermissionNeeded -> {
                        pendingSaveFileName = fileName()
                        permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                }
            }
        },
        onWhatsApp = {
            val bmp = bitmap ?: return@ShareSheet
            scope.launch {
                val uri = writeCardToCache(context, bmp, fileName())
                shareToWhatsApp(context, uri, shareText(hadith))
            }
        },
        onFacebook = {
            val bmp = bitmap ?: return@ShareSheet
            scope.launch {
                val uri = writeCardToCache(context, bmp, fileName())
                shareToFacebook(context, uri)
            }
        },
        onInstagram = {
            val bmp = bitmap ?: return@ShareSheet
            scope.launch {
                val name = fileName()
                val uri = writeCardToCache(context, bmp, name)
                when (shareToInstagram(context, uri)) {
                    InstagramShareResult.Sent -> Unit
                    InstagramShareResult.Unavailable -> toastState.show("Instagram unavailable. Try Share the image.")
                }
            }
        },
        onCopyImage = {
            val bmp = bitmap ?: return@ShareSheet
            scope.launch {
                copyCardToClipboard(context, bmp, fileName())
                imageCopied = true
            }
        },
    )
}

private suspend fun finishSave(context: Context, bitmap: Bitmap, fileName: String, toastState: ToastState) {
    when (saveCardToDevice(context, bitmap, fileName)) {
        SaveResult.Success -> toastState.show("Card saved")
        else -> toastState.show("Could not save the card")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShareSheet(
    bitmap: Bitmap?,
    rendering: Boolean,
    theme: CardTheme,
    includeArabic: Boolean,
    imageCopied: Boolean,
    showIncludeArabicOption: Boolean,
    onDismiss: () -> Unit,
    onSetTheme: (CardTheme) -> Unit,
    onSetIncludeArabic: (Boolean) -> Unit,
    onShareImage: () -> Unit,
    onSaveToDevice: () -> Unit,
    onWhatsApp: () -> Unit,
    onFacebook: () -> Unit,
    onInstagram: () -> Unit,
    onCopyImage: () -> Unit,
) {
    val colors = LocalHadithColors.current
    val typography = LocalHadithTypography.current
    val hasImage = bitmap != null
    val toastState = LocalToastState.current

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Box(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = 18.dp).padding(bottom = 20.dp)) {
            Text(text = "Share this Hadith", style = typography.sectionTitle.copy(fontSize = 20.sp), color = colors.text)
            Spacer(Modifier.height(4.dp))
            Text(text = "1080 × 1080, sized for an Instagram post.", color = colors.muted, fontSize = 13.6.sp)
            Spacer(Modifier.height(16.dp))

            Box(
                Modifier
                    .fillMaxWidth()
                    .widthIn(max = 280.dp)
                    .aspectRatio(1f)
                    .background(colors.surface, HadithShapes.md)
                    .border(1.dp, colors.border, HadithShapes.md)
                    .align(Alignment.CenterHorizontally),
            ) {
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                    )
                }
                if (rendering) {
                    Box(
                        Modifier.fillMaxWidth().aspectRatio(1f).background(colors.surfaceSolid),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = "Composing…", color = colors.muted, fontSize = 14.4.sp)
                    }
                }
            }
            Spacer(Modifier.height(HadithSpacing.md))

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                ThemeSegmentedControl(theme, onSetTheme)
                if (showIncludeArabicOption) {
                    Spacer(Modifier.weight(1f))
                    IncludeArabicChip(includeArabic, onSetIncludeArabic)
                }
            }
            Spacer(Modifier.height(HadithSpacing.md))

            ShareActionButton(
                label = "Share the image",
                enabled = hasImage,
                filled = true,
                onClick = onShareImage,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            ShareActionButton(
                label = "Save to device",
                enabled = hasImage,
                filled = false,
                onClick = onSaveToDevice,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Spacer(Modifier.height(HadithSpacing.sm))

            Row(
                modifier = Modifier.widthIn(max = 280.dp).fillMaxWidth().align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                shareTargets.forEach { target ->
                    ShareTargetCell(
                        target = target,
                        enabled = hasImage,
                        darkTheme = theme == CardTheme.DARK,
                        copied = target == ShareTarget.COPY_IMAGE && imageCopied,
                        onClick = when (target) {
                            ShareTarget.WHATSAPP -> onWhatsApp
                            ShareTarget.FACEBOOK -> onFacebook
                            ShareTarget.INSTAGRAM -> onInstagram
                            ShareTarget.COPY_IMAGE -> onCopyImage
                        },
                    )
                }
            }
            Spacer(Modifier.height(HadithSpacing.sm))

            Text(
                text = "The card carries the reference and hadithpull.online, so anyone you send it to can trace it back.",
                color = colors.muted,
                fontSize = 12.5.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        ToastHost(
            state = toastState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp),
        )
        }
    }
}

@Composable
private fun ShareActionButton(
    label: String,
    enabled: Boolean,
    filled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalHadithColors.current
    val typography = LocalHadithTypography.current
    val container = if (filled) colors.accent else Color.Transparent
    val content = if (filled) colors.bg else colors.accentInk

    Box(
        modifier = modifier
            .widthIn(max = 280.dp)
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(HadithShapes.pill)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 36.dp)
                .background(if (enabled) container else container.copy(alpha = 0.4f), HadithShapes.pill)
                .then(if (filled) Modifier else Modifier.border(1.dp, if (enabled) colors.borderStrong else colors.border, HadithShapes.pill))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(label, style = typography.secondaryAction, color = if (enabled) content else colors.muted)
        }
    }
}

@Composable
private fun ThemeSegmentedControl(current: CardTheme, onSelect: (CardTheme) -> Unit) {
    val colors = LocalHadithColors.current
    Row(
        modifier = Modifier
            .background(colors.bg, HadithShapes.pill)
            .border(1.dp, colors.border, HadithShapes.pill)
            .padding(3.dp),
    ) {
        CardTheme.entries.forEach { theme ->
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

@Composable
private fun IncludeArabicChip(checked: Boolean, onToggle: (Boolean) -> Unit) {
    val colors = LocalHadithColors.current
    Row(
        modifier = Modifier
            .clickable { onToggle(!checked) }
            .background(if (checked) colors.accentSoft else Color.Transparent, HadithShapes.pill)
            .border(1.dp, colors.border, HadithShapes.pill)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = "ع Include Arabic", color = if (checked) colors.accent else colors.muted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

private enum class ShareTarget(val label: String, val icon: Int) {
    WHATSAPP("WhatsApp", HadithIcons.whatsapp),
    FACEBOOK("Facebook", HadithIcons.facebook),
    INSTAGRAM("Instagram", HadithIcons.instagram),
    COPY_IMAGE("Copy image", HadithIcons.copy),
}

private val shareTargets = ShareTarget.entries.toList()

private data class BrandTint(val tintLight: Color, val fgLight: Color, val fgDark: Color)

private val brandTints = mapOf(
    ShareTarget.WHATSAPP to BrandTint(Color(0xFF25D366).copy(alpha = 0.12f), Color(0xFF1DA851), Color(0xFF4ADE80)),
    ShareTarget.FACEBOOK to BrandTint(Color(0xFF1877F2).copy(alpha = 0.12f), Color(0xFF1877F2), Color(0xFF7CB8FF)),
    ShareTarget.INSTAGRAM to BrandTint(Color(0xFFD62976).copy(alpha = 0.12f), Color(0xFFD62976), Color(0xFFF9A8D4)),
)

@Composable
private fun ShareTargetCell(target: ShareTarget, enabled: Boolean, darkTheme: Boolean, copied: Boolean, onClick: () -> Unit) {
    val colors = LocalHadithColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val tint = brandTints[target]
    val background = when {
        copied -> colors.accentSoft
        pressed && tint != null -> tint.tintLight
        else -> colors.bg
    }
    val foreground = if (pressed && tint != null) (if (darkTheme) tint.fgDark else tint.fgLight) else colors.textSoft

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClickLabel = if (copied) "Image copied" else target.label,
                onClick = onClick,
            )
            .then(if (target == ShareTarget.COPY_IMAGE) Modifier.semantics { liveRegion = LiveRegionMode.Polite } else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(background, CircleShape)
                .border(1.dp, if (copied) colors.accent.copy(alpha = 0.35f) else colors.border, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(if (copied) HadithIcons.check else target.icon),
                contentDescription = if (copied) "Image copied" else target.label,
                tint = when {
                    !enabled -> colors.muted.copy(alpha = 0.5f)
                    copied -> colors.accentInk
                    else -> foreground
                },
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
