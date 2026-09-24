# Releasing

## What the workflow does

`.github/workflows/release.yml` builds a signed `app-release.aab`, ready to
upload to Play Console. It never runs on its own — no push or tag trigger —
because an accidental automatic release is worse than a manual step.

**To trigger it:** GitHub → this repo → Actions tab → "Release build" →
"Run workflow" → pick the branch → Run workflow.

It checks out the repo, sets up JDK 21 (Temurin), decodes the keystore secret
to a temp file, runs `testDebugUnitTest` and `lintDebug` (the workflow fails
here if either fails — there's no point signing a broken build), then runs
`bundleRelease` with the signing config pointed at the decoded keystore.

**Where the artifact ends up:** the finished run's page, under "Artifacts",
as `app-release.aab`. Download it from there and upload it to Play Console
yourself — the workflow does not talk to Play Console.

## One-time setup: GitHub secrets

Add these under this repo's Settings → Secrets and variables → Actions →
"New repository secret". The workflow reads them; nothing in this repo or
this session sets them for you.

| Secret name | Value |
|---|---|
| `KEYSTORE_BASE64` | Your release keystore file, base64-encoded (see below) |
| `KEYSTORE_PASSWORD` | The keystore's password |
| `KEY_ALIAS` | The key alias inside the keystore |
| `KEY_PASSWORD` | That key's password |

**To base64-encode your keystore file**, run this on your machine (Git Bash
or WSL — PowerShell's `certutil` also works if you prefer, ask if you need
that variant instead):

```bash
base64 -w0 /path/to/your-release-key.jks > keystore-base64.txt
```

Paste the contents of `keystore-base64.txt` as the `KEYSTORE_BASE64` secret's
value, then delete that file locally — it's a plaintext copy of your signing
key.

## Local release builds

`app/build.gradle.kts`'s signing config reads `KEYSTORE_PATH` /
`KEYSTORE_PASSWORD` / `KEY_ALIAS` / `KEY_PASSWORD` from environment variables
first; if any of those aren't set, it falls back to these keys in your
gitignored `local.properties`:

```properties
RELEASE_KEYSTORE_PATH=C:\\path\\to\\your-release-key.jks
RELEASE_KEYSTORE_PASSWORD=...
RELEASE_KEY_ALIAS=...
RELEASE_KEY_PASSWORD=...
```

If neither the environment variables nor `local.properties` have a complete
set, `./gradlew bundleRelease` / `assembleRelease` still runs, just unsigned
(the same state the project was in before this signing config existed).
