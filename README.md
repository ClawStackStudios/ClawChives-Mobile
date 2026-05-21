<div align="center">

```text
   ______ __                 ______  __     _                     
  / ____// /____ _ _      __/ ____// /_   (_)_   __ ___   _____
 / /    / // __ `/| | /| /// /    / __ \ / // | / // _ \ / ___/
/ /___ / // /_/ / | |/ |/ // /___ / / / // / | |/ //  __/(__  ) 
\____//_/ \__,_/  |__/|__/ \____//_/ /_//_/  |___/ \___//____/  
                                                                
     __  __     __    _ __                                  
    /  |/  /___/ /_  (_) /__                                
   / /|_/ / __  / __ \/ / / _ \                               
  / /  / / /_/ / /_/ / / /  __/                               
 /_/  /_/\__,_/\____/_/_/\___/                                
                                                                
                ClawStack Mobile Studios©™
```

[![Kotlin](https://img.shields.io/badge/Kotlin-Native-7F52FF?style=for-the-badge&logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Compose-UI-4285F4?style=for-the-badge&logo=android)](https://developer.android.com/jetpack/compose)
[![OWASP](https://img.shields.io/badge/Security-OWASP-black?style=for-the-badge&logo=owasp)](https://owasp.org/)

**A sovereign, secure, native Android companion application for your self-hosted ClawChives server.**

[Quickstart](QUICKSTART.md) • [Architecture](ARCHITECTURE.md) • [Security](SECURITY.md) • [Contributing](CONTRIBUTING.md)

</div>

---

## 🦞 Overview

ClawChives Mobile is the official Android companion app for a self-hosted ClawChives vault. Built completely in **Kotlin** and **Jetpack Compose**, it connects to your sovereign server (via LAN or secure Cloudflare Tunnel) to manage your Pinchmarks, Pods, and Tags. 

It is designed with strict **OWASP Security Protocols**, offline-first capable architecture, and the unbreakable **ClawKeys©™** token protocol.

---

<details>
<summary><h2>📱 Current API Routes & Integrity</h2></summary>
<br/>

ClawChives Android application queries the unified API routes established in the knowledge base. No unverified routes exist.

| HTTP Method | Route Path | Auth Required | Description |
|:---|:---|:---:|:---|
| `GET` | `/api/health` | No | Connectivity & ping check. |
| `POST` | `/api/auth/token` | No | Login. Submits local `keyHash` and receives `api-` token. |
| `GET` | `/api/bookmarks` | Yes | Retrieves all bookmarks. Supports query params & pagination. |
| `POST` | `/api/bookmarks` | Yes | Creates a new bookmark (Pinchmark). |
| `PUT` | `/api/bookmarks/{id}` | Yes | Updates specific bookmark fields. |
| `DELETE`| `/api/bookmarks/{id}` | Yes | Deletes a specific bookmark. |
| `GET` | `/api/bookmarks/stats` | Yes | Fast query for totals (`total`, `starred`, `archived`). |
| `GET` | `/api/bookmarks/tags` | Yes | Returns unique distinct tags. |
| `GET` | `/api/folders` | Yes | Fetches flat array of user folders/pods. |
| `POST` | `/api/folders` | Yes | Creates a new pod. |

</details>

---

<details>
<summary><h2>🚀 Building the Android App Manually (TL;DR)</h2></summary>
<br/>

> For the comprehensive build guide, please see [QUICKSTART.md](QUICKSTART.md).

If you want to pull this down and build the APK yourself in Android Studio:

1. **Clone the repository.**
   ```bash
   git clone https://github.com/ClawStack-Studios/clawchives-mobile.git
   cd clawchives-mobile
   ```

2. **Open in Android Studio.**
   - Open Android Studio.
   - Select **File > Open** and locate the `clawchives-mobile` root folder.

3. **Sync Gradle.**
   - Wait for the initial background indexing to complete.
   - If prompted, click **Sync Project with Gradle Files** (the elephant icon).

4. **Add Network Security (If applicable).**
   - If you are running your server locally (e.g., `http://192.168.x.x`), ensure you have created the `network_security_config.xml` to allow cleartext LAN traffic.

5. **Build and Run.**
   - Connect your Android device via USB (with USB Debugging enabled) or start an Emulator.
   - Click the green **Run (Play)** button in the top toolbar to install the app.

</details>

---

<details>
<summary><h2>🏗️ Codebase Structure</h2></summary>
<br/>

Following strict separation of concerns and micro-service architectural standards:

```text
app/src/main/
├── AndroidManifest.xml
└── java/com/example/
    ├── MainApplication.kt
    ├── MainActivity.kt
    ├── ui/
    │   ├── theme/          # M3 Colors, Typography, Shapes
    │   └── feature/        # Micro-architectural feature folders
    │       ├── auth/
    │       ├── dashboard/  # Pinchmark fetching, UI, pagination
    │       └── addbookmark/# Zod-compliant creation dialogs
    ├── data/
    │   └── remote/         # Ktor unified networking client & models
    └── security/
        └── ClawCrypto.kt   # Local SHA-256 pre-hashing
```
</details>

---

<details>
<summary><h2>🛡️ Security Assurances</h2></summary>
<br/>

- **Cryptographic Handshake:** The master `hu-` identity key is NO LONGER sent over the network. It is hashed locally using SHA-256 in `ClawCrypto.kt`.
- **Zod Compliant:** Native Kotlin serializers sanitize inputs before network transit, matching strict Node.js validation constraints perfectly to avoid HTTP 400 leaks.
- **Encrypted Shared Preferences:** Ephemeral `api-` tokens are persisted natively using Jetpack Encrypted storage.

For full disclosure and methodology, consult [SECURITY.md](SECURITY.md).
</details>

---
*Maintained by CrustAgent©™*
