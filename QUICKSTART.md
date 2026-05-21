# 🚀 ClawChives Mobile Quickstart

Welcome to the full build and deployment guide for compiling the ClawChives Android companion securely on your local machine.

---

## Prerequisites

- **Android Studio** (Koala or newer recommended).
- **Java Development Kit (JDK) 17+**.
- An Android Device (running Android 8.0+) or an Android Virtual Device (AVD).

---

## Step 1: Clone the Repository

Clone the ClawChives Mobile client to your local workspace:

```bash
git clone https://github.com/ClawStack-Studios/clawchives-mobile.git
cd clawchives-mobile
```

## Step 2: Gradle Initialization

Open Android Studio and map to the downloaded directory. Gradle will natively fetch the Compose toolchains, Ktor dependencies, and Kotlin Serialization libraries.

_Note:_ Do not interrupt the Gradle sync. Verify your `gradle.properties` matches standard Jetpack mappings.

## Step 3: Network Config Binding

If you are pointing your Android application to a local development server (e.g. `http://192.168.1.150:4646`), ensure your `network_security_config.xml` allows traffic to private LAN IP ranges. If you are using Cloudflare Tunnels (`https://.*`), no extra configurations are needed.

## Step 4: Compiling

Execute the standard Android compilation:

```bash
./gradlew assembleDebug
```

Alternatively, tap the `"Run App"` icon explicitly in the Android Studio top action bar.

## Troubleshooting

- **400 Bad Request Errors:** Verify your payload mapped correctly via the `sanitize()` methods from the `Models.kt` classes. You must match the Zod constraints running on the server.
- **Unauthorized (401) Errors:** Ensure the initial Human Key input is being sent through the `ClawCrypto.hashHumanKey()` algorithm successfully before transmission.

---
*Maintained by CrustAgent©™*
