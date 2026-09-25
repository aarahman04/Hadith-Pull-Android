package online.hadithpull.app.ui.reader

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import online.hadithpull.app.data.prefs.ArabicScript
import online.hadithpull.app.data.prefs.TextSize
import online.hadithpull.app.domain.Hadith
import online.hadithpull.app.domain.DRAW_FAILURE_MESSAGE
import online.hadithpull.app.domain.text.buildExcerpt
import online.hadithpull.app.domain.text.paragraphize
import online.hadithpull.app.domain.text.hasArabicWorthShowing
import online.hadithpull.app.domain.text.wordCount
import online.hadithpull.app.domain.text.PAGE_EXCERPT
import online.hadithpull.app.ui.components.ActionTone
import online.hadithpull.app.ui.components.HadithCard
import online.hadithpull.app.ui.components.HadithIcons
import online.hadithpull.app.ui.components.HadithPrimaryButton
import online.hadithpull.app.ui.components.ReferenceSummary
import online.hadithpull.app.ui.components.ScreenHero
import online.hadithpull.app.ui.components.SecondaryAction
import online.hadithpull.app.ui.components.StatusPill
import online.hadithpull.app.ui.components.shimmerColor
import online.hadithpull.app.ui.theme.HadithColors
import online.hadithpull.app.ui.theme.HadithShapes
import online.hadithpull.app.ui.theme.HadithSpacing
import online.hadithpull.app.ui.theme.HadithTypography
import online.hadithpull.app.ui.theme.LocalHadithColors
import online.hadithpull.app.ui.theme.LocalHadithTypography

/** R0.3/R1.4: the fixed dock (reference + Sunnah link + New Hadith + quiet actions) needs at
 * least this much room below the scrolling content before it takes over the whole screen. */
private val DOCKED_MODE_MIN_HEIGHT = 480.dp

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
    onOpenSunnah: (String) -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val docked = maxHeight >= DOCKED_MODE_MIN_HEIGHT
        val listState = rememberLazyListState()
        val scope = rememberCoroutineScope()

        // R0.3: scroll to top on every draw after the first one in this process.
        var hasShownAResult by remember { mutableStateOf(false) }
        LaunchedEffect(uiState) {
            if (uiState !is ReaderUiState.Loading) {
                hasShownAResult = true
            } else if (hasShownAResult) {
                listState.scrollToItem(0)
            }
        }

        // R0.3: expand/collapse scrolls so the sticky reading bar pins and the narration starts
        // directly under it, replacing the old bringIntoView behaviour.
        val onToggleExpandWithScroll: () -> Unit = {
            onToggleExpand()
            scope.launch {
                withFrameNanos { }
                listState.animateScrollToItem(1)
            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (docked) {
                Box(Modifier.weight(1f).widthIn(max = 780.dp).fillMaxWidth()) {
                    ReaderContentList(
                        listState = listState,
                        uiState = uiState,
                        darkTheme = darkTheme,
                        arabicScript = arabicScript,
                        textSize = textSize,
                        onSetArabicScript = onSetArabicScript,
                        onCycleTextSize = onCycleTextSize,
                        onToggleExpand = onToggleExpandWithScroll,
                    )
                }
                ReaderDock(
                    uiState = uiState,
                    darkTheme = darkTheme,
                    isSaved = isSaved,
                    onDraw = onDraw,
                    onCopy = onCopy,
                    onOpenSave = onOpenSave,
                    onOpenShare = onOpenShare,
                    onOpenSunnah = onOpenSunnah,
                )
            } else {
                Box(Modifier.weight(1f).widthIn(max = 780.dp).fillMaxWidth()) {
                    ReaderContentList(
                        listState = listState,
                        uiState = uiState,
                        darkTheme = darkTheme,
                        arabicScript = arabicScript,
                        textSize = textSize,
                        onSetArabicScript = onSetArabicScript,
                        onCycleTextSize = onCycleTextSize,
                        onToggleExpand = onToggleExpandWithScroll,
                        trailingDockContent = {
                            ReaderDockContent(
                                uiState = uiState,
                                darkTheme = darkTheme,
                                isSaved = isSaved,
                                onDraw = onDraw,
                                onCopy = onCopy,
                                onOpenSave = onOpenSave,
                                onOpenShare = onOpenShare,
                                onOpenSunnah = onOpenSunnah,
                            )
                        },
                    )
                }
            }
        }
    }
}

/** R1.4: the ONLY scrolling region -- hero, a sticky reading bar, the narration, and (docked
 * mode only) the attribution line. In stacked mode `trailingDockContent` appends the dock's own
 * content as an ordinary last item instead of a fixed sibling. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ReaderContentList(
    listState: LazyListState,
    uiState: ReaderUiState,
    darkTheme: Boolean,
    arabicScript: ArabicScript,
    textSize: TextSize,
    onSetArabicScript: (ArabicScript) -> Unit,
    onCycleTextSize: () -> Unit,
    onToggleExpand: () -> Unit,
    trailingDockContent: (@Composable () -> Unit)? = null,
) {
    val colors = LocalHadithColors.current
    val windowWidthDp = LocalConfiguration.current.screenWidthDp
    val sidePadding = if (windowWidthDp < 720) 16.dp else 20.dp
    val showScriptToggle = (uiState as? ReaderUiState.Loaded)
        ?.let { it.expanded && hasArabicWorthShowing(it.hadith.arabic) } == true
    // Keep the sticky reading tool on one stable surface. Changing its opacity when the list
    // scrolls makes the control appear to flicker or reactivate even when it was not touched.
    val barBackground = colors.bg.copy(alpha = 0.96f)

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().padding(horizontal = sidePadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item("hero") {
            Spacer(Modifier.height(HadithSpacing.xl))
            ScreenHero(
                eyebrow = "HADITH OF THE MOMENT",
                title = "Read. Reflect. Remember.",
                titleStyle = LocalHadithTypography.current.panelTitle.copy(fontSize = 22.sp),
                subline = null,
            )
            Spacer(Modifier.height(16.dp))
        }
        stickyHeader("bar") {
            Box(Modifier.fillMaxWidth().background(barBackground).padding(vertical = 8.dp)) {
                ReadingBar(showScriptToggle, arabicScript, textSize, onSetArabicScript, onCycleTextSize)
            }
        }
        item("narration") {
            Spacer(Modifier.height(HadithSpacing.xl))
            NarrationBlock(uiState, darkTheme, windowWidthDp, onToggleExpand)
            Spacer(Modifier.height(HadithSpacing.xxl))
        }
        if (trailingDockContent != null) {
            item("dock") { trailingDockContent() }
        }
    }
}

@Composable
private fun ReadingBar(
    showScriptToggle: Boolean,
    arabicScript: ArabicScript,
    textSize: TextSize,
    onSetArabicScript: (ArabicScript) -> Unit,
    onCycleTextSize: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        ReadingModeControl(showScriptToggle, arabicScript, textSize, onSetArabicScript, onCycleTextSize)
    }
}

/** Round 3 §Step 29.3: the reading-mode control reads as one segmented pill (script + text size),
 * not two separately styled chips. */
@Composable
private fun ReadingModeControl(
    showScriptToggle: Boolean,
    arabicScript: ArabicScript,
    textSize: TextSize,
    onSetArabicScript: (ArabicScript) -> Unit,
    onCycleTextSize: () -> Unit,
) {
    val colors = LocalHadithColors.current
    val textSizeInteractionSource = remember { MutableInteractionSource() }
    val textSizePressed by textSizeInteractionSource.collectIsPressedAsState()
    val textSizePressScale by animateFloatAsState(
        targetValue = if (textSizePressed) 0.98f else 1f,
        animationSpec = tween(120),
        label = "textSizePressScale",
    )
    Row(
        modifier = Modifier
            .height(40.dp)
            .background(colors.surfaceSolid, HadithShapes.pill)
            .border(1.dp, colors.border, HadithShapes.pill)
            .padding(3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showScriptToggle) {
            ArabicScript.entries.forEach { script ->
                val selected = script == arabicScript
                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .clip(HadithShapes.pill)
                        .background(if (selected) colors.accentSoft else Color.Transparent)
                        .clickable { onSetArabicScript(script) }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = script.name.lowercase().replaceFirstChar { it.uppercase() },
                        color = if (selected) colors.accentInk else colors.muted,
                        fontSize = 14.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    )
                }
            }
            Box(Modifier.padding(horizontal = 4.dp).width(1.dp).height(20.dp).background(colors.borderStrong))
        }

        val typography = LocalHadithTypography.current
        val label = textSize.name.lowercase().replaceFirstChar { it.uppercase() }
        Row(
            modifier = Modifier
                .height(34.dp)
                .clip(HadithShapes.pill)
                // Size has no persistent fill: only the interaction source drives a brief
                // press scale, so recomposition and scrolling cannot leave a highlight behind.
                .graphicsLayer {
                    scaleX = textSizePressScale
                    scaleY = textSizePressScale
                }
                .clickable(
                    interactionSource = textSizeInteractionSource,
                    indication = null,
                    onClickLabel = "Change text size",
                    onClick = onCycleTextSize,
                )
                .padding(horizontal = 12.dp)
                .semantics { contentDescription = "Text size: $label" },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "Aa", fontFamily = typography.heroTitle.fontFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = colors.text)
            if (!showScriptToggle) {
                Text(text = " $label", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.muted)
            }
        }
    }
}

@Composable
private fun NarrationBlock(
    uiState: ReaderUiState,
    darkTheme: Boolean,
    windowWidthDp: Int,
    onToggleExpand: () -> Unit,
) {
    Box(Modifier.fillMaxWidth()) {
        AnimatedContent(
            targetState = uiState,
            transitionSpec = {
                val initialKey = (initialState as? ReaderUiState.Loaded)?.hadith?.key
                val targetKey = (targetState as? ReaderUiState.Loaded)?.hadith?.key
                val hadithChanged = initialKey != targetKey
                if (targetState is ReaderUiState.Loaded && hadithChanged) {
                    (
                        fadeIn(tween(900, easing = FastOutSlowInEasing)) +
                            expandVertically(
                                animationSpec = tween(900, easing = FastOutSlowInEasing),
                                expandFrom = Alignment.Top,
                            ) +
                            slideInVertically(
                                animationSpec = tween(900, easing = FastOutSlowInEasing),
                                initialOffsetY = { -8 },
                            )
                        ) togetherWith fadeOut(tween(240))
                } else if (targetState !is ReaderUiState.Loaded) {
                    fadeIn(tween(140)) togetherWith fadeOut(tween(180))
                } else {
                    EnterTransition.None togetherWith ExitTransition.None
                }
            },
            label = "hadithContentTransition",
        ) { state ->
            when (state) {
                is ReaderUiState.Loaded ->
                    LoadedNarration(state, darkTheme, windowWidthDp, onToggleExpand)
                is ReaderUiState.Loading -> SkeletonBlock()
                is ReaderUiState.Failure -> FailureBlock()
            }
        }
    }
}

@Composable
private fun SkeletonBlock() {
    val barColor = shimmerColor()
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
private fun LoadedNarration(
    state: ReaderUiState.Loaded,
    darkTheme: Boolean,
    windowWidthDp: Int,
    onToggleExpand: () -> Unit,
) {
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

/** R1.4: the reference block and Sunnah link no longer render here -- they moved into the
 * ReaderDock. This composable now stops after the expand toggle, with FullReference still shown
 * inline (in the scrolling list) when expanded, per R0.3. */
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
            Text(text = hadith.narrator, style = typography.contentMetaItalic, color = colors.muted)
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

        if (state.expanded) {
            Spacer(Modifier.height(24.dp))
            FullReference(hadith, darkTheme)
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
private fun FullReference(hadith: Hadith, darkTheme: Boolean) {
    val colors = LocalHadithColors.current
    val typography = LocalHadithTypography.current
    val windowWidthDp = LocalConfiguration.current.screenWidthDp
    Column(
        Modifier
            .fillMaxWidth()
            .background(colors.surfaceSolid, HadithShapes.md)
            .border(1.dp, colors.border, HadithShapes.md)
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "REFERENCE", style = typography.sectionLabel, color = colors.accentInk)
            StatusPill(primary = hadith.primary, darkTheme = darkTheme)
        }
        Spacer(Modifier.height(14.dp))
        if (windowWidthDp >= 400) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ReferenceField("COLLECTION", hadith.collectionTitle, Modifier.weight(1f))
                Spacer(Modifier.width(16.dp))
                ReferenceField("HADITH NUMBER", hadith.ref, Modifier.weight(1f))
            }
        } else {
            ReferenceField("COLLECTION", hadith.collectionTitle, Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            ReferenceField("HADITH NUMBER", hadith.ref, Modifier.fillMaxWidth())
        }
        if (hadith.chapter.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Column(Modifier.fillMaxWidth()) {
                Text(text = "CHAPTER", style = typography.label, color = colors.muted)
                Text(text = hadith.chapter, style = typography.contentMeta, color = colors.text)
            }
        }
        if (hadith.book != null && hadith.inBook != null) {
            Spacer(Modifier.height(12.dp))
            ReferenceField("IN-BOOK", "Book ${hadith.book}, Hadith ${hadith.inBook}", Modifier.fillMaxWidth())
        }
        val grades = hadith.grades
        val consensus = hadith.primary?.consensus == true
        if (consensus || grades.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Column(Modifier.fillMaxWidth()) {
                Text(text = "GRADES", style = typography.label, color = colors.muted)
                Spacer(Modifier.height(4.dp))
                if (consensus) {
                    Text(
                        text = "Accepted as sahih by scholarly consensus",
                        style = typography.contentMeta,
                        color = colors.text,
                    )
                } else {
                    grades.forEach { grade ->
                        Text(
                            text = "${grade.by}: ${grade.grade}",
                            style = typography.contentMeta,
                            color = colors.text,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReferenceField(label: String, value: String, modifier: Modifier = Modifier) {
    val colors = LocalHadithColors.current
    val typography = LocalHadithTypography.current
    Column(modifier) {
        Text(text = label, style = typography.label, color = colors.muted)
        Text(text = value, style = typography.contentMeta, color = colors.text)
    }
}

@Composable
private fun FailureBlock() {
    val colors = LocalHadithColors.current
    Text(text = DRAW_FAILURE_MESSAGE, color = colors.error, fontSize = 16.sp, textAlign = TextAlign.Center)
}

/** R1.4: the fixed sibling in docked mode. Never scrolls; its two top rows never change height
 * across Loading/Loaded/Failure (R0.3's "dock height stability"). */
@Composable
private fun ReaderDock(
    uiState: ReaderUiState,
    darkTheme: Boolean,
    isSaved: Boolean,
    onDraw: () -> Unit,
    onCopy: (Hadith) -> Unit,
    onOpenSave: (Hadith) -> Unit,
    onOpenShare: (Hadith) -> Unit,
    onOpenSunnah: (String) -> Unit,
) {
    val colors = LocalHadithColors.current
    Column(Modifier.fillMaxWidth().widthIn(max = 780.dp)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
        ReaderDockContent(uiState, darkTheme, isSaved, onDraw, onCopy, onOpenSave, onOpenShare, onOpenSunnah)
    }
}

@Composable
private fun ReaderDockContent(
    uiState: ReaderUiState,
    darkTheme: Boolean,
    isSaved: Boolean,
    onDraw: () -> Unit,
    onCopy: (Hadith) -> Unit,
    onOpenSave: (Hadith) -> Unit,
    onOpenShare: (Hadith) -> Unit,
    onOpenSunnah: (String) -> Unit,
) {
    val loaded = uiState as? ReaderUiState.Loaded
    val hadith = loaded?.hadith
    val sidePadding = if (LocalConfiguration.current.screenWidthDp < 720) 16.dp else 20.dp

    Column(Modifier.fillMaxWidth().padding(horizontal = sidePadding).padding(top = 12.dp, bottom = 8.dp)) {
        HadithCard(contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 0.dp)) {
            ReferenceSummary(hadith = hadith, loading = uiState is ReaderUiState.Loading, darkTheme = darkTheme, onOpenSunnah = onOpenSunnah)
        }
        Spacer(Modifier.height(12.dp))
        PrimaryButton(uiState = uiState, onDraw = onDraw)
        Spacer(Modifier.height(4.dp))
        QuietActionsRow(uiState = uiState, isSaved = isSaved, onCopy = onCopy, onOpenSave = onOpenSave, onOpenShare = onOpenShare)
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
    HadithPrimaryButton(
        text = label,
        onClick = onDraw,
        enabled = !loading,
        compact = true,
        leadingIcon = {
            if (loading) {
                LoadingSpinner(color = colors.bg)
            } else {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            }
        },
    )
}

@Composable
private fun LoadingSpinner(color: Color) {
    val rotation = rememberInfiniteTransition(label = "newHadithSpinner").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing)),
        label = "newHadithSpinnerRotation",
    ).value
    Canvas(Modifier.size(16.dp)) {
        drawCircle(
            color = color.copy(alpha = 0.28f),
            style = Stroke(width = 2.dp.toPx()),
        )
        drawArc(
            color = color,
            startAngle = rotation - 90f,
            sweepAngle = 105f,
            useCenter = false,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
        )
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
    val hadith = (uiState as? ReaderUiState.Loaded)?.hadith
    val enabled = hadith != null
    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(hadith?.key) { copied = false }
    LaunchedEffect(copied) {
        if (copied) {
            delay(2000)
            copied = false
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SecondaryAction(
            label = if (copied) "Copied" else "Copy",
            icon = painterResource(if (copied) HadithIcons.check else HadithIcons.copy),
            onClick = { hadith?.let { onCopy(it); copied = true } },
            enabled = enabled,
            tone = if (copied) ActionTone.Accent else ActionTone.Neutral,
            liveLabel = true,
            compact = true,
        )
        SecondaryAction(
            label = if (isSaved) "Saved" else "Save",
            icon = painterResource(if (isSaved) HadithIcons.bookmarkFilled else HadithIcons.bookmark),
            onClick = { hadith?.let(onOpenSave) },
            enabled = enabled,
            tone = if (isSaved) ActionTone.Accent else ActionTone.Neutral,
            compact = true,
        )
        SecondaryAction(
            label = "Share",
            icon = painterResource(HadithIcons.upload),
            onClick = { hadith?.let(onOpenShare) },
            enabled = enabled,
            compact = true,
        )
    }
}
