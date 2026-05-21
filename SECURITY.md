# 🛡️ ClawStack Mobile Security Guidelines

All code merged into ClawChives Mobile MUST strictly adhere to the following security principles, guided by OWASP protocols and the **ClawKeys©™** architecture.

---

## 1. The ClawKeys©™ Master Identity Protocol

ClawChives employs a hierarchical credential model:
1. **Master "Human" Keys (`hu-`)**: Represent absolute system sovereignty.
2. **Session API Tokens (`api-`)**: Ephemeral, revocable, short-lived tokens generated from the master identity.

### Invariant Constraint:
The native Android app **NEVER** transits the `hu-` master key over the network in plaintext. All input keys must pass through the `ClawCrypto` singleton (`SHA-256`) generating a cryptographic hash that is exchanged for the `api-` token.

---

## 2. On-Device Storage Hardening

Tokens MUST NEVER be stored in standard Android `SharedPreferences` locally.
Tokens MUST be stored using Jetpack Security's **EncryptedSharedPreferences**, which uses `AndroidKeyStore` to wrap and encrypt values locally.

---

## 3. Network Cleartext Boundary

By default, the Android application does not permit cleartext HTTP traffic.
However, for users hosting ClawChives on a local area network (LAN), an exception is specifically carved into `res/xml/network_security_config.xml`. This configuration whitelist maps ONLY to private IP ranges (`192.168.x.x`, `10.x.x.x`) to ensure traffic bounds are rigid.

---

## 4. Input Sanitization & Serialization Escaping

The API payload is generated strictly via `kotlinx.serialization` mapping against our `Models.kt` classes. All user inputs are natively escaped by the serializer. We utilize extension functions (`BookmarkCreateRequest.sanitize()`) to guarantee compliance with the strictly enforced backend Zod parsing requirements.

---
*Maintained by CrustAgent©™*
