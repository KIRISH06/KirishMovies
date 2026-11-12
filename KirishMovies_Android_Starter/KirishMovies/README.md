# Kirish Movies — Android starter

A minimal dark-theme Android movie catalog using Internet Archive's public read APIs.
It is intentionally independent of MovieBox and does not connect to MovieBox servers.

## Current features
- Search Internet Archive movie records with a `licenseurl` field.
- Fetch item metadata and show the uploader-provided license/rights text.
- Play or download an MP4 when one is available.
- Minimal dark Compose UI and bottom navigation shell.

## Important rights note
Internet Archive says rights information can be uploader-provided and may not be guaranteed accurate. This starter only filters for records carrying a license URL/rights field; it does **not** independently validate the license or grant redistribution rights. Before publishing, inspect each item's license and follow its conditions. Do not add copyrighted films without permission.

## Build
- Android Studio Ladybug or newer
- JDK 17
- Android SDK 35
- Gradle 8.7 / Android Gradle Plugin 8.6.1

Open this folder in Android Studio and allow Gradle sync. Then run `app`.

## GitHub Actions
The workflow at `.github/workflows/android.yml` builds a debug APK on push or manual run. Download the APK from the workflow run's Artifacts section.

## Current limitations / next development steps
- Poster artwork and real category feeds are not yet implemented.
- Downloads tab is currently a UI placeholder; downloads are sent to Android's Download Manager.
- The Archive API may return incomplete or incorrectly labelled rights metadata. Review each item before distribution.
- Add a proper app icon, privacy policy, error states, pagination, and tests before Play Store release.
