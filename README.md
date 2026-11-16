# মানবসেবা ফাউন্ডেশন (Manobseba Foundation Android)

Native Android application built with **Kotlin**, **Jetpack Compose (Material Design 3)**, **Firebase Firestore**, **Firebase Auth (Credential Manager Google Sign-In)**, and **Firebase AI Logic (`gemini-3.8-flash` / `gemini-3.5-flash-lite`)**.

## Core Modules

- **Home & Live Transparency Dashboard**: Real-time net balance, collection goal progress, active relief projects, urgent blood requests, announcements, daily prayer countdown, and Open-Meteo local weather.
- **Finance & Transparent Ledger**: Multi-fund income/donation and expense tracking, monthly member fees, quick donation sheet (bKash/Nagad/Rocket/Bank), category analytics, and verifiable digital receipts.
- **Blood Donor & Urgent Requests**: Filterable 8-group blood donor directory across 64 Bangladesh districts, emergency hospital blood appeals, fulfillment toggling, and one-tap dial actions.
- **Islamic Tools & Weather**: Solar prayer schedule for all 64 districts of Bangladesh (Hanafi/Shafi Asr), Hijri calendar, Qibla bearing compass, digital Tasbih, curated Bangla Hadith, Surah reader (`api.alquran.cloud`), daily Amol journal, and real-time Open-Meteo weather + AQI.
- **Manobseba AI Hub**: Streaming Bengali/English financial & humanitarian assistant backed by Firebase AI Logic, interactive action confirmation drafts, live financial audit reports, multimodal handwritten ledger OCR (`PickVisualMedia` zero-permission photo picker), and legacy member list parser.
- **Community, Governance & Security**: Projects, verified field reports, role-based members directory, events with RSVP, committee polls, team chat room, notices, 7 Material 3 Bengali themes with dark mode, and optional SHA-256 PIN + Biometric (`BiometricPrompt`) App Lock.

## Building & Downloading the APK (GitHub Actions)

1. In GitHub, open **Settings → Secrets and variables → Actions → New repository secret** and create **`GOOGLE_SERVICES_JSON`** containing the JSON from `app/google-services.json` (excluded automatically on GitHub push for security).
2. Push this repository to GitHub (triggers `.github/workflows/build-apk.yml` automatically on any push or via **Actions → Build Android APK → Run workflow**).
3. Open the **Actions** tab in your GitHub repository and select the latest workflow run.
4. Download **`manobseba-foundation-debug-apk`** from the **Artifacts** section to get `app-debug.apk`.

