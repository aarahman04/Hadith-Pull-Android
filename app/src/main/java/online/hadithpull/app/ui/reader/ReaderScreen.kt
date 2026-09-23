package online.hadithpull.app.ui.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import online.hadithpull.app.data.prefs.ArabicScript
import online.hadithpull.app.data.prefs.TextSize
import online.hadithpull.app.domain.Hadith
import online.hadithpull.app.domain.DrawResult
import online.hadithpull.app.domain.text.buildExcerpt
import online.hadithpull.app.domain.text.paragraphize
import online.hadithpull.app.domain.text.hasArabicWorthShowing
import online.hadithpull.app.domain.text.wordCount
import online.hadithpull.app.domain.text.PAGE_EXCERPT
import online.hadithpull.app.ui.components.StatusPill
import online.hadithpull.app.ui.theme.HadithColors
import online.hadithpull.app.ui.theme.HadithShapes
import online.hadithpull.app.ui.theme.HadithTypography
import online.hadithpull.app.ui.theme.LocalHadithColors
import online.hadithpull.app.ui.theme.LocalHadithTypography

@Composable
fun ReaderScreen(
    uiState: ReaderUiState,
    darkTheme: Boolean,
    arabicScript: ArabicScript,
    textSize: TextSize,
    onDraw: () -> Unit,
    onToggleExpand: () -> Unit,
    onSetArabicScript: (ArabicScript) -> Unit,
    onCycleTextSize: () -> Unit,
    isSaved: Boolean,
    onCopy: (Hadith) -> Unit,
    onOpenSave: (Hadith) -> Unit,
    onOpenShare: (Hadith) -> Unit,
    onOpenAttribution: () -> Unit,
) {
    val colors = LocalHadithColors.current
    val windowWidthDp = LocalConfiguration.current.screenWidthDp
    val sidePadding = if (windowWidthDp < 720) 16.dp else 20.dp
    val scrollState = rememberScrollState()
    val narrationBringIntoView = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()

    // §2.2: after any draw except the first in this process, animate the scroll to 0.
    var hasShownAResult by remember { mutableStateOf(false) }
    LaunchedEffect(uiState) {
        if (uiState !is ReaderUiState.Loading) {
            if (hasShownAResult) scrollState.animateScrollTo(0)
            hasShownAResult = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = sidePadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(24.dp))
        Box(Modifier.widthIn(max = 780.dp).fillMaxWidth()) {
            Column {
                Hero(windowWidthDp)
                Spacer(Modifier.height(20.dp))
                ReadingBar(
                    windowWidthDp = windowWidthDp,
                    showScriptToggle = (uiState as? ReaderUiState.Loaded)?.hadith?.arabic?.isNotEmpty() == true,
                    arabicScript = arabicScript,
                    textSize = textSize,
                    onSetArabicScript = onSetArabicScript,
                    onCycleTextSize = onCycleTextSize,
                )
                Spacer(Modifier.height(24.dp))
                NarrationBlock(
                    uiState = uiState,
                    darkTheme = darkTheme,
                    windowWidthDp = windowWidthDp,
                    modifier = Modifier.bringIntoViewRequester(narrationBringIntoView),
                    onToggleExpand = {
                        onToggleExpand()
                        // §2.2: if the narration block's top is above the viewport, scroll it into view.
                        scope.launch { narrationBringIntoView.bringIntoView() }
                    },
                )
                Spacer(Modifier.height(20.dp))
                PrimaryButton(uiState = uiState, onDraw = onDraw)
                Spacer(Modifier.height(16.dp))
                QuietActionsRow(uiState = uiState, isSaved = isSaved, onCopy = onCopy, onOpenSave = onOpenSave, onOpenShare = onOpenShare)
                Spacer(Modifier.height(20.dp))
                AttributionLine(onClick = onOpenAttribution)
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun Hero(windowWidthDp: Int) {
    val colors = LocalHadithColors.current
    val typography = LocalHadithTypography.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(28.dp).height(1.dp).background(colors.borderStrong))
            Text(
                text = "HADITH OF THE MOMENT",
                style = typography.eyebrow,
                color = colors.muted,
                modifier = Modifier.padding(horizontal = 10.dp),
            )
            Box(Modifier.width(28.dp).height(1.dp).background(colors.borderStrong))
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Read. Reflect. Remember.",
            style = typography.heroTitle,
            color = colors.text,
            textAlign = TextAlign.Center,
        )
        if (windowWidthDp >= 720) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = "A random narration, with its reference — so you can always verify the source.",
                style = typography.body,
                color = colors.textSoft,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ReadingBar(
    windowWidthDp: Int,
    showScriptToggle: Boolean,
    arabicScript: ArabicScript,
    textSize: TextSize,
    onSetArabicScript: (ArabicScript) -> Unit,
    onCycleTextSize: () -> Unit,
) {
    val arrangement = if (windowWidthDp < 720) Arrangement.Center else Arrangement.End
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = arrangement) {
        if (showScriptToggle) {
            ScriptToggle(arabicScript, onSetArabicScript)
            Spacer(Modifier.width(10.dp))
        }
        TextSizeChip(textSize, onCycleTextSize)
    }
}

@Composable
private fun ScriptToggle(current: ArabicScript, onSelect: (ArabicScript) -> Unit) {
    val colors = LocalHadithColors.current
    Row(
        modifier = Modifier
            .background(colors.surface, HadithShapes.pill)
            .border(1.dp, colors.border, HadithShapes.pill)
            .padding(3.dp),
    ) {
        ArabicScript.entries.forEach { script ->
            val selected = script == current
            Box(
                modifier = Modifier
                    .clickable { onSelect(script) }
                    .background(if (selected) colors.accentSoft else Color.Transparent, HadithShapes.pill)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(
                    text = script.name.lowercase().replaceFirstChar { it.uppercase() },
                    color = if (selected) colors.text else colors.muted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun TextSizeChip(textSize: TextSize, onCycle: () -> Unit) {
    val colors = LocalHadithColors.current
    val pressed = textSize != TextSize.COMFORTABLE
    Row(
        modifier = Modifier
            .clickable(onClick = onCycle)
            .background(if (pressed) colors.accentSoft else Color.Transparent, HadithShapes.pill)
            .border(1.dp, colors.border, HadithShapes.pill)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = "Aa", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = colors.text)
        Text(
            text = " " + textSize.name.lowercase().replaceFirstChar { it.uppercase() },
            fontSize = 13.sp,
            color = colors.muted,
        )
    }
}

@Composable
private fun NarrationBlock(
    uiState: ReaderUiState,
    darkTheme: Boolean,
    windowWidthDp: Int,
    onToggleExpand: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        when (uiState) {
            is ReaderUiState.Loading -> SkeletonBlock()
            is ReaderUiState.Loaded -> LoadedNarration(uiState, darkTheme, windowWidthDp, onToggleExpand)
            is ReaderUiState.Failure -> FailureBlock(uiState.cause)
        }
    }
}

@Composable
private fun SkeletonBlock() {
    val colors = LocalHadithColors.current
    val transition = rememberInfiniteTransition(label = "skeleton")
    val shimmer by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "shimmer",
    )
    val barColor = lerp(colors.border, colors.borderStrong, shimmer)
    val widths = listOf(1f, 0.92f, 0.97f, 0.68f)
    Column(Modifier.fillMaxWidth()) {
        widths.forEachIndexed { index, w ->
            if (index > 0) Spacer(Modifier.height(15.dp))
            Box(
                Modifier
                    .fillMaxWidth(w)
                    .height(17.dp)
                    .background(barColor, RoundedCornerShape(8.dp)),
            )
        }
    }
}

@Composable
private fun LoadedNarration(state: ReaderUiState.Loaded, darkTheme: Boolean, windowWidthDp: Int, onToggleExpand: () -> Unit) {
    val colors = LocalHadithColors.current
    val typography = LocalHadithTypography.current
    val hadith = state.hadith
    val pageExcerpt = remember(hadith.english) { buildExcerpt(hadith.english, PAGE_EXCERPT) }
    val hasArabic = hasArabicWorthShowing(hadith.arabic)

    Box(Modifier.fillMaxWidth()) {
        QuoteMark(expanded = state.expanded, windowWidthDp = windowWidthDp)
        LoadedNarrationContent(state, hadith, pageExcerpt, hasArabic, colors, typography, darkTheme, onToggleExpand)
    }
}

/** §2.2: the decorative "”" behind the text, gold @16% alpha, fading to 0 over 400ms when expanded. */
@Composable
private fun QuoteMark(expanded: Boolean, windowWidthDp: Int) {
    val colors = LocalHadithColors.current
    val typography = LocalHadithTypography.current
    val targetAlpha = if (expanded) 0f else 0.16f
    val alpha by animateFloatAsState(targetValue = targetAlpha, animationSpec = tween(400), label = "quoteMarkAlpha")
    val sizeSp = (0.12f * windowWidthDp).coerceIn(72f, 112f)
    Text(
        text = "”",
        fontFamily = typography.heroTitle.fontFamily,
        color = colors.gold.copy(alpha = alpha),
        fontSize = sizeSp.sp,
        modifier = Modifier.offset(x = (-10).dp, y = (-8).dp),
    )
}

@Composable
private fun LoadedNarrationContent(
    state: ReaderUiState.Loaded,
    hadith: Hadith,
    pageExcerpt: String?,
    hasArabic: Boolean,
    colors: HadithColors,
    typography: HadithTypography,
    darkTheme: Boolean,
    onToggleExpand: () -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        if (state.fallbackNote != null) {
            Text(text = state.fallbackNote, style = typography.body, color = colors.muted, fontSize = 13.6.sp)
            Spacer(Modifier.height(10.dp))
        }

        AnimatedVisibility(visible = state.expanded && hasArabic, enter = expandVertically() + fadeIn(tween(500))) {
            Column {
                Text(text = "ARABIC", style = typography.sectionLabel, color = colors.muted)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = hadith.arabic,
                    style = typography.arabic.copy(textAlign = TextAlign.Right, textDirection = TextDirection.Rtl),
                    color = colors.text,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 26.dp),
                )
                Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
                Spacer(Modifier.height(27.dp))
            }
        }

        val englishParagraphs = if (!state.expanded && pageExcerpt != null) {
            listOf(pageExcerpt)
        } else {
            paragraphize(hadith.english)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(tween(450)),
        ) {
            englishParagraphs.forEachIndexed { index, paragraph ->
                if (index > 0) Spacer(Modifier.height(18.dp))
                Text(text = paragraph, style = typography.english, color = colors.text)
            }
        }

        if (hadith.narrator.isNotEmpty()) {
            Spacer(Modifier.height(22.dp))
            Text(text = hadith.narrator, style = typography.narrator, color = colors.muted, fontStyle = FontStyle.Italic)
        }

        val showExpandButton = pageExcerpt != null || hasArabic
        if (showExpandButton) {
            Spacer(Modifier.height(18.dp))
            ExpandToggle(
                expanded = state.expanded,
                hasExcerpt = pageExcerpt != null,
                hasArabic = hasArabic,
                wordCount = wordCount(hadith.english),
                onToggle = onToggleExpand,
            )
        }

        Spacer(Modifier.height(24.dp))
        if (state.expanded) {
            FullReference(hadith, darkTheme)
        } else {
            BriefReference(hadith, darkTheme)
        }
    }
}

@Composable
private fun ExpandToggle(expanded: Boolean, hasExcerpt: Boolean, hasArabic: Boolean, wordCount: Int, onToggle: () -> Unit) {
    val colors = LocalHadithColors.current
    val rotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f, animationSpec = tween(350), label = "chevron")
    Row(
        modifier = Modifier
            .clickable(onClick = onToggle)
            .background(colors.surfaceSolid, HadithShapes.pill)
            .border(1.dp, colors.borderStrong, HadithShapes.pill)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val label = when {
            expanded -> "Show less"
            hasExcerpt -> "Show full Hadith"
            else -> "Show Arabic"
        }
        Text(text = label, color = colors.textSoft, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        if (!expanded && hasExcerpt) {
            Text(text = " · $wordCount words", color = colors.muted, fontSize = 14.sp)
            if (hasArabic) Text(text = " · Arabic", color = colors.muted, fontSize = 14.sp)
        }
        Spacer(Modifier.width(6.dp))
        Icon(
            imageVector = Icons.Filled.KeyboardArrowDown,
            contentDescription = null,
            tint = colors.textSoft,
            modifier = Modifier.rotate(rotation).size(16.dp),
        )
    }
}

@Composable
private fun BriefReference(hadith: Hadith, darkTheme: Boolean) {
    val colors = LocalHadithColors.current
    Column(
        Modifier
            .fillMaxWidth()
            .border(0.dp, colors.border)
            .padding(top = 18.dp),
    ) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
        Spacer(Modifier.height(18.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column {
                Text(
                    text = "${hadith.book}  ·  Hadith ${hadith.number}",
                    color = colors.text,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                )
                if (hadith.chapter.isNotEmpty()) {
                    Text(text = hadith.chapter, color = colors.muted, fontSize = 14.sp)
                }
            }
            StatusPill(status = hadith.status, darkTheme = darkTheme)
        }
    }
}

@Composable
private fun FullReference(hadith: Hadith, darkTheme: Boolean) {
    val colors = LocalHadithColors.current
    val windowWidthDp = LocalConfiguration.current.screenWidthDp
    Column(
        Modifier
            .fillMaxWidth()
            .background(colors.refBg, HadithShapes.md)
            .border(1.dp, colors.border, HadithShapes.md)
            .padding(vertical = 18.dp, horizontal = 20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "REFERENCE", color = colors.accentInk, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
            StatusPill(status = hadith.status, darkTheme = darkTheme)
        }
        Spacer(Modifier.height(14.dp))
        if (windowWidthDp >= 400) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ReferenceField("COLLECTION", hadith.book, Modifier.weight(1f))
                Spacer(Modifier.width(16.dp))
                ReferenceField("HADITH NUMBER", hadith.number, Modifier.weight(1f))
            }
        } else {
            ReferenceField("COLLECTION", hadith.book, Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            ReferenceField("HADITH NUMBER", hadith.number, Modifier.fillMaxWidth())
        }
        if (hadith.chapter.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Column(Modifier.fillMaxWidth()) {
                Text(text = "CHAPTER", color = colors.muted, fontSize = 11.8.sp, fontWeight = FontWeight.SemiBold)
                Text(text = hadith.chapter, color = colors.text, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun ReferenceField(label: String, value: String, modifier: Modifier = Modifier) {
    val colors = LocalHadithColors.current
    Column(modifier) {
        Text(text = label, color = colors.muted, fontSize = 11.8.sp, fontWeight = FontWeight.SemiBold)
        Text(text = value, color = colors.text, fontSize = 17.3.sp, fontWeight = FontWeight.SemiBold, lineHeight = 24.sp)
    }
}

@Composable
private fun FailureBlock(cause: DrawResult.Failure) {
    val colors = LocalHadithColors.current
    val typography = LocalHadithTypography.current
    val message = when (cause) {
        DrawResult.Failure.KeyRejected -> "The Hadith service rejected this request. The API key may need renewing."
        DrawResult.Failure.Network -> "Could not reach the Hadith service. Check your connection and try again."
        DrawResult.Failure.Busy -> "The Hadith service is busy. Please try again in a moment."
        DrawResult.Failure.Exhausted -> "Could not find a narration just now. Please try again."
    }
    if (cause is DrawResult.Failure.Exhausted) {
        Text(text = message, style = typography.placeholder, color = colors.muted, textAlign = TextAlign.Center)
    } else {
        Text(text = message, color = colors.error, fontSize = 16.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun PrimaryButton(uiState: ReaderUiState, onDraw: () -> Unit) {
    val colors = LocalHadithColors.current
    val loading = uiState is ReaderUiState.Loading
    val label = when (uiState) {
        is ReaderUiState.Loading -> "Seeking…"
        is ReaderUiState.Loaded -> "New Hadith"
        is ReaderUiState.Failure -> "Try again"
    }
    val rotation = rememberInfiniteTransition(label = "spin").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
        label = "spinAngle",
    ).value

    Button(
        onClick = onDraw,
        enabled = !loading,
        shape = HadithShapes.pill,
        colors = ButtonDefaults.buttonColors(containerColor = colors.text, contentColor = colors.bg, disabledContainerColor = colors.text.copy(alpha = 0.55f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(
            imageVector = Icons.Filled.Refresh,
            contentDescription = null,
            modifier = Modifier
                .size(17.dp)
                .rotate(if (loading) rotation else 0f),
        )
        Spacer(Modifier.width(8.dp))
        Text(text = label, fontWeight = FontWeight.Medium, fontSize = 15.sp)
    }
}

@Composable
private fun QuietActionsRow(
    uiState: ReaderUiState,
    isSaved: Boolean,
    onCopy: (Hadith) -> Unit,
    onOpenSave: (Hadith) -> Unit,
    onOpenShare: (Hadith) -> Unit,
) {
    val colors = LocalHadithColors.current
    val hadith = (uiState as? ReaderUiState.Loaded)?.hadith
    val enabled = hadith != null
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
        QuietAction("Copy", enabled) { hadith?.let(onCopy) }
        Divider14(colors.borderStrong)
        QuietAction(if (isSaved) "Saved" else "Save", enabled, accent = isSaved) { hadith?.let(onOpenSave) }
        Divider14(colors.borderStrong)
        QuietAction("Share", enabled) { hadith?.let(onOpenShare) }
    }
}

@Composable
private fun Divider14(color: Color) {
    Box(Modifier.width(1.dp).height(14.dp).background(color))
}

@Composable
private fun QuietAction(label: String, enabled: Boolean, accent: Boolean = false, onClick: () -> Unit) {
    val colors = LocalHadithColors.current
    val color = when {
        !enabled -> colors.muted.copy(alpha = 0.45f)
        accent -> colors.accent
        else -> colors.muted
    }
    Text(
        text = label,
        color = color,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        modifier = Modifier.clickable(enabled = enabled, onClick = onClick),
    )
}

@Composable
private fun AttributionLine(onClick: () -> Unit) {
    val colors = LocalHadithColors.current
    Text(
        text = "Texts via HadithAPI",
        color = colors.muted,
        fontSize = 13.4.sp,
        modifier = Modifier.clickable(onClick = onClick),
    )
}
