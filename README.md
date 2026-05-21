---
Brand: ClawStack Studios©™
Project: ClawChives Mobile©™
---

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
[![License: AGPL-3.0](https://img.shields.io/badge/License-AGPL--3.0-yellow.svg?style=for-the-badge)](LICENSE)

**A sovereign, secure, native Android companion application for your self-hosted ClawChives server.**

[Quickstart](QUICKSTART.md) • [Architecture](ARCHITECTURE.md) • [Security](SECURITY.md) • [Contributing](CONTRIBUTING.md)

</div>

---

## 📜 Table of Contents

<details>
<summary>Unfurl the Scroll 📜</summary>

- [Overview](#-overview)
- [Current API Routes & Integrity](#-current-api-routes--integrity)
- [Building the Android App Manually](#-building-the-android-app-manually)
- [Codebase Structure](#-codebase-structure)
- [Security Assurances](#-security-assurances)

</details>

---

## 🦞 Overview

**ClawChives Mobile** is the official native companion application for your self-hosted ClawChives vault. Built completely in modern **Kotlin** and **Jetpack Compose**, it connects to your sovereign backend via local LAN configurations or secure Cloudflare Tunnels to manage your pinchmarks, pods, and tags on the go.

The client is built with a strict offline-first mindset, leveraging local state synchronization, paginated lazy loads, and premium Material 3 liquid-metal styling guidelines matching your desktop client perfectly.

- 📱 **Sovereign UI Parity** — Implements matching color palettes, dynamic status counter badges, custom canvas-drawn pod indicators, and real-time dashboard searches.
- 🔐 **ShellCryption©™ Auth Handshake** — Connects using the secure `hu-` or `lb-` key protocol without exposing plaintext secrets over the wire.
- ⚡ **Infinite Scroll Bedrock** — High-performance pagination dynamically handles massive collections of over 800+ pinchmarks without lag.
- 🛡️ **Encrypted Cache** — Persists ephemeral API access tokens inside secure, native hardware-backed storage wrappers.

---

## 📱 Current API Routes & Integrity

ClawChives Mobile queries the unified API endpoints defined in the core ClawChives specifications. Every transaction is sanitized locally using Ktor's content negotiation engine.

| HTTP Method | Route Path | Auth Required | Description |
|:---:|:---|:---:|:---|
| `GET` | `/api/health` | No | Connectivity, ping, and gateway health telemetry. |
| `POST` | `/api/auth/token` | No | Authenticaton gateway. Validates locally pre-hashed keys. |
| `GET` | `/api/bookmarks` | Yes | Fetches paginated pinchmarks. Supports queries, stars, and archives. |
| `POST` | `/api/bookmarks` | Yes | Commits a new pinchmark to the vault database. |
| `PUT` | `/api/bookmarks/{id}` | Yes | Edits existing pinchmark parameters (URLs, Titles, Pods, Tags). |
| `DELETE`| `/api/bookmarks/{id}` | Yes | Removes a pinchmark permanently from the database. |
| `GET` | `/api/bookmarks/stats` | Yes | Real-time counts of Starred, Archived, and Total items. |
| `GET` | `/api/bookmarks/tags` | Yes | Retrieves unique active tags for drawer filtering. |
| `GET` | `/api/folders` | Yes | Gathers the list of active user Pods (folders). |
| `POST` | `/api/folders` | Yes | Generates a new Pod with customizable colors. |

---

## 🚀 Building the Android App Manually

> [!NOTE]
> For the comprehensive compilation and configuration guidelines, consult the [QUICKSTART.md](QUICKSTART.md).

If you want to pull down the project and compile the APK directly on your workstation or Android Studio:

### 1. Clone the Repository
```bash
git clone https://github.com/ClawStack-Studios/clawchives-mobile.git
cd clawchives-mobile
```

### 2. Import into Android Studio
* Open Android Studio.
* Select **File > Open** and choose the `clawchives-mobile` root folder.
* Allow the IDE to index dependencies and sync the build platform.

### 3. Sync Gradle
* Click **Sync Project with Gradle Files** (the Gradle Elephant icon in the top right menu).
* Ensure you are running JDK 17+ or compile with the bundled Gradle toolchain.

### 4. Enable Local Development Cleartext (LAN only)
If your self-hosted instance resides on an unencrypted local IP address (e.g., `http://192.168.x.x`), ensure you define a `network_security_config.xml` profile permitting cleartext traffic to that domain.

### 5. Build and Install
* Connect your physical Android device with **USB Debugging** enabled, or spin up an emulator.
* Select `app` from the run configurations dropdown, and click the green **Play (Run)** icon.

---

## 🏗️ Codebase Structure

Our codebase strictly adheres to micro-service feature boundaries and clean separation of concerns:

```text
app/src/main/
├── AndroidManifest.xml
└── java/com/example/
    ├── MainApplication.kt
    ├── MainActivity.kt
    ├── ui/
    │   ├── theme/          # Custom M3 palette (RedAccent, CyanAccent, Liquid Metal shades)
    │   └── feature/        # Segregated micro-feature blocks
    │       ├── auth/       # ShellCryption authentication forms
    │       ├── dashboard/  # Drawer nav, pagination list, search, and pods
    │       └── addbookmark/# Form validations and add/edit modal overlays
    ├── data/
    │   └── remote/         # Ktor unified network client, serializable DTO models
    └── security/
        └── ClawCrypto.kt   # Local SHA-256 pre-hashing engine
```

---

## 🛡️ Security Assurances

* **Zero Plaintext Transmission:** The primary `hu-` identity key is never sent across the internet. It is pre-hashed using SHA-256 in `ClawCrypto.kt` before transit, neutralizing intercept vectors.
* **Zod-Safe Kotlin DTOs:** Every outgoing request object strictly validates and aligns with back-end constraints, completely eliminating `400 Bad Request` schema mismatches.
* **Encrypted Storage:** Long-lived credentials and active session tokens are stored using Android's native `EncryptedSharedPreferences` backed by Keystore hardware encryption.

---

<div align="center">

**Happy Pinching! 🦞**

</div>

---
*Maintained by CrustAgent©™*
