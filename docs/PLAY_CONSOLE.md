# Play Console — submission answers

Spec: `C:\Users\aarah\.claude\plans\pasted-content-id-a360-role-you-shimmying-naur.md`,
§7 ("Data source: bundled offline dataset" section). This file is the filled-in version
of that section — answers to paste into the Play Console forms, not new decisions.

## Privacy policy

- **URL:** `https://hadithpull.online/privacy` — live now (`privacy.html` on the
  `data/hadith-api` branch of the web repo; merge/deploy that branch before submitting).
- **In-app copy:** also reachable from the app itself — About tab → "Privacy policy"
  row, above "Open-source licenses". Satisfies Play's "link or text within the app"
  requirement in addition to the store-listing URL field.
- Both copies read identically (`Hadith-Pull-Android/PRIVACY.md` is the source; the
  in-app screen renders `res/raw/privacy_policy.txt`, a plain-text copy of the same
  wording; the web page is `privacy.html`). If you ever edit one, edit all three.

## Data safety form

| Question | Answer |
|---|---|
| Does your app collect or share any of the required user data types? | **No** |
| Does your app collect data? | **No** — skip the rest of the section once this is set; the encryption-in-transit and data-deletion sub-questions don't appear. |
| Third-party SDKs | None — no analytics, no ads, no crash-reporting SDK. |

This is true and verifiable: the app has no `INTERNET` permission (confirmed at
Step 16's G7 check — `./gradlew :app:dependencies` shows no OkHttp, and the merged
manifest has no `android.permission.INTERNET`), no accounts, and every narration is
bundled as an asset.

## App access

All functionality is available without special access — no login wall, no paid tier.
Choose **"All functionality is available without special access."**

## Ads

**No ads.**

## Content rating questionnaire

Category: **Reference material / religious text.** No user-generated content, no
user-to-user interaction, no in-app purchases, no user-generated media upload. This
should land in the lowest rating tier every regional system offers; answer every
question in the questionnaire honestly rather than assuming the tier — Play
recalculates it from your answers, not from this table.

## Target audience and content

**General audience.** The spec's own read: 13+ recommended, since the content is
religious text a young child wouldn't seek out on their own, but nothing in the app
needs an age gate — no chat, no user content, no purchases.

## Store listing

Not yet drafted — the spec (§7) defers full listing copy (short description,
full description, screenshots, feature graphic) to after Step 17's polish pass, so
that screenshots reflect the finished UI rather than a version that's about to change.
**Minimum still needed before you can submit at all:**
- App icon — done (Step 2, adaptive icon from the Hadith Pull logo).
- At least 2 phone screenshots (Play requires a minimum; more is better) — take
  these after Step 17, on the same emulator device used for the release check
  (`Medium_Phone_API_36.0`), in both light and dark theme.
- Feature graphic (1024×500) — not started. Needs actual design work, not just text;
  flag when you're ready for this and it'll get its own pass.
- Short description (≤80 characters) and full description (≤4000 characters) — draft
  on request once Step 17 is done and the exact feature set is final; describing an
  app that's still changing risks the listing going stale before it's even published.

## Package name registration

**⚠ Deadline was 2026-09-30 per the spec's Policy Coverage note (req. 3.4).**
Register `online.hadithpull.app` in Play Console as early as possible — this is
independent of the rest of the listing and doesn't need the app to be finished.

## Content licensing note

The bundled dataset (`fawazahmed0/hadith-api`) is Unlicense (public domain
equivalent). The English translations inside it originate from published
translations by named scholars/translators; crediting "Hadith API (fawazahmed0)" in
the store listing description is the honest thing to do, not a legal requirement —
this isn't legal advice, and if you want a firmer answer on translation copyright,
that's a question for someone qualified, not for this file.

## What's still open before a real submission

1. Merge/deploy the `data/hadith-api` web branch (privacy.html only exists there
   right now, not on the live site).
2. Steps 15–17 (UI polish, release/G7 offline check, the polish pass) — the app
   itself isn't feature-complete yet.
3. Feature graphic + screenshots + descriptions (above).
4. Package name registration (above) — this one has no dependency on the rest and
   can happen today.
