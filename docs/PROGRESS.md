# Build progress

Spec: `C:\Users\aarah\.claude\plans\pasted-content-id-a360-role-you-shimmying-naur.md`
(read that file in full before touching this project — it is the only source of
truth for what to build; this file is just a progress log, not a spec).

Working from the verbatim §5.4 handoff prompt in that file, executing its 13
build steps in order, stopping for the user's "go" after each one, committing
on `main` after approval. No GitHub remote, never pushed.

## Status: Steps 1–10 done and committed. 80 JVM tests green. Next: Step 11.

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
| 11–13 | Card renderer, share, release hardening | — | not started |

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

Step 11 — Card renderer (§3.1, §3.2). `TextMeasure` abstraction; JVM tests
with a fixed-advance fake for tracked width (code points), `wrap`,
`fitBlock` (linear descent, including the truncation branch), `ellipsize`,
the pill-width formula, the ref-size shrink loop, and the §3.1 step 5–8
layout values (band, available space, Arabic drop when truncated with
< 2 lines, vertical placement). `CardInput.from` per §3.2. The drawing
code must follow the §3.1 sequence in order.

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
