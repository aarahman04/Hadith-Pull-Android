# Build progress

Spec: `C:\Users\aarah\.claude\plans\pasted-content-id-a360-role-you-shimmying-naur.md`
(read that file in full before touching this project — it is the only source of
truth for what to build; this file is just a progress log, not a spec).

Working from the verbatim §5.4 handoff prompt in that file, executing its 13
build steps in order, stopping for the user's "go" after each one, committing
on `main` after approval. No GitHub remote, never pushed.

## Status: Steps 1–5 done. Step 5 awaiting user "go" before commit. Next after that: Step 6.

| Step | What | Commit | Status |
|---|---|---|---|
| 1 | Scaffold (:app, Gradle wrapper, BuildConfig key, manifest, PRIVACY.md) | `2f5dd49` | done |
| — | Master logo files copied in (Step 2 input) | `0fb79dc` | done |
| 2 | Launcher icon — **scripted, not the Studio wizard** (see Deviations) | `9cc453d` | done |
| 3 | Domain text layer (jsTrim, self-contained, excerpts, paragraphize, plainText/shareText/titleCase, grading) | `daaac49` | done |
| 4 | API/DTOs/normalisation/draw engine (§1.1, §1.2, §1.3, §1.5, P1–P3, P5) | `a7d7fb0` | done |
| 5 | Room + DataStore (§1.6, §1.7, G1) | — | done, awaiting "go" |
| 6–13 | HadithRepository+P6, theme/shell, Reader, Bookmarks, About, card renderer, share, release hardening | — | not started |

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

## Next step for whoever picks this up

Step 6 — HadithRepository, AppContainer, P6 (G1, G2, §4.1).
`HadithRepository.draw()` should wrap `DrawEngine.draw()`, writing to
`recent_hadiths` on `Success` and returning a cache-backed fallback on
`Failure` (P6: the fallback excludes the current key, carries which failure
caused it, and never writes `recent_hadiths`). Needs `AppContainer` wiring
the whole stack (HTTP client, DrawEngine, Room database, both repositories)
manually — no Hilt, per the spec's fixed architecture line.
