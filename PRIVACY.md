# 🦞 ClawChives Mobile — Privacy Policy

**Effective Date:** 2026-06-23

At ClawStack Mobile Studios, we believe that your data is your own. The ClawChives Mobile companion app is designed with architectural fidelity and data sovereignty as its core principles. This Privacy Policy explains how our application handles your information. 

## 1. Sovereign Data Ownership
ClawChives is a client application designed to connect to your own self-hosted or designated ClawChives server. We do not operate a centralized cloud service that aggregates your data. Your data lives exactly where you choose to put it.

## 2. What Information We Collect (And What We Don't)
**We do not collect, harvest, or monetize your personal data.** 
Because ClawChives Mobile is a sovereign client, there are no third-party trackers, no hidden analytics SDKs, and no telemetry systems phoning home to us. 

* **Server Configuration & Authentication:** The app stores your server URL, Session Tokens, and ClawKeys securely on your local device using Android's native `Room` database and `EncryptedSharedPreferences`. 
* **User Content (Pinchmarks, Folders, Profile):** Your bookmarks (Pinchmarks), folder structures, and profile settings are transmitted directly between your mobile device and your configured ClawChives server via HTTPS. 
* **Local Caching:** For offline access and performance, the app caches a synchronized copy of your data locally on your device. This data never leaves your device except to sync with your chosen server.

## 3. Data Transmission
All communication occurs strictly between the ClawChives mobile client and the server URL you explicitly provide during the authentication phase. We do not intercept, route, or proxy your traffic.

## 4. Permissions
The app requests only the permissions absolutely necessary to function:
* **Network Access:** Required to communicate with your ClawChives server.
* **Storage (Optional):** May be requested if you utilize the Import/Export features or upload a custom ClawKey file from your local file system.

## 5. Security Posture
We design security around invariants, not assumptions. Your Authentication tokens and ClawKeys are encrypted at rest using Android's Keystore system. It is your responsibility to ensure that the ClawChives server you connect to is secured (e.g., using TLS/HTTPS) and trusted.

## 6. Changes to this Policy
If our application's architecture changes in a way that affects local data handling, we will update this policy. However, our core philosophy—that you own your data and we do not want it—will never change.

## 7. Contact
If you have questions regarding the application's architecture, security invariants, or privacy practices, please consult the open-source repository or reach out to the project maintainers.
