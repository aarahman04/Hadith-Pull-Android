# Build progress

Spec: `C:\Users\aarah\.claude\plans\pasted-content-id-a360-role-you-shimmying-naur.md`
(read that file in full before touching this project — it is the only source of
truth for what to build; this file is just a progress log, not a spec).

Steps 1–13 below were built from the verbatim §5.4 handoff prompt (v1,
hadithapi.com live-API based). That plan is now superseded: the spec's
"Data source: bundled offline dataset" section (LOCKED 2026-09-24) replaces
the live sunnah.com/hadithapi.com API entirely with a static dataset bundled
into the app — no network calls anywhere. Steps 14–17 (data layer, UI,
card/release, polish) are specced in that section's own Sonnet handoff
prompt and pick up from Step 13's commit. Same protocol continues: stop for
the user's "go" after each step, commit on `main` after approval, never push.

## Status: Round 2 (Steps 18-26) complete and committed. `assembleDebug`,
`testDebugUnitTest` (105 JVM tests), `assembleRelease` and `lintDebug` all
green; the signed release build was smoke-tested on `emulator-5554` with
airplane mode on. Next: Phase R2 (web parity, branch `web/round2-polish`,
ending in a PR) per the spec's own "NEXT" line, or a new phase if the user
redirects. The matching web changes for the earlier bundled-offline-dataset
migration (§5) are committed on the web repo's `data/hadith-api` branch,
uncommitted-PR (both apps reviewed as one unit before any PR opens).

See `docs/RELEASING.md` for the manual GitHub Actions release workflow
(`.github/workflows/release.yml`) that builds a signed `app-release.aab` —
separate from the numbered spec steps, added on request.

| Step | What | Commit | Status |
|---|---|---|---|
| 1 | Scaffold (:app, Gradle wrapper, BuildConfig key, manifest, PRIVACY.md) | `2f5dd49` | done |
| — | Master logo files copied in (Step 2 input) | `0fb79dc` | done |
| 2 | Launcher icon — **scripted, not the Studio wizard** (see Deviations) | `9cc453d` | done |
| 3 | Domain text layer (jsTrim, self-contained, excerpts, paragraphize, plainText/shareText/titleCase, grading) | `daaac49` | done |
| 4 | API/DTOs/normalisation/draw engine (§1.1, §1.2, §1.3, §1.5, P1–P3, P5) | `a7d7fb0` | done |
| 5 | Room + DataStore (§1.6, §1.7, G1) | `a8e9b4c` | done |
| 6 | HadithRepository, AppContainer, P6 (G1, G2, §4.1) | `e3c3c6d` | done |
| 7 | Theme, fonts, shell (§2.1, §2.6, U1, U3, U4, D4) | `68353f5` | done |
| 8 | Reader (§2.2, P6 note, Copy) | `619b36c` | done |
| 9 | Save sheet + Bookmarks tab (§2.3, §2.4, P4) | `3291ac7` | done |
| — | Fix: Step 8's quiet-actions row shipped with no icons (§2.2) | `5e07e9e` | done |
| 10 | About + Licenses (§2.5) | `cd5555e` | done |
| 11 | Card renderer (§3.1, §3.2) | `5e092cf` | done |
| 12 | Share sheet + share paths (§3.3, §3.4, S1, S2) | `70540e0` | done |
| 13 | Release hardening / G7 | `83f5e80` | done |
| 14 | Bundled-dataset data layer (§4: assets, HadithStore, DrawEngine rewrite, Hadith model rewrite, Room snapshot, network removal) | `f86777d` | done |
| 15–16 | Bundled-dataset UI (references, grades, Sunnah.com link, About/Licenses copy) + release/G7 offline check | `4feeff0` | done |
| — | Fix status-bar overlap and stray border on the brief reference | `1b5b512` | done |
| — | Icon-only share targets, centered hero text | `9e86196` | done |
| 17 | Superseded — the user's own bug reports/notes became "Round 2" (§R0–R1 of the spec) instead of a separate emulator-audit step | — | superseded, see Round 2 below |

## Environment notes for the next session

- Android SDK: `C:\Users\aarah\AppData\Local\Android\Sdk`. `ANDROID_HOME` not set — always full paths.
- Gradle runs on Android Studio's bundled JBR 21, not the shell's default JDK:
  `JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew <task>` (Git Bash).
- `local.properties` is gitignored and holds `sdk.dir` + `HADITH_API_KEY`. The
  key currently in there is the **existing public web key** from
  `C:\Users\aarah\Hadith-Pull\script.js` (used as a placeholder per the user's
  instruction). **A real app-only key must replace it before Step 13's release
  check** (D3).
- Per-step gate is `assembleDebug` + `testDebugUnitTest` only. No emulator, no
  instrumented tests, until Step 13's release sanity check (§5.1).

## Version catalog decisions (not pinned by the spec; chosen live, Sep 2026)

AGP 9.4.0, Gradle 9.6.0, Kotlin 2.3.20, core-ktx 1.18.0, activity-compose
1.13.0, lifecycle 2.10.0, Compose BOM 2026.06.00, kotlinx.serialization
1.11.0, JUnit 4.13.2, OkHttp (+ okhttp-coroutines, mockwebserver3) 5.4.0.

**Why not the newest of each:** the actually-latest AndroidX releases at the
time (core-ktx 1.19.0, lifecycle 2.11.0, Compose BOM 2026.09.00) all require
**compileSdk 37**, which conflicts with the spec's locked D2 (compileSdk 36).
Since D2 is locked and compileSdk can't be bumped without going back through
the plan file, each library was stepped back to the newest version that still
targets compileSdk 36. Expect this gap to keep showing up in later steps —
check compileSdk requirements before adding any new AndroidX dependency.

Same gap hit non-AndroidX OkHttp at Step 4: OkHttp 5.5.0 pulls in
`okhttp-android` transitively, whose AAR metadata declares it needs
compileSdk 37 (Android 17 / API 37 ECH support, per the 5.5.0 changelog).
Stepped back to 5.4.0, the newest version without that transitive
requirement, for `okhttp`, `okhttp-coroutines` and the test-only
`mockwebserver3`.

Step 5 added Room 2.8.5, KSP 2.3.12 (the Kotlin Symbol Processing compiler
plugin, needed for Room's annotation processor; matched to Kotlin 2.3.20 by
its own "target Kotlin 2.3" versioning) and DataStore Preferences 1.2.1 —
none of these hit the compileSdk 37 wall, so they're at latest stable.

Also test-only, added after the user asked for Room-backed coverage of
`moveItem`/`renameFolder`: Robolectric 4.17 and `androidx.test:core` 1.7.0,
so `BookmarkRepositoryDbTest` can build a real in-memory Room database
(`Room.inMemoryDatabaseBuilder(context, ...)`) from a plain JVM unit test.
Room's own context-free JVM builder (`Room.inMemoryDatabaseBuilder<T>()` with
`BundledSQLiteDriver`) doesn't resolve here — this module compiles against
Room's Android artifact variant, not its JVM/KMP variant — so Robolectric's
`ApplicationProvider` is the one supplying the `Context` Room still wants.

Step 7 added `androidx.core:core-splashscreen` 1.2.0 (no compileSdk issue) and
`androidx.navigation:navigation-compose`. Latest stable there is 2.10.1, but it
pulls `androidx.lifecycle:lifecycle-*-compose-android:2.11.0` transitively,
which needs compileSdk 37 — same wall as before. Stepped back to
**navigation-compose 2.9.0** (the latest release before that lifecycle bump),
which still has the type-safe (`@Serializable` route) Compose Navigation API
this project uses.

Also: **AGP 9.0+ removed the standalone `org.jetbrains.kotlin.android` Gradle
plugin** — Kotlin compilation is now built into AGP. Don't apply that plugin
and don't use the old `kotlinOptions {}` DSL block (`compileOptions
sourceCompatibility/targetCompatibility` is what controls the JVM target
now). `org.jetbrains.kotlin.plugin.compose` is still required separately for
Compose.

## Deviations from the spec (both approved live by the user)

1. **Step 2 (§5.4, launcher icon): the Android Studio Image Asset wizard was
   replaced with a generation script**, since this session has no GUI/Studio
   access. `logos and favicons/hadith_pull_logo.png` (1254×1254 RGBA) was
   alpha-trimmed to its bounding box (1112×1238), then scaled so the longer
   trimmed dimension fills 66/108 of each adaptive-icon canvas, centered.
   Same math reused for the legacy launcher/round icons at their own sizes.
   Background is a flat `#F6F2EA` color resource, not a wizard-generated PNG.
   Monochrome layer is the same silhouette with alpha preserved, RGB set to
   white (Android tints it at runtime). No ImageMagick was available on this
   machine — used Python 3.13 (`C:\Program Files\Python313\python.exe`) +
   Pillow 11.1.0 instead. The one-off script isn't checked into the repo
   (scratchpad only); if the logo source ever changes, it needs to be
   rewritten or the icons regenerated by hand.
2. Gradle 9.6.0's wrapper jar/scripts were bootstrapped by downloading a
   throwaway Gradle distribution (no `gradle` binary was preinstalled),
   running `gradle wrapper --gradle-version 9.6.0`, then deleting the
   throwaway distribution. This only matters if the wrapper ever needs
   regenerating.

## Known test-coverage gap (flagged, not fixed)

`paragraphize`'s "lossless check fails, fall back to `existing`" branch
(§1.4 step 9) is not separately unit-tested. Given the algorithm as ported,
constructing a text where `paragraphs.joinToString(" ")` squashes differently
from the source seems to require an actual bug in the port rather than a
natural input — the happy-path test exercises the check *passing* instead.
Revisit if this ever turns out to matter.

## Step 4 design note

`DrawEngine` takes `fetch: suspend (slug, number) -> HttpOutcome` rather than
the concrete `HadithHttpClient`, so its retry/exclusion/demotion logic can be
unit-tested against a plain lambda (e.g. one that throws
`CancellationException`) without a real or mocked server standing in for
every scenario. `HadithHttpClient` itself is tested separately against
MockWebServer. Both are wired together in `data/HadithHttpClient.kt` +
`domain/DrawEngine.kt`; `AppContainer` wiring is Step 6.

## Step 5 design note

File-based `DataStore<Preferences>` (`PreferenceDataStoreFactory.create` +
`TemporaryFolder`) fails its own unit tests on this Windows dev machine: its
tmp-file-then-rename write path throws `IOException: Unable to rename ...`
the second time a write targets an already-existing destination file (a
known cross-platform DataStore limitation, not an app bug — Android's Linux
rename() allows overwrite, Windows's doesn't). `SettingsRepositoryTest` uses
a small in-memory `DataStore<Preferences>` test double instead (the same
technique nowinandroid's own test suite switched to for the same reason);
`SettingsRepository` itself is unchanged and takes any `DataStore<Preferences>`,
so real on-device storage is untouched.

## Step 6 design note

`HadithPullApp` (the `Application`, registered in the manifest as
`android:name=".HadithPullApp"`) now owns `AppContainer` — created solely so
`AppContainer` has somewhere to live and a real `Context` to build the Room
database and DataStore file from. `MainActivity` is untouched (still Step 1's
placeholder); wiring it to the container and building the real UI shell is
Step 7's job, not this one.

`HadithRepositoryTest` and `BookmarkRepositoryDbTest` both need a real Room
database, so both run under Robolectric (added in Step 5) rather than a bare
JVM `DrawEngine`-only test.

Two gaps the user caught in review, both fixed: the first fallback test only
ever had one qualifying cached row, so it couldn't tell a correct uniformly
random pick from an accidentally-deterministic one — added a test that seeds
3 rows and asserts more than one distinct key appears across 40 draws. And
`RecentDao` itself (the eviction-past-30 and upsert-on-existing-key behaviour
G1 requires) had no test at all — added `RecentDaoTest` with `count()`,
`allKeys()` and `shownAtOf()` query methods on the DAO purely to make that
observable from a test.

## Step 8 design note

`ReaderViewModel` + `ReaderRoute` (the composable that builds the ViewModel
via `viewModelFactory { initializer { ... createSavedStateHandle() } }` and
wires `AppContainer`'s repositories in) + `ReaderScreen` (the actual §2.2
UI, taking plain state/callbacks — no `AppContainer` reference, so it stays
easy to reason about and to later drop into a preview). `Save`/`Share`
quiet actions call empty `onOpenSave`/`onOpenShare` lambdas for now (Steps 9
and 12 wire them to real sheets); the "Saved" pressed state in the quiet
actions row is deferred to Step 9 too, since it needs `BookmarkRepository`
wiring that has nowhere to attach until the Save sheet exists.

Added a toast mechanism (`ui/components/Toast.kt`: `ToastState` +
`ToastHost`, provided via `LocalToastState` from `AppNav`, positioned above
the bottom nav bar) since §2.2's Copy and text-size actions both need one,
and `androidx.compose.material:material-icons-core` (for the refresh/
chevron icons) and `androidx.lifecycle:lifecycle-viewmodel-savedstate` (for
`createSavedStateHandle()` in the manual `viewModelFactory`) — neither hit
the compileSdk 37 wall.

One correction caught before it shipped: the loading skeleton's shimmer
was first written with `RepeatMode.Restart` (a hard reset each 1.4s cycle,
i.e. a flash, not a shimmer) — fixed to `RepeatMode.Reverse` so it actually
pulses back and forth between `border` and `borderStrong` as the spec's
"shimmer" wording implies.

Two more gaps the user caught in review, before the commit: the decorative
gold quote mark (Cormorant `"`, 16% alpha, `clamp(72, 12%w, 112)sp`, top
-8dp/left -10dp, fading to 0 over 400ms on expand) had been entirely
skipped — added as `QuoteMark()`, layered behind the narration text in a
`Box`. And neither of the two scroll behaviours was implemented: scroll-to-0
after any draw but the first in the process (a `hasShownAResult` flag
remembered across recompositions, reset only by process death — matching
"in this process" exactly) and scrolling the narration block into view on
expand (`BringIntoViewRequester`, which is a superset of the spec's
"only if the top is above the viewport" — it also corrects a block that's
below the viewport, which is harmless).

## Next step for whoever picks this up

Step 14 — bundled-dataset data layer. See the spec's "Data source: bundled
offline dataset" section, §4, and its own Sonnet handoff prompt at the end
of the spec file for the exact remove/add list, the new `Hadith`/`Grade`/
`PrimaryGrade` model, `HadithStore`, the rewritten `DrawEngine` (uniform
draw over all eligible hadiths, no retry/collection-health logic), the
Room `hadithJson` snapshot column, and the full field-rename pass
(slug→collection, book→collectionTitle, number→ref, status→primary?.grade)
across card/share code. `app/src/main/assets/hadith/v1/` must be copied in
first (byte-identical from `Hadith-Pull\data\v1\`, excluding `report.txt`).

## Step 11 design note

`CardRenderer.render` was checked line-by-line against `card.js` itself
(not just the spec's §3.1 summary) — worth flagging since a couple of
details only live in the JS source: `drawRule`'s stroke width (1.3) is
shared by *both* the rule under the Arabic block and the reference-row
rule (the spec states 1.3 for the Arabic one but doesn't repeat it for
the other), and the status pill colours in card.js's own `STATUS_COLORS`
table are bit-for-bit identical to `ui/theme/Colors.kt`'s
`statusPillColors(...).foreground` (checked all 8 values), so the
renderer reuses that instead of a second copy of the same table.

A user review before commit caught a real drift risk: the measuring
Paint (`PaintTextMeasure`, used by `wrap`/`fitBlock` to decide line
breaks) and the drawing Paint (`CardRenderer.render`, which actually
draws those lines) were built from two separately-typed-out flag sets
that happened to be identical at the time. If they'd rendered anything
under the JVM tests, this would've been invisible, since the tests
exercise a fake `TextMeasure`, not real `Paint` — a later edit to one
Paint's flags without the other would've made the computed line breaks
silently stop matching what's drawn. Fixed by extracting one
`newCardPaint()` function both now build from. The RTL Arabic draw
(`canvas.drawTextRun(..., x - measure(line), baseline, true, paint)`
with `Paint.Align.LEFT`) and the narrator's synthetic italic
(`textSkewX = -0.25f`, reset after) were already correct as specified,
not fixes.

`CardFonts` (in `TextMeasure.kt`) takes explicit
`(family, weight, min, max, lineHeight)` with a `CardFonts.forScript()`
factory for production use, rather than only accepting `ArabicScript`
directly. This was needed to unit-test `layoutCard`'s "Arabic block
truncated under 2 lines gets dropped" branch at all: with the real
§3.1 constants, `available` (band minus narrator/arabic-gap) never gets
small enough relative to `min * lineHeight` for that branch to trigger
through any realistic combination of reference/narrator/script inputs —
the test constructs a synthetic `CardFonts` with a deliberately huge
`min`/`max` instead.

## Step 10 design note

About tab (`ui/about/AboutScreen.kt`) and Licenses (`ui/about/LicensesScreen.kt`)
wired into `AppNav` exactly like Step 9's Folder detail: `LicensesDetailRoute`
is a route pushed inside the About tab's back stack (not a `TabRoute`), with
the same `isTabRoot` guard so back pops to the licence list before jumping
to Reader. Licenses' own list-vs-detail toggle is local composable state plus
a scoped `BackHandler`, not a second nav destination, since nothing outside
the screen needs to address a specific licence directly.

`res/raw/apache_2_0.txt` is the one full Apache-2.0 text every non-font
dependency's licence entry points at (Kotlin, AndroidX Core KTX/Activity
Compose/Lifecycle/Navigation Compose/Room/DataStore/Core SplashScreen,
OkHttp, kotlinx.serialization) — the version catalog's actual dependency
list, since the spec doesn't enumerate them. Font entries reuse the
Step 7 `res/raw/ofl_*.txt` files and show each one's first (copyright) line
as the list-row subtitle.

`ic_open_in_new.xml` is hand-drawn (no web equivalent — the "Say salam"
social links and their open-in-new glyph don't exist on the web, which
uses Font Awesome `<i>` tags instead) rather than pulled from
`material-icons-extended`, which isn't a dependency and would have been
the only reason to add one just for this glyph.

A user review before commit confirmed three things that are easy to lose
copying spec prose into Compose string literals: the top-of-screen H1
"About" + lede (`typography.pageTitle`, matching the Reader/Bookmarks hero
pattern), all three footer lines (not just the version line), and the
"Show full Hadith" span in paragraph 1 rendered bold via
`buildAnnotatedString`/`SpanStyle(fontWeight = FontWeight.Bold)` rather
than as plain text — all three were already correct in the first draft,
not fixes.

## Step 9 design note

Save sheet (`ui/reader/SaveSheet.kt`) and the Bookmarks tab
(`ui/bookmarks/{BookmarksViewModel,FolderDialogs,FoldersScreen,FolderScreen}.kt`)
call `BookmarkRepository` directly from the composable (via
`collectAsState` + a `rememberCoroutineScope` launch), the same pattern
`ReaderRoute` already used for settings — no dedicated ViewModel for the
sheet itself, since it's just a reactive view over Room. `BookmarksViewModel`
exists only because both Bookmarks screens need it (folders/folder/items
flows plus the five write operations), and each screen gets its own
instance (scoped to its own nav back-stack entry) since the shared state
lives in Room, not the ViewModel.

Folder detail (`FolderDetailRoute(id: Long)`) is the app's first screen
pushed *inside* a tab's back stack, which exposed a real bug in `AppNav`:
the "back jumps to Reader" override (§2.1 rule 3) was keyed only on
"is this tab non-Reader," so it would have fired on Folder detail too and
skipped popping back to the Folders list (rule 2). Fixed by adding an
`isTabRoot` check so the override only applies to an actual tab root.
The same fix made the top bar route-aware: `Scaffold`'s `topBar` now only
renders `HadithTopBar` on a tab root; Folder detail draws its own
back-arrow bar (`HadithBackTopBar`, new in `TopBar.kt`) as the first thing
in its own composable, which sidesteps double top bars without needing
`Scaffold`'s topBar slot to be aware of the pushed screen's content.

`FolderRoute` distinguishes "still loading" from "folder deleted out from
under me" (§2.4: "if the folder stops existing... pop back to Folders")
by tracking a `folderLoaded` flag set on the first Room emission, rather
than trusting `collectAsState(initial = null)` directly — that placeholder
`null` is indistinguishable from "not found" and would have popped back
instantly on every entry, before Room's first real emission arrived.

Two things flagged rather than silently decided, per the user's review:
1. The folder card's Rename/Delete icons are now 40dp (glyph) inside a
   48dp touch target, matching §2.4's literal wording — increased from an
   initial 20dp guess.
2. §2.2's quiet-actions row icons (copy/bookmark/upload-arrow) were
   missing entirely since Step 8 (`619b36c`) — text-only labels, no icons
   in any state. That's a Step 8 gap, not new Step 9 scope, so it's fixed
   in its own commit `5e07e9e`, not folded into `3291ac7`: ported the copy
   and upload-arrow icons from their exact SVG paths in `index.html`
   (`ic_copy.xml`, `ic_upload.xml`), reused `ic_bookmark.xml` for Save, and
   added `ic_bookmark_filled.xml` (solid version of the same path) layered
   underneath at `accentSoft` so the saved state reads as an
   accent-outlined, softly-filled bookmark — both icon and text turn
   `accent` when saved.

## Step 7 design note

**Fonts:** all 5 files pulled from `github.com/google/fonts` `ofl/` at the
exact paths §2.6 lists, and their byte sizes matched the spec's table exactly
(amiri_regular.ttf 431 KB, scheherazade_regular.ttf 332 KB,
noto_naskh_arabic_var.ttf 308 KB, cormorant_garamond_var.ttf 1,196 KB,
inter_var.ttf 877 KB) — good confirmation the right files landed. OFL.txt
files copied to `res/raw/ofl_<family>.txt` (family-slug naming wasn't
specified further; `ofl_scheherazade_new` / `ofl_noto_naskh_arabic` /
`ofl_cormorant_garamond` are this session's choice).

**Icons:** sun, moon and bookmark are exact ports of the SVG `d` paths from
`index.html` (verified against source, not memory). The Read and About tabs
have no web equivalent (the web has no bottom nav) — "open book outline" and
"info circle" are hand-drawn to match the sun/moon/bookmark stroke style
(viewBox 24×24, strokeWidth 1.7, round caps), not ported from anywhere. All
five are Android vector drawables (`res/drawable/ic_*.xml`) rather than
`ImageVector.Builder` code, since Android's vector `pathData` accepts the
same arc syntax as SVG `d` verbatim — hand-converting the moon/bookmark arcs
into Compose's `Path` arc API would have risked a subtly wrong shape for no
benefit.

**Backdrop blob B's centre** isn't given by the spec (only blob A's centre
formula and blob B's diameter are stated; §2.6 says B is "placed bottom-left
symmetrically per style.css:174-189"). Re-derived it by applying the same
box-edge-plus-or-minus-radius algebra that reproduces the spec's own blob A
centre formula from `.glow-a`'s CSS (`top:-16vmax; right:-12vmax`), to
`.glow-b`'s CSS (`bottom:-18vmax; left:-12vmax`): centre =
`(-0.12·max + 0.40·max, h + 0.18·max - 0.40·max)`. Worth the user's eyes on
this one specifically since it's the one number in Backdrop.kt I derived
rather than copied.

**Grain texture** (the 4dp/1dp paper-grain dot grid) is a tiled `BitmapShader`
built once and cached via `remember`, not tens of thousands of individual
`drawCircle` calls — same visual result, far cheaper per frame.

**Not yet wired:** Folder detail and Licenses routes don't exist (their
steps haven't happened), so the §2.1 back-stack rule 1 (closing a sheet/dialog
first) and rule 2 (popping within a tab) have nothing to exercise yet beyond
what `NavController`'s defaults already do. Rules 3 and 4 are live and
testable now (any non-Reader tab root back-navigates to Reader; Reader's
back exits, unhandled).

**Version resolution note for next time:** `RowScope.weight` needed to be
used via Compose's implicit default imports, not an explicit
`import androidx.compose.foundation.layout.weight` — that explicit import
resolved to an unrelated *internal* `RowColumnParentData.weight` property
sharing the same simple name in the same package and failed to compile.
`NavDestination.hasRoute(route: KClass<*>)` also isn't the right call for
type-safe routes in navigation-compose 2.9.0 — the reified
`NavDestination.hasRoute<T>()` (no KClass argument) is.

---

## Round 2: polish and bug-fix pass (Steps 18–26)

Spec: `Phase R0` (review, decisions R-Q1–R-Q4) and `Phase R1` (the Step
18–26 build spec) in the same spec file, appended 2026-09-25. Per the
user's instruction, Steps 18–26 are being built straight through in one
session, without stopping for a "go" after each step — but each step is
still committed separately, in order, with its own report below, exactly
as if approval gating were still on. A consolidated summary follows after
Step 26.

### Step 18 — Fix hidden toasts inside the Save and Share sheets

**Done:** `ModalBottomSheet` renders in a separate dialog window on top of
the activity's `Scaffold`, where `AppNav.kt`'s `ToastHost` lives. Any toast
fired while a sheet was open ("Saved to {name}", "Folder \"{name}\"
created", "Card saved", the Instagram-fallback message, the storage-
permission message, the render-failure message) rendered behind the sheet
and its scrim and was invisible — the underlying action still succeeded,
so a user tapping again could save duplicates without any feedback.
`SaveSheet.kt` and `ShareSheet.kt`'s private `ShareSheet` composable each
now wrap their `Column` in a `Box` with their own `ToastHost`, anchored
bottom-centre with a 24dp offset, matching the existing host's placement.
`AppNav.kt`'s own `ToastHost` is untouched — it still covers toasts fired
with no sheet open (clipboard copy, "Text size: …", folder create/rename/
delete from the Bookmarks tab).

**Commands run:**
- `JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug --rerun-tasks` → BUILD SUCCESSFUL in 55s (a first run showed everything UP-TO-DATE despite the edits — a stale Gradle daemon from before this session's edits; stopping the daemon and re-running with `--rerun-tasks` confirmed a genuine clean compile with no errors, only one pre-existing warning in `LicensesScreen.kt` unrelated to this step).

**Acceptance:**
- `assembleDebug` passes: PASS
- Both `SaveSheet.kt` and the private `ShareSheet` composable in `ShareSheet.kt` contain a `ToastHost` call inside their `ModalBottomSheet` content: PASS (verified by grep)

**Unspecified choices:** none — this step's instructions were exact.

**Blockers/questions:** none.


### Step 19 — Copy pass: em dashes and the About Sunnah.com paragraph

**Done:** every user-visible em dash listed in the spec's Android table
(§R1.2) is replaced by exact match: `shareText`'s reference line, the full-
reference grade rows (now `"{by}: {grade}"`), the Reader hero subline, the
Bookmarks subline, the Save sheet's empty state, three Share-sheet strings
(permission toast, Instagram-fallback toast, sheet subtitle), six About
strings (three prose paragraphs, "A note on the Arabic", the grades
paragraph, the skipped-entries paragraph, "Say salam", the footer line),
and both privacy-policy title lines (`res/raw/privacy_policy.txt`,
`PRIVACY.md`). `LicensesScreen.kt`'s five OFL-subline em dashes are
deliberately left as-is per the spec — Step 23 replaces that file's entire
entry list, so fixing them now would be edited twice. Code comments
(`AppTypefaces.kt`, `CardRenderer.kt`, `HadithRepository.kt`,
`DrawEngine.kt`, `ShareActions.kt`, `Icons.kt`, `AppNav.kt`) are untouched,
per the spec's scope.

Also rewrote About's "Where the texts come from" closing paragraph (item 7)
to the spec's exact wording: the Sunnah.com link is a generated URL, not a
data source, and the app pulls no text or data from Sunnah.com.

**One test updated:** `PlainTextTest.kt`'s `shareText` expectation dropped
its leading `"— "` to match the new copy. No other test asserted an
em-dash string produced by app code (the `"— Narrated X"` fixtures in
`CardInputTest.kt`/`TextMeasureTest.kt`/`PlainTextTest.kt` are arbitrary
test data for the `narrator` field, not em-dash copy the app generates —
Android's `narrator` field never carries that prefix itself, unlike the
web's).

**Commands run:**
- `grep -rn "—" app/src/main/java app/src/main/res/values app/src/main/res/raw/privacy_policy.txt PRIVACY.md` (before and after) → confirmed only comments and `LicensesScreen.kt` remain.
- `JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug testDebugUnitTest` → BUILD SUCCESSFUL in 25s, all tests green.

**Acceptance:**
- `assembleDebug` + `testDebugUnitTest` pass: PASS
- Every string in the spec's table appears verbatim at its cited location: PASS
- `grep` for em dashes outside comments/third-party license text returns nothing (excluding `LicensesScreen.kt`, deferred to Step 23 by design): PASS

**Unspecified choices:** none — every replacement string was given verbatim in the spec.

**Blockers/questions:** none.


### Step 20 — Typography tiers, Arabic sizing, reading-bar visibility

**Done:** `Type.kt`'s Arabic size formula now reuses `englishSize` (already
scaled by the reading-scale preference) times the script's `arabicScale`
factor, deleting the separate `clamp(24, 0.044w, 33.6)` base (R-Q3). Four
new `HadithTypography` roles were added -- `contentMeta` (15sp·scale, Inter
600, line-height 1.4), `contentMetaItalic` (same but italic/regular
weight, line-height 1.5), `secondaryScaled` (13sp·scale, Inter 400) and
`label` (11.5sp·scale, Inter 600, tracking 0.07em) -- and wired at every
call site the spec's table names: the brief reference's main line and
chapter line, every `ReferenceField` label/value pair, the full
reference's chapter value and grade rows, and the narrator line (which now
uses `contentMetaItalic` in place of the old fixed-size `narrator` style).
The now-unused `narrator` `TextStyle` field was removed from
`HadithTypography` as a Simplicity-First cleanup, along with the
`FontStyle` import in `ReaderScreen.kt` that only its old call site needed
(`placeholder` still uses `FontStyle` inside `Type.kt` itself, so the
import stays there).

The script toggle's visibility condition changed from "the hadith has any
Arabic text" to `expanded && hasArabicWorthShowing(arabic)` -- it now
tracks whether Arabic is actually on screen, not just present. `ReadingBar`
centres the text-size chip at every width when the toggle is hidden; when
shown, it keeps the existing phone-centred / ≥720dp-end-aligned split.

Fixed-size controls (the "Aa" chip, script-toggle labels, the expand
toggle, the Sunnah link, the quiet-actions labels) and the fixed secondary
tier (attribution, About's footer) were left untouched, per the spec --
they don't scale with the reading-scale setting by design.

**Commands run:**
- `JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug testDebugUnitTest` → BUILD SUCCESSFUL in 20s, all tests green.

**Acceptance:**
- `assembleDebug` + `testDebugUnitTest` pass: PASS
- The four new typography roles exist and are wired at every call site in the spec's table: PASS
- The Arabic size formula no longer has its own clamp: PASS
- The script toggle's visibility condition matches `expanded && hasArabicWorthShowing(arabic)` exactly: PASS
- Compose-only logic (the two visibility conditions) has no JVM test, as the spec anticipated -- NOT TESTED by an automated test; verified by reading the composable.

**Unspecified choices:** none.

**Blockers/questions:** none.


### Step 21 — Reader dock layout and scroll rules

**Done:** `ReaderScreen`'s single `Column().verticalScroll(...)` is replaced
by `BoxWithConstraints` choosing docked vs. stacked mode at a 480dp content-
height threshold. Docked mode: a `LazyColumn` (`ReaderContentList`, the
only scrolling region) holding the hero, a `stickyHeader` reading bar, the
narration block, the full reference (only when expanded) and the
attribution line, with a fixed `ReaderDock` sibling below it holding the
brief reference + status pill, the chapter line + Sunnah link, New Hadith,
and the Copy/Save/Share row (R-Q1 -- fixed, its own row). Stacked mode
(short or landscape windows) renders the same `ReaderDockContent` as a
trailing item inside the same `LazyColumn` instead of a second layout --
there is still exactly one scrolling region either way.

`BriefReference` and the Reader's two old `SunnahLink` call sites are
gone; the Sunnah link now lives only in the dock, with a new
`keepSpaceWhenHidden` parameter that renders the same row at `alpha = 0f`
and non-clickable instead of collapsing when `sunnahUrl` is null, so the
dock's second row never changes height. `FullReference` stays in the
scrolling list, shown only when `state.expanded`. The dock's top two rows
(ref-main + pill, chapter + link) show shimmer placeholders while Loading
and stay empty-but-sized during Failure, so nothing jumps height as a new
hadith arrives (R0.3's "dock height stability").

The skeleton's shimmer animation was factored into a small
`shimmerColor()` composable, reused by both `SkeletonBlock` and the dock's
two loading placeholders, rather than duplicating the
`rememberInfiniteTransition` block three times.

Scroll rules: scroll-to-top on every draw after the first (via
`listState.scrollToItem(0)` inside the existing `LaunchedEffect(uiState)`)
and scroll-to-the-reading-bar on expand/collapse
(`listState.animateScrollToItem(1)` after a `withFrameNanos {}` yield, so
the toggled content has measured before the scroll runs) replace the old
`ScrollState.animateScrollTo(0)` and `BringIntoViewRequester` calls, which
are both gone from the file.

**Commands run:**
- First `assembleDebug` attempt failed: `Unresolved reference 'stickyHeader'` -- I'd added an explicit `import androidx.compose.foundation.lazy.stickyHeader`, but `stickyHeader` is a member function of `LazyListScope`, not a top-level extension, so the import itself was invalid. Removed it; `stickyHeader("bar") { ... }` resolves fine as a plain member call inside the `LazyColumn` lambda (still needs `@OptIn(ExperimentalFoundationApi::class)` on `ReaderContentList`, which stayed).
- `JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug testDebugUnitTest` → BUILD SUCCESSFUL in 14s after the fix, all tests green.
- `grep -n "BringIntoViewRequester\|verticalScroll\|rememberScrollState"` on the file → no hits.
- `grep -c "LazyColumn("` on the file → 1.

**Acceptance:**
- `assembleDebug` + `testDebugUnitTest` pass: PASS
- Exactly one `LazyColumn`, no `verticalScroll` remains: PASS
- The dock's two top rows are height-stable across states: PASS (verified by reading the composable -- both branches of each `when` occupy the same row height: `Text`/skeleton `Box`/`Spacer` all sized consistently, and the 48dp `heightIn` on the link row is unconditional)
- The Sunnah link appears exactly once on screen, inside the dock: PASS
- `BringIntoViewRequester` no longer appears in the file: PASS
- Compose layout has no JVM test coverage, as the spec anticipated -- NOT TESTED by an automated test; this is the step most worth the user's own device pass, especially the sticky-bar background swap and the two scroll animations, which can't be verified by reading code alone.

**Unspecified choices:** none beyond what the spec's code sample already fixed -- I followed the R1.4 structure as written, including its exact composable names and parameter shapes.

**Blockers/questions:** none. (One self-corrected build error, not a spec ambiguity -- noted above for the record.)


### Step 22 — Bookmarks redesign: one grouped list instead of a card grid

**Done:** `FoldersScreen.kt`'s `LazyVerticalGrid` of bordered `FolderCard`s
is replaced by a single bordered/rounded surface holding a plain `Column`
of `FolderRow`s with hairline dividers between them (folder counts are
small, so a plain `Column` was used rather than `LazyColumn`, matching the
existing Save-sheet folder list's own pattern). Each row: a 40dp
`accentSoft` leading tile with a filled bookmark glyph, the folder name
(Cormorant 600, 20sp) over its count, a ⋮ overflow button opening a
`DropdownMenu` with Rename and Delete (Delete text in the `error` colour),
and a trailing chevron. The header block (eyebrow, "Bookmarks" title, the
≥720dp subline) and the create row are now centred, matching the Reader
hero's treatment.

**Deviation from the spec's literal code, flagged:** the spec's sample used
`Icons.Filled.ChevronRight`. This project depends on
`androidx.compose.material:material-icons-core` only (not `-extended`),
and `ChevronRight` lives in the extended set -- the first build failed
with `Unresolved reference 'ChevronRight'`. Used
`Icons.AutoMirrored.Filled.KeyboardArrowRight` instead (the core set's
equivalent glyph, and the RTL-aware variant rather than the deprecated
non-mirrored one, which triggered its own compiler warning on the first
attempt). `Icons.Filled.MoreVert` compiled as specified -- it is in core.

**Commands run:**
- First `assembleDebug`: FAILED -- `Unresolved reference 'ChevronRight'` at two call sites. Fixed by switching to `Icons.Filled.KeyboardArrowRight`.
- Second `assembleDebug`: BUILD SUCCESSFUL, but with a deprecation warning on `Icons.Filled.KeyboardArrowRight` ("Use the AutoMirrored version"). Fixed by switching to `Icons.AutoMirrored.Filled.KeyboardArrowRight`.
- `JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug testDebugUnitTest` → BUILD SUCCESSFUL in 13s, clean (no warnings), all tests green.

**Acceptance:**
- `assembleDebug` + `testDebugUnitTest` pass: PASS
- The folder list renders as one bordered surface with hairline dividers, not a grid: PASS
- Each row is ≥72dp with a 40dp accent-soft leading tile, name, count, a ⋮ menu (Rename/Delete, Delete in error colour), and a trailing chevron: PASS
- Eyebrow, title, subline and create row are centred: PASS

**Unspecified choices:** the chevron icon substitution above (forced by the project's dependency set, not a free choice); worth a glance to confirm the glyph reads the same as a true chevron at 16dp (KeyboardArrowRight is visually a thinner caret, not a chevron -- close enough at this size, but flagging since the spec named a specific icon).

**Blockers/questions:** none -- both issues were mechanical (a missing-icon compile error and a deprecation warning), not spec ambiguity, and were self-corrected within the step.


### Step 23 — About redesign and Licenses consolidation

**Done:** `AboutScreen.kt` gets a centred hero (eyebrow "HADITH PULL",
title "About", lede) matching Reader/Bookmarks, and a centred footer. The
three prose sections (about-the-app, Arabic typefaces, where the texts
come from) are rewritten as `ProseSection` -- an eyebrow, a Cormorant
title, body text, no surrounding box -- separated by hairline dividers
via `SectionDivider`, instead of the old boxed `Panel`. Appearance, Say
salam and a new "Legal" group (replacing two separate plain rows for
Privacy policy / Open-source licenses) became `GroupPanel`-wrapped grouped
lists, reusing the Step 22 row pattern: a bordered/rounded surface with
hairline dividers between rows. The three social links moved from stacked
`OutlinedButton`s into one such list (`LinkRow`, trailing open-in-new for
external links, a chevron for the two in-app destinations). Source chips
switched fill from `colors.bg` to `colors.surfaceSolid` per the spec.

`LicensesScreen.kt`'s entry list is rewritten around a `LicenseRow`
sealed interface (`Single` / `Group`). The Hadith API/Unlicense entry
stays one prominent top row, titled "Hadith API dataset (Fawaz Ahmed)".
Five OFL font entries and eleven Apache-2.0 library entries (the original
ten plus `kotlinx.coroutines`, missing from the old list despite shipping
in the app) each collapse into one row -- "Fonts" and "Third-party
libraries" -- with their subtitles giving the family/library names and
the license inline, so nothing meaningful is hidden, just de-emphasised.
`LicensesRoute` gained a middle navigation level (`expandedGroup`) between
the top list and a license's full text; `BackHandler` now clears
whichever level is innermost first.

Verified the library name list against the release dependency tree
(`:app:dependencies --configuration releaseRuntimeClasspath`, filtered to
`org.jetbrains.kotlin*`/`androidx.*`): every Apache-2.0 group present in
the classpath (Compose, Activity, Annotation, Arch Core, Collection, and
the rest of the AndroidX/Compose surface) is covered by the existing
"AndroidX and Jetpack Compose libraries" umbrella in the group's subtitle
-- no additional named group was missing.

This step also finished Step 19's deferred em-dash cleanup for
`LicensesScreen.kt`, since the whole entry list was replaced anyway.

**Commands run:**
- `JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew :app:dependencies --configuration releaseRuntimeClasspath` (filtered) → confirmed the library list's coverage.
- `JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug testDebugUnitTest` → BUILD SUCCESSFUL in 14s. The only compiler warning is the pre-existing `@RawRes` annotation-target notice on `LicenseEntry`'s constructor, present since Step 10 -- unrelated to this step's changes.
- `grep -n "—" AboutScreen.kt LicensesScreen.kt` → no hits.

**Acceptance:**
- `assembleDebug` + `testDebugUnitTest` pass: PASS
- About's hero and footer are centred: PASS
- The three prose sections have no box/border: PASS
- Appearance, Say salam and Legal are grouped-list surfaces: PASS
- Licenses shows the Hadith API entry prominently, one Fonts group and one Third-party libraries group (with kotlinx.coroutines present), and every group drills into its own entries: PASS

**Unspecified choices:**
- The Hadith API entry title uses the parenthetical form ("... (Fawaz Ahmed)") rather than an em dash, per the spec's own stated default when it flagged this as a judgment call.
- Section-1 (the three free paragraphs) needed an eyebrow/title of its own for the new `ProseSection` wrapper; used "ABOUT THE APP" / "Read. Reflect. Remember." as the spec suggested, reusing the Reader's own H1 line rather than inventing new copy.
- Section 2 and 3 also needed short eyebrow words the spec didn't specify verbatim ("TYPEFACES", "SOURCE") -- their titles ("A note on the Arabic", "Where the texts come from") were already fixed by the spec and are unchanged; only the short all-caps eyebrow above each is my own wording, matching the Reader's "HADITH OF THE MOMENT" pattern.

**Blockers/questions:** none.


### Step 24 — Icons: `tools/make_icons.py`

**Done:** added `tools/make_icons.py`, run with the system's `python`
(Anaconda's, with Pillow 10.4.0 available -- verified before starting).
It loads `C:\Users\aarah\Hadith-Pull\logos and favicons\hadith-pull-logo_app.png`,
keys its opaque cream card background (and white outer corners) to
transparent by per-pixel colour distance, crops to the mark's content
bounding box with 2% padding, then fits that mark into the central 66dp
safe zone of a 108dp adaptive-icon canvas at all 5 densities
(`ic_launcher_foreground.png`), derives a white-silhouette
`ic_launcher_monochrome.png` from the same alpha channel, and composites
legacy square/round `ic_launcher.png`/`ic_launcher_round.png` at the
5 legacy sizes with the `#F6F2EA` background baked in. It also writes
`drawable-nodpi/brand_logo.png` -- the same keyed/cropped mark centred on
a 192x192 transparent canvas, no safe-zone shrink -- replacing the old
copy of the website's favicon-derived art (R-Q2). The script takes no
arguments and is idempotent; nothing about this step touched
`mipmap-anydpi-v26/ic_launcher.xml` or `colors.xml`, since both already
pointed at the right filenames/colour from the original Step 2 build.

**Verified before committing** (not just eyeballed): the foreground's four
corner pixels and two near-corner samples are all `(0,0,0,0)` -- fully
transparent, no white square or frame-line remnant; the monochrome
layer's alpha bounding box exactly matches the foreground's; a sampled
monochrome pixel inside the mark reads opaque white `(255,255,255,255)`.

**Commands run:**
- `python tools/make_icons.py` → `make_icons: wrote foreground/monochrome/legacy icons at 5 densities, and brand_logo.png`.
- `python -c "..."` (Pillow pixel-bbox/corner checks above) → all as expected.
- `JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug testDebugUnitTest` → BUILD SUCCESSFUL in 11s, all tests green.

**Acceptance:**
- The script runs clean and reports success: PASS
- `assembleDebug` passes: PASS
- All five foreground/monochrome density folders and both legacy-icon density sets regenerated: PASS (`git status` shows all 20 PNGs + brand_logo.png modified)
- `brand_logo.png` is the new logo with a transparent background: PASS (192x192, alpha bbox 17,9-175,183 -- real transparent margin, not the old opaque favicon copy)
- No manual Android Studio step was needed: PASS

**Unspecified choices:** none -- `CARD_BG`, `KEY_TOLERANCE` and the safe-zone fraction were all given exact values in the spec, taken from this session's own earlier pixel measurements of the source file.

**Blockers/questions:** none. `logos and favicons/hadith_pull_logo.png` (the old, undeleted master file the user mentioned) is still on disk in the web repo, untouched -- out of this app's scope per R0, flagging again in case the user meant to remove it themselves.


### Step 25 — Bookmark export/import (item 12, R-Q4)

**Done:** new package `data/library/`:
- `LibraryModels.kt` -- `LibraryExportDocument`/`Folder`/`Item`, the JSON schema embedded in an
  exported file. Only each item's `key` is stored; text is never round-tripped through the file.
- `LibraryHtmlTemplate.kt` -- the static HTML/CSS/JS shell (searchable, JS-off safe -- every
  `<article>` is visible by default, the inline script only adds filtering) plus `htmlEscape` and
  `renderFoldersHtml`, which escapes every visible field, including folder names (the XSS vector
  the spec called out explicitly for user-typed text).
- `LibraryExport.kt` -- `LibraryExport.buildDocument(bookmarkRepository, folderId?)` assembles the
  page: the embedded JSON has its `</` sequences escaped to `<\/` so an item's data can't close
  the `<script>` block early.
- `LibraryImport.kt` -- `LibraryImport.parse(bytes)` extracts and un-escapes the embedded JSON,
  validating the `format` field, folder/item counts (200/5000) and the 10MB byte cap before ever
  attempting to decode; `BookmarkRepository.applyImport(doc, hadithSource)` resolves every key
  against the bundled dataset and applies the R0.6 merge rules.

`BookmarkRepository` gained `exportSnapshot(folderId?)` (decodes each bookmark's existing
`hadithJson` snapshot -- no new decode path), `ensureFolder(name)` (case-insensitive match-or-
create, reusing `createFolder`'s own lookup rather than duplicating it), and a two-case
`EnsureSavedResult` (`Added`/`AlreadyPresent`) so `ensureSaved` -- unchanged in behaviour, just a
richer return type -- can tell an import loop which happened; Room's own `OnConflictStrategy.IGNORE`
already made this free (`insert` returns `-1L` on the unique-index conflict, no extra query
needed). `HadithStore.kt` gained `HadithSource.resolve(key)`, a suspend extension that splits the
key, looks up the collection in `index.json`, and scans its shards in order for a matching `ref`
-- import-time only, so no new index structure was needed.

UI: `FoldersScreen.kt` gained a "Library" row (Export via `CreateDocument("text/html")`, Share via
the existing `ShareActions.kt` cache/FileProvider pattern extended with a new
`writeLibraryToCache` + `library/` cache dir + `file_paths.xml` entry, Import via `OpenDocument`),
with a confirm `AlertDialog` (folder/item counts) before writing anything and a summary
`AlertDialog` after, phrased exactly per the spec's template. `FolderScreen.kt` gained a "Share
folder" top-bar icon action exporting just that folder straight to the share chooser, no SAF save
step. `AppContainer.hadithStore` is now public so the UI can pass it into `applyImport`.

Both Android privacy-policy copies (`res/raw/privacy_policy.txt`, `PRIVACY.md`) gained the
"Exporting your bookmarks" paragraph after "Sharing", worded exactly per the spec.

**Deviations flagged:**
- The spec's `LibraryExport.buildDocument` signature included an unused `hadithStore: HadithStore`
  parameter (never referenced in its own pseudocode body). Dropped it -- export builds entirely
  from `exportSnapshot`'s already-decoded `Hadith` snapshots, so the dataset store adds nothing;
  keeping an unused parameter would violate Simplicity-First for no benefit.
- The "Library" entry point is an expand-in-place row (tap to reveal Export/Share/Import as a
  quiet-text row) rather than a separate bottom sheet -- the spec offered either explicitly
  ("opening a small bottom sheet or inline expansion"), and inline expansion needed no new sheet
  component.

**Commands run:**
- `JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug` (before writing tests, to catch wiring mistakes early) → BUILD SUCCESSFUL in 10s on the first attempt.
- `JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug testDebugUnitTest` (after adding the tests below) → BUILD SUCCESSFUL in 12s.
- Verified test counts directly from the JUnit XML reports rather than trusting "BUILD SUCCESSFUL" alone: `BookmarkRepositoryImportTest` 3/3, `LibraryHtmlTemplateTest` 3/3, `LibraryImportTest` 8/8, `HadithStoreTest` 3/3 (1 pre-existing + 2 new `resolve()` assertions folded into one test method... actually 3 total test methods, one of which is new). Whole-suite total: **105 tests, 0 failures, 0 errors.**

**Acceptance:**
- `assembleDebug` + `testDebugUnitTest` pass, including the new tests: PASS
- An exported file opens correctly in a browser with JS disabled: NOT TESTED by an automated check (no browser harness in this JVM-test project) -- every `<article>` renders unconditionally with no `hidden` attribute by default, and the filtering `<script>` only ever adds behavior, so this is correct by construction; worth the user's own visual check.
- The embedded JSON round-trips through `LibraryImport.parse` for a file this build itself produces: PASS (`LibraryImportTest`'s escaping round-trip case, plus the full pipeline exercised end-to-end in `BookmarkRepositoryImportTest` via `applyImport`)
- A folder name containing HTML special characters exports safely and imports correctly without double/under-escaping: PASS (`LibraryHtmlTemplateTest`'s `<script>`-in-folder-name case for the export side; `LibraryImportTest`'s `</script>`-round-trip case for the import side -- the name is read from the JSON block, which is escaped once on write and unescaped once on read, never touching the HTML-escaped visible copy)

**Unspecified choices:** the two flagged above (dropped unused parameter, expand-in-place UI
instead of a bottom sheet).

**Blockers/questions:** none.

**Post-commit fix (`68b1e4d`), found during live device verification of the export/share flow:**
`LibraryExport.kt` called the top-level `Json.encodeToString(document)`, which uses
kotlinx.serialization's default `Json` instance. That instance omits any field equal to its
declared default value, so `format` ("hadith-pull-library") and `version` (1) -- both declared
with defaults in `LibraryExportDocument` -- were silently missing from every exported file's
embedded JSON, even though `LibraryImport.parse` checks `doc.format` and the schema in R0.6/R1.8
documents both fields as always present. Fixed by giving `LibraryExport` its own
`Json { encodeDefaults = true }` instance. Caught by manually exporting a bookmark on-device
(SAF save dialog, `adb pull`, inspected the embedded `<script type="application/json">` block) --
not by the JVM test suite, since `LibraryImportTest`'s round-trip tests are built from
`LibraryExportDocument` Kotlin objects (which always have both fields, whether written explicitly
or defaulted) fed straight into `Json.decodeFromString`, never through `LibraryExport.buildDocument`
itself, so the omission never showed up as a parse failure in-process.


### Step 26 — Release build sanity check (R1.9, the G7 pattern)

**Done:** `assembleRelease` + `lintDebug` clean (one pre-existing, unrelated `LicensesScreen.kt`
compiler note, present since before Round 2). Signed a copy of the unsigned release APK with
build-tools 36.0.0 `apksigner` and the debug keystore (`%USERPROFILE%\.android\debug.keystore`,
password `android`), installed and launched it on the running `emulator-5554` (Medium Phone
API 36) with airplane mode on. `aapt2 dump badging` confirms the release manifest still carries no
`INTERNET` permission, so airplane mode is a belt-and-braces check, not a load-bearing one -- this
app cannot reach the network regardless of radio state.

On-device checks, all via `adb shell input tap` + `uiautomator dump` for exact coordinates
(screenshots reviewed, not just asserted): a cold draw renders correctly with the new dock layout,
the new launcher icon and header brand mark show the regenerated logo (Step 24), the Save sheet's
"Folder \"Test\" created" toast renders on top of the sheet and its scrim (confirms Step 18's fix
survives in a release/R8 build, not just debug), the Bookmarks tab shows the Step 22 grouped-list
redesign, and the Library row's Export action opens the real SAF "Save a copy" dialog with the
correct default filename (`hadith-pull-library-2026-09-25.html`). Saved the file, `adb pull`ed it,
and confirmed it is valid self-contained HTML: every visible field is HTML-escaped, the Arabic
block is behind a JS-independent `<details>`, and the embedded
`<script type="application/json" id="hadith-pull-library">` block round-trips exactly the schema
R0.6 specifies -- this is also the on-device confirmation that the Step 25 post-commit fix above
actually reaches a real release build, not just the JVM test suite.

`./gradlew :app:dependencies --configuration releaseRuntimeClasspath` re-confirms the dependency
tree is unchanged from before Round 2 -- no new library, matching the expectation that the
export/import feature used only what was already present (kotlinx.serialization, AndroidX SAF
contracts, FileProvider).

**Commands run:**
- `JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew assembleRelease lintDebug --rerun-tasks` → BUILD SUCCESSFUL in 4m 22s.
- `apksigner sign --ks debug.keystore ...` on a copy of `app-release-unsigned.apk` → signed clean.
- `adb install -r` + `adb shell am start` on `emulator-5554` → app launches, draws, no crash.
- `aapt2 dump badging` on the unsigned release APK → confirmed no `INTERNET` permission.
- `adb shell input tap` / `uiautomator dump` / `adb shell screencap` cycle → Save-sheet toast,
  Bookmarks redesign and Library export flow all visually confirmed from pulled screenshots.
- `adb pull` on the exported `.html` → inspected directly; embedded JSON has `format`/`version`
  present (the just-fixed bug), folder/item data correct, HTML escaping correct.
- `./gradlew :app:dependencies --configuration releaseRuntimeClasspath` → dependency tree unchanged.
- Cleaned up: uninstalled the signed test build and deleted the on-device exported/screenshot files
  after verification; the local signed-APK copy was also deleted (release APKs are not checked in).

**Acceptance:**
- `lintDebug` clean: PASS (one pre-existing, unrelated compiler note only)
- All JVM tests green (carried over from Steps 18-25's own runs plus this step's `assembleRelease`
  build, which compiles the same sources): PASS
- Signed release APK installs, launches, and a draw + Save-sheet toast are visible on screen: PASS
- Share-sheet toast: NOT independently re-checked this step (Step 18's report already confirmed
  both `SaveSheet.kt` and `ShareSheet.kt` share the identical fix, verified by grep at the time);
  the Save-sheet check above exercises the same underlying mechanism in a release build.
- Launcher icon and brand mark are the new artwork: PASS (visual, from the pulled screenshots)
- Exported library file opens as valid HTML with the correct embedded JSON: PASS (opened as text
  and inspected directly, not opened in an actual browser on this pass -- Step 25's own report
  already flagged the JS-disabled-browser check as the user's own manual pass to make; nothing new
  to flag here beyond confirming the file itself is well-formed)

**Unspecified choices:** none.

**Blockers/questions:** none. The one real finding this step's verification produced was the
Step 25 JSON-defaults bug fixed above, caught precisely because this step (and the user's own
manual pass before it) exercised the actual exported file instead of trusting the JVM tests alone.


---

## Round 3: visual redesign (Steps 27-34)

### Step 27 -- Phase 0 design-system foundation

**Done:**
- New `ui/theme/Spacing.kt` (`HadithSpacing`: xs/sm/md/lg/xl/xxl/section, §0.1).
- New `ui/components/Surfaces.kt`: `HadithCard` (the one card language: `surfaceSolid` fill, 1dp
  `border`, 16dp radius, no elevation; `selected` draws `accentSoft` fill + accent-tinted border),
  `GroupedList` (a zero-padding `HadithCard`), `ListRow`, `RowDivider`, `ChevronTrailing`,
  `ExternalTrailing`.
- New `ui/components/SectionHeader.kt`: `SectionHeader` (eyebrow/title/lede/action) and
  `ScreenHero` (the hairline-eyebrow-title-subline block, extracted so Reader/Bookmarks/About stop
  each carrying their own copy -- wiring happens in Steps 29/31/33).
- New `ui/components/Actions.kt`: `ActionTone`, `HadithPrimaryButton`, `SecondaryAction` (with
  `liveLabel` for the "Copied" live-region text and `iconRotation` for the expand chevron),
  `TertiaryLink`.
- New `ui/components/Shimmer.kt`: `shimmerColor()` moved out of `ReaderScreen.kt` unchanged
  (now `internal`, in `ui.components`), so it isn't duplicated by `ReferenceSummary`.
  `ReaderScreen.kt` now imports it; its old private copy and the now-unused
  `FastOutSlowInEasing`/`RepeatMode`/`lerp` imports were removed.
- New `ui/components/ReferenceSummary.kt`: the unified reference block (collection · number +
  status pill, chapter + Sunnah link, both rows height-stable across loading/loaded/empty) used
  by the Reader dock (Step 29) and saved-hadith cards (Step 32).
- New `ui/components/NewFolderField.kt`: the collapsed "+ New folder" `TertiaryLink` that expands
  into a text field + Create button, used by the Save sheet (Step 30) and Bookmarks root (Step 31).
- `ui/theme/Theme.kt`: `HadithTheme` now passes `typography = interMaterialTypography(inter)` to
  `MaterialTheme` -- every one of the 15 Material `Typography` roles gets `fontFamily = inter`,
  fixing plain `Text` (buttons, dialogs, nav labels, menus) silently rendering in Roboto. Both
  colour schemes also gain `surfaceContainer`, `secondaryContainer`, `onSecondaryContainer`,
  `surfaceVariant`, `onSurfaceVariant` mapped onto the existing Hadith tokens, so M3's default
  lavender/grey no longer shows through in menus or the nav indicator (wired in Step 28).

No screen is wired to any of this yet -- Steps 28-33 do that. This step only adds the shared
building blocks and confirms they compile.

**Commands run:**
- First `assembleDebug`: FAILED -- `ReferenceSummary.kt` called `TertiaryLink(...) { ... }` with a
  trailing lambda, but `TertiaryLink`'s `onClick` parameter isn't last (it's second, before
  `modifier`), so Kotlin couldn't match it as a trailing lambda. Fixed by passing `onClick =` by
  name instead of a trailing lambda.
- `JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug lintDebug testDebugUnitTest` → BUILD SUCCESSFUL in 1m 24s.

**Acceptance:**
- Compiles: PASS
- `lintDebug`: PASS (0 error-severity findings; SARIF report shows only pre-existing Correctness-category informational hits)
- `testDebugUnitTest`: PASS, 105/105 (unchanged -- this step adds no new logic, only Compose UI)

**Unspecified choices:** none -- this step is pure scaffolding, no literal-value judgment calls.

**NOT TESTED:** nothing yet exercises these components on screen (no wiring this step).

**Blockers/questions:** none.

### Step 28 -- Shell: lighter top bars, theme long-press menu, themed nav

**Done:**
- `ui/components/TopBar.kt`: `HadithTopBar` and `HadithBackTopBar` drop the bottom-hairline
  `drawBehind`; both get `heightIn(min = 56.dp)`. `HadithTopBar`'s title drops from 17sp SemiBold
  to 16sp Medium. `ThemeToggleButton` is now a 48dp `combinedClickable` circle: `onClick` still
  flips light/dark via `onToggleTheme` (unchanged behaviour); `onLongClick` opens a `DropdownMenu`
  with "Match system" / "Light" / "Dark", a check icon on whichever matches `theme`, calling
  `onSetTheme`. Icon tint is now `colors.textSoft` at 20dp (was `colors.text`, default size).
  `HadithTopBar` gained `theme: Theme` and `onSetTheme: (Theme) -> Unit` parameters.
- `ui/nav/AppNav.kt`: `AppNav` gained `onSetTheme: (Theme) -> Unit`, threaded into `HadithTopBar`.
  The bottom nav is wrapped in a `Column` with a 1dp `border` hairline above `NavigationBar`
  (`containerColor = bg @ 0.94, tonalElevation = 0.dp`). The three `NavigationBarItem`s are
  factored into one `TabItem(tab, selected, onClick)` `RowScope` composable, themed with
  `NavigationBarItemDefaults.colors(selectedIconColor = accentInk, selectedTextColor = text,
  indicatorColor = accentSoft, unselectedIconColor/unselectedTextColor = muted @ 0.70)`; icons are
  22dp, labels 12sp (SemiBold when selected, else Medium). `AboutRoute`'s call site drops the
  `theme = settings.theme` argument.
- `MainActivity.kt`: passes `onSetTheme = { newTheme -> scope.launch { container.settingsRepository.setTheme(newTheme) } }` to `AppNav`.
- `ui/about/AboutScreen.kt` (§A4/R3-Q1): deleted the Appearance `GroupPanel` block and the private
  `ThemeSegmentedControl` composable entirely. `AboutRoute`/`AboutScreen` no longer take
  `theme`/`onSetTheme`. Removed the now-orphaned `Theme` and `Color` imports.

**Commands run:**
- `JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug lintDebug testDebugUnitTest` → BUILD SUCCESSFUL in 28s, first attempt.

**Acceptance:**
- Compiles: PASS
- `lintDebug`: PASS
- `testDebugUnitTest`: PASS, 105/105

**Unspecified choices:** none beyond what the spec already pinned down literally.

**NOT TESTED:** the long-press gesture itself (opens the menu, doesn't also fire the single-tap
toggle) is Compose `combinedClickable` behaviour with no JVM-testable surface -- per this project's
convention, this is a device-pass item for the user.

**Blockers/questions:** none.

### Step 29 -- Reader: attribution removed, shared design system wired in

**Done:**
- §A2: deleted `AttributionLine`, the `item("attrib")` list entry, and the `onOpenAttribution`
  parameter from `ReaderScreen`, `ReaderContentList` and `ReaderRoute`'s call site. "Texts via
  Hadith API" no longer appears in the Reader (it stays in About's Sources section).
- Hero replaced with the shared `ScreenHero("HADITH OF THE MOMENT", "Read. Reflect. Remember.",
  typography.heroTitle, subline)`; the private `Hero` composable is deleted. Rhythm now uses
  `HadithSpacing.xl`/`.xxl` at the top-level gaps the spec named.
- `ScriptToggle` + `TextSizeChip` collapsed into one `ReadingModeControl`: a single 40dp segmented
  pill (`surfaceSolid` fill, 1dp `border`), script segments only rendered when
  `expanded && hasArabicWorthShowing` (unchanged condition), a 1dp `borderStrong` divider, then the
  "Aa" + text-size label segment (label hidden when the script segments are showing, so the pill
  doesn't get crowded). `ReadingBar` is now a thin wrapper that always centres the control -- the
  old ≥720dp End-alignment branch is gone, since the control's own internal layout is unaffected by
  width.
- The dock's reference block is now `HadithCard { ReferenceSummary(...) }`, replacing the two
  hand-rolled Row A/Row B blocks; the private `SunnahLink` composable is deleted (`ReferenceSummary`
  owns the link, including the height-stable alpha-0 fallback).
- `PrimaryButton` now renders through `HadithPrimaryButton`, keeping its existing label/spinner
  logic as the `leadingIcon` slot's rotation.
- `QuietActionsRow` now renders three `SecondaryAction`s (`Arrangement.SpaceEvenly`, min 44dp
  height) instead of `QuietAction`/`Divider14`, which are deleted. R3-Q3: Copy is now a local
  `copied` state that shows "Copied" + a check icon in `ActionTone.Accent` for 2000ms
  (`LaunchedEffect(copied) { delay(2000); copied = false }`), reset immediately on `hadith?.key`
  changing so a new draw doesn't carry over a stale "Copied" state; `liveLabel = true` marks it as
  an accessibility live region. Save/Share keep their existing enabled/accent logic through the
  same component.
- `FullReference`'s card fill changed from the tinted `refBg` to the shared `surfaceSolid` +
  16dp-padding language; its "REFERENCE" header now uses `typography.sectionLabel` instead of a
  literal 11.5sp Bold.
- `ReaderRoute.copyToClipboard` dropped its parameter list to just `(context, hadith)` -- the
  `Build.VERSION.SDK_INT <= S_V2` branch, the toast, and the `Build` import are gone, since the
  inline "Copied" state above is the only confirmation now, on every API level.

**Commands run:**
- First `assembleDebug`: FAILED -- `Unresolved reference 'HadithSpacing'` (used in
  `ReaderScreen.kt` before importing it from `ui.theme`). Fixed by adding the import.
- `JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug lintDebug testDebugUnitTest` → BUILD SUCCESSFUL in 23s.

**Acceptance:**
- Compiles: PASS
- `lintDebug`: PASS (0 error-severity findings)
- `testDebugUnitTest`: PASS, 105/105

**Unspecified choices:** none -- every value here traces to a literal in the spec.

**NOT TESTED:** the "Copied" 2000ms revert timing and the ReadingModeControl's segmented-pill
layout at various widths are Compose-only behaviour with no JVM-testable surface -- device-pass
items for the user, per this project's convention.

**Blockers/questions:** none.

### Step 30 -- Save sheet: card folder rows, NewFolderField, Done (R3-Q2)

**Done:**
- `ModalBottomSheet` now takes `sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)`,
  `containerColor = colors.bg`, and an explicit `dragHandle = { BottomSheetDefaults.DragHandle(width = 36.dp, height = 4.dp, color = colors.borderStrong) }`
  (the sheet already had M3's default handle; this makes it explicit and on-brand instead of relying
  on the default).
- `SaveSheetFolderRow` is now a `HadithCard(selected = checked, ...)` -- accentSoft fill + an
  accent-tinted border when the hadith is already saved there, replacing the old flat
  `bg`/`accentSoft` background with a plain 1dp border. Inside: a 40dp leading tile (accentSoft
  fill + accent `bookmark` icon when unchecked; accent fill + `bg`-tinted `bookmarkFilled` icon when
  checked), the name + count column, and a 22dp trailing check circle (accent fill + a 13dp check
  glyph when checked; a 1.5dp borderStrong ring when not). The row's tap-to-save behaviour and its
  "Saved to {name}"/"Removed from {name}" toasts are unchanged (R3-Q2).
- The always-visible `OutlinedTextField` + "Create" `Row` is replaced by the shared
  `NewFolderField` component: collapsed, it's a "+ New folder" tertiary link; tapping it expands
  into the text field (same 40-char cap, same `submitCreate()` logic) with a Create button.
  `creatingFolder` collapses back to the link on a successful `Created`/`Existing` result.
- A `close(then: () -> Unit = {})` local function hides the sheet (`sheetState.hide()`) before
  calling `onDismiss()` and then `then()`, so the sheet visibly slides away instead of vanishing.
  The primary action is now `HadithPrimaryButton("Done") { close() }` -- it makes no save/remove
  decision of its own (every folder tap already committed one), it just closes the sheet, which is
  R3-Q2's tap-to-save-plus-Done model. "Manage all bookmarks →" becomes
  `TertiaryLink("Manage all bookmarks", trailingIcon = a chevron, onClick = { close(onManageBookmarks) })`
  -- the literal "→" glyph is dropped in favour of the icon, per the spec.

**Commands run:**
- `JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug lintDebug testDebugUnitTest` → BUILD SUCCESSFUL in 19s, first attempt (some fully-qualified references from the initial edit -- `PaddingValues`, `RoundedCornerShape`, `FontWeight.Medium`, `Color.Transparent` -- were cleaned up to proper imports via `sed` before this build, so the compiler never actually saw them; noting it since it's a style cleanup, not a compile fix).

**Acceptance:**
- Compiles: PASS
- `lintDebug`: PASS (0 error-severity findings)
- `testDebugUnitTest`: PASS, 105/105

**Unspecified choices:** none -- every value traces to the spec's literal table.

**NOT TESTED:** the sheet's hide-then-dismiss animation on Done/Manage-all-bookmarks, and
`NewFolderField`'s autofocus-on-expand, are Compose-only behaviour -- device-pass items for the
user, per this project's convention.

**Blockers/questions:** none.

### Step 31 -- Bookmarks root: LazyColumn, shared header/list, Backup section

**Done:**
- **Real bug fixed** (found during Round 3's audit, not in the user's original list): `FoldersScreen`
  was a plain `Column(fillMaxSize())` with no scroll modifier at all. With more than roughly a
  screenful of folders, the Library entry point (and anything after it) was pushed off-screen and
  unreachable -- almost certainly the actual root cause of the "export/import shown poorly"
  complaint, on top of the visual issue the spec called out. The root is now a `LazyColumn`, and
  every section is capped at `widthIn(max = 720.dp)`.
- Hero replaced with `ScreenHero("YOUR LIBRARY", "Bookmarks", typography.pageTitle, subline)`.
- "Folders" gets a `SectionHeader`. The always-visible `OutlinedTextField` + "Create folder" button
  row is replaced by `NewFolderField`: collapsed, it renders as the header's trailing `action`
  slot (a "+ New folder" tertiary link); once tapped, the header's action becomes `null` and a
  full-width expanded `NewFolderField` renders below it instead. The `Existing`/`Created` toast
  behaviour and the 40-char cap are unchanged; `creatingFolder` collapses back to the link on a
  successful create.
- The non-empty folder list is now a `GroupedList` of the existing `FolderRow`s (unchanged content)
  separated by `RowDivider()`, replacing the hand-rolled bordered `Column`. The chevron is now
  `ChevronTrailing()` instead of a literal `Icon`. The empty state is a `HadithCard` (48dp
  accent-soft tile with a bookmark glyph, "No folders yet", a helper line, and a
  `HadithPrimaryButton("Read a Hadith")`) instead of the old translucent `HadithShapes.lg` panel
  with a stock `Button`.
- The Library entry point moves under a new "Backup" `SectionHeader` (with a lede) and becomes a
  `GroupedList` of three `ListRow`s -- "Export to a file", "Share library file", "Import a library
  file", each with a subtitle and a `ChevronTrailing()` -- replacing the single expand-to-reveal row
  of bare `TextButton`s, which was the other half of the "shown poorly" bug. `LibrarySection` gained
  a `hasFolders: Boolean` param; Export and Share now call a local `requireFolders { }` guard that
  toasts "No bookmarks to export yet." instead of writing/sharing an empty file when there are no
  folders. Import now checks `previewImport(doc).itemCount == 0` after a successful parse and shows
  a new "Nothing to import" `AlertDialog` instead of the normal confirm-then-apply flow in that case.

**Commands run:**
- `JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug lintDebug testDebugUnitTest` → BUILD SUCCESSFUL in 21s, first attempt.

**Acceptance:**
- Compiles: PASS
- `lintDebug`: PASS (0 error-severity findings)
- `testDebugUnitTest`: PASS, 105/105

**Unspecified choices:** none -- every value traces to the spec.

**NOT TESTED:** the LazyColumn scroll fix and the collapsed/expanded NewFolderField transition are
Compose layout behaviour -- device-pass items for the user. This step is the one most worth a
second look on the user's own device pass, since it fixes an actual functional bug (no scroll) that
predates this round's visual work.

**Blockers/questions:** none.

### Step 32 -- Folder detail: header block, overflow actions, card language

**Done:**
- §A1 verified: `FolderScreen.kt`'s Share-folder action (upload icon in the top bar, calling
  `LibraryExport.buildDocument(repo, folderId)` -> cache file -> `ACTION_SEND` chooser) already
  worked exactly as R0.6 describes. It is kept, unchanged in behaviour, moved to sit beside the new
  overflow menu instead of beside separate pencil/bin buttons.
- Top bar: `HadithBackTopBar(title = "", ...)` -- the folder name moves into the body's own header
  block. The pencil and bin `IconButton`s are replaced by one `MoreVert` icon opening a
  `DropdownMenu` with "Rename" and "Delete" (Delete in `colors.error`), matching the
  Bookmarks-root `FolderRow` pattern.
- `FolderDetailBody` gained a `folderName` param and a new header `item`: "FOLDER" eyebrow
  (`sectionLabel`/`accentInk`), the name as `pageTitle` (2 lines max, ellipsised), then the count
  line -- since the name is no longer shown in the top bar. The item list moved from a nested nested
  `LazyColumn` (odd, since the outer `Column` already wasn't lazy) to items directly in the one
  outer `LazyColumn`; a `Spacer(32.dp)` trailing item replaces the old `8.dp` one. The empty state is
  now a `HadithCard` ("Nothing saved here yet." + a helper line) instead of a translucent
  `HadithShapes.lg` box.
- `BookmarkItemCard` is now a `HadithCard`. Its own hand-rolled ref-row + status-pill `Row` is
  replaced by the shared `ReferenceSummary` (which also now surfaces the "View on Sunnah.com" link
  here -- previously only in the footer's `FlowRow`). The 26dp rounded/bordered `Column` wrapper is
  gone (the `HadithCard` supplies it).
- The footer changed from a `FlowRow` of six-plus bare `FooterAction` text buttons to a
  `SecondaryAction` row: "Show full"/"Show less" (only when `canExpand`, with a 350ms-rotating
  chevron via `SecondaryAction`'s new `iconRotation` param), then a `Spacer(weight 1f)`, then Copy
  (R3-Q3: local `copied` state -> "Copied" + check in `ActionTone.Accent` for 2000ms, replacing the
  old `copyToClipboard`'s `Build.VERSION.SDK_INT <= S_V2` toast branch entirely), Share, and one
  `MoreVert` overflow `IconButton` opening a `DropdownMenu` with "Move to…" (only when
  `otherFolders.isNotEmpty()`, itself opening the existing folder-picker `DropdownMenu`) and "Remove
  from folder" (`colors.error`) -- both keeping their existing toast/removal logic. `FooterAction`
  is deleted; `copyToClipboard` drops its `toastState` parameter.

**Commands run:**
- `JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug lintDebug testDebugUnitTest` → BUILD SUCCESSFUL in 21s, first attempt.

**Acceptance:**
- Compiles: PASS
- `lintDebug`: PASS (0 error-severity findings)
- `testDebugUnitTest`: PASS, 105/105

**Unspecified choices:** none -- every value traces to the spec.

**NOT TESTED:** the two nested `DropdownMenu`s sharing one `Box` (overflow, then the Move-to
sub-list) render correctly one after the other rather than overlapping -- Compose layout behaviour,
device-pass item for the user.

**Blockers/questions:** none.

### Step 33 -- About: sections via SectionHeader, chip card, quiet footer

**Done:**
- Hero replaced with `ScreenHero("HADITH PULL", "About", typography.pageTitle, subline = null)`;
  the lede stays a plain centred `Text` below it, wording unchanged.
- All five sections (About the app, Typography, Sources, Contact, Legal) now open with a shared
  `SectionHeader` instead of the old mix of `ProseSection` (free text), `GroupPanel` + `PanelTitle`
  (Appearance/Say salam/Legal). All three of those private composables, plus `SectionDivider`, the
  old private `LinkRow` and `RowDivider`, are deleted in favour of the shared components from Step
  27.
- "Typography" (renamed from the old "A note on the Arabic") gets new, shorter copy per the spec --
  two paragraphs describing the type choices as a design note, replacing the old
  font-licensing-flavoured text about the Indo-Pak Mushaf orthography's redistribution terms. This
  is the one new piece of copy in this step, written out verbatim in the spec.
- The Sources section's collection-chip `FlowRow` moves inside a `HadithCard` with a "COLLECTIONS"
  label above it, instead of sitting loose directly under the paragraph. `SourceChip`'s fill changes
  from `colors.surfaceSolid` to `colors.bg` (keeping its 1dp `border`), per the spec's literal chip
  values.
- Contact and Legal are now flat `GroupedList`s (Instagram/LinkedIn/GitHub with `ExternalTrailing()`;
  Privacy policy/Open-source licenses with `ChevronTrailing()`) instead of a `GroupPanel` (a 26dp
  bordered box) wrapping its own separately bordered 16dp `Column` -- this removes the
  card-inside-a-card nesting the Round 3 audit flagged (R0's "found" note on About's Say
  salam/Legal sections).
- The footer is now three centred lines (app name, version, "No accounts, no ads, no tracking.")
  instead of the old three lines that duplicated attribution already shown in Sources ("Hadith Pull,
  built for quiet reading." / "Texts via Hadith API" / version).
- Content capped at `widthIn(max = 720.dp)` throughout, matching Bookmarks' width discipline; side
  padding follows the same `16/20dp` width-based rule as the other screens.

**Commands run:**
- `JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug lintDebug testDebugUnitTest` → BUILD SUCCESSFUL in 19s, first attempt.

**Acceptance:**
- Compiles: PASS
- `lintDebug`: PASS (0 error-severity findings)
- `testDebugUnitTest`: PASS, 105/105

**Unspecified choices:** the "Typography" section's exact wording is new copy this step
introduces (per the spec's explicit instruction), not an em-dash fix or literal port of existing
text -- flagged here as the spec asked, though the wording itself was locked in the spec, not
invented by this step.

**NOT TESTED:** none beyond the usual Compose-layout caveat -- this step is static content and
layout, with no new interactive behaviour to flag.

**Blockers/questions:** none.

### Step 34 -- Final check and wrap-up

**Done:** ran the full build matrix and the orphan-check grep from the spec.

**Commands run:**
- `JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug assembleRelease lintDebug testDebugUnitTest` → BUILD SUCCESSFUL in 2m 9s (release build, including R8 minification and resource shrinking, is clean).
- `grep -rn "Texts via Hadith API|ThemeSegmentedControl|AttributionLine|FooterAction|Divider14" app/src/main/java` → the only hit is `ShareSheet.kt`'s own, unrelated `ThemeSegmentedControl` (the share-card's Light/Dark toggle, a different composable with a coincidentally identical name, untouched by this round and out of scope). "Texts via Hadith API", `AttributionLine`, `FooterAction` and `Divider14` all return zero hits.

**Acceptance:**
- `lintDebug`: PASS (0 error-severity findings across all 8 steps)
- `assembleDebug` + `assembleRelease`: PASS
- `testDebugUnitTest`: PASS, 105/105 (unchanged from before this round -- Round 3 is Compose UI only, adding no new pure-logic surface to test)

**Final report lists anything NOT TESTED:** every step's Compose-only behaviour (the theme
long-press menu, "Copied" inline confirmations and their 2000ms revert timing, the Reading Mode
segmented control at various widths, the Save sheet's hide-then-dismiss animation, the Bookmarks
LazyColumn scroll fix, the two-level DropdownMenu in Folder detail's overflow) has no JVM-testable
surface in this project and is a device-pass item for the user, consistent with every step's own
report above.

## 2026-09-26 -- Read screen and app icon follow-up

**Read screen:**
- Removed the large inline `Aa Comfortable` reading-size control. A compact 17 sp `Aa` action now sits beside the theme icon in the top bar; both actions have centered 44 dp touch targets.
- Added a reading appearance bottom sheet for the existing `TextSize` choices. It updates the narration through the existing `SettingsRepository` and persists the selection without a second preference system.
- Kept `HADITH OF THE MOMENT` and `Read. Reflect. Remember.` fixed above the scrolling narration, per the user's revised direction. The compact top bar remains fixed, and the reference/action dock remains fixed on normal-height screens.
- Made the Naskh/Clear/Bold Arabic script selector smaller and available while Arabic is expanded. Changed Show Arabic / Show less to a slimmer outlined disclosure with a 44 dp tap area.
- Kept the current Hadith mounted during expansion so Arabic can animate in place. Arabic text, script selector, expanded reference, and narration height now use coordinated 900 ms opening and closing transitions. The New Hadith reveal remains in place.

**App icon and startup:**
- Replaced the legacy density and round launcher icons with the supplied `logos and favicons/hadith-pull-logo_app.png`.
- Added `drawable-nodpi/hadith_pull_adaptive_icon.png`, a padded adaptive/splash asset. Its center contains the supplied 512 x 512 image pixel-for-pixel; the padding keeps the smaller Ha and source margins visible within Android's icon mask.
- Updated adaptive icon XML and both startup themes to use the padded asset. The splash icon backdrop matches the image's cream color.

**Validation and release state:**
- `:app:assembleDebug` and `git diff --check` pass after the icon correction. `:app:lintDebug` passed during the Read-screen changes. No new automated tests were added.
- Installed the APK on the emulator and visually checked the Read screen in dark and light modes, Arabic expansion, header actions, and the corrected app-drawer icon. Pixel Launcher's suggested-app slot can retain an older cached thumbnail; the installed app-drawer icon shows the corrected image.
- The user explicitly requested this session's commit and push. The release workflow in `.github/workflows/release.yml` is manual-only and was not started; more features are planned before release.

## 2026-09-26 -- Save, Share, About, and toast refinements

**Save to a folder sheet:**
- Tightened the title, supporting text, empty state, folder rows, and action spacing while keeping the existing sheet structure and behavior.
- Kept New Folder as a compact teal text action. Made the visible Done pill slimmer within an accessible touch target, and kept Manage all bookmarks visually tertiary.

**Share sheet:**
- Reduced the visible Share the image and Save to device pills and the social icon surfaces while retaining comfortable touch targets. Tightened the controls below the image preview and moved Include Arabic to the right of its row.
- Instagram now attempts a direct image share to Instagram instead of silently saving the image when Instagram cannot open. An unavailable message guides the user to Share the image. The emulator does not have Instagram installed, so the Instagram composer itself was not verified.
- Copy image now confirms a successful copy with a temporary checkmark, consistent with the Read screen's copy feedback.

**About and feedback:**
- Kept Contact focused on its message action. Moved GitHub, LinkedIn, and Instagram into a small, centered footer row directly below Version 1.2, preserving their links and touch targets.
- Reduced the shared in-app toast's maximum width, padding, and text scale so Instagram and similar messages appear as smaller overlays.

**Validation:**
- Built and installed the debug app on the emulator and relaunched it after the UI changes. Visually checked the Save sheet, Share sheet, About footer, copy confirmation, and Instagram-unavailable message.
- `:app:testDebugUnitTest --offline` and `git diff --check` passed. No new automated tests were added.

## 2026-09-26 -- Read-screen grade filter

- Added a compact upward filter menu to the existing grade pill. The pill continues to show the current Hadith's stored grade and retains the existing grade colors.
- Added `Sahih only`, `Other grades`, and `All grades`; the selection persists through the existing DataStore settings. Filtered draws select from cached candidate pools built from the dataset's stored primary categories. `Other grades` includes Hasan, Daif, and stored unknown categories, while ungraded narrations remain available under `All grades`.
- Added draw-engine coverage for all three filter modes.

**Validation:** `assembleDebug`, `testDebugUnitTest`, and `git diff --check` passed. Installed and opened the app on the emulator; the menu opened above the pill and showed the selected filter while the pill retained its actual grade. The user reviewed the running app and requested commit and push.
