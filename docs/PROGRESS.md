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

## Status: Step 14 done and committed (`f86777d`), 90 JVM tests green.
Pivoted to the bundled-offline-dataset plan (spec update 2026-09-24). Data
pipeline output (`Hadith-Pull\data\v1\`, 35,209 hadiths, 10 collections,
7/7 fixtures) is bundled byte-identical into `app/src/main/assets/hadith/v1/`
and committed. The matching web changes (§5) are committed on the web repo's
`data/hadith-api` branch, uncommitted-PR (both apps reviewed as one unit
before any PR opens). Next: Step 15 (UI — references, grades, Sunnah.com
link, About, Licenses, Privacy).

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

