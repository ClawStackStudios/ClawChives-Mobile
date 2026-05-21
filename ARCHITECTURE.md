# 🏗️ ClawChives Mobile Architecture

This document blueprints the structural truth of the ClawChives Mobile application. It maps the seams, the state flows, and the boundaries.

---

## 🏛️ Base Topology (High-Level Android Blueprint)

The application enforces a strict unidirectional data flow and repository pattern. 

```text
┌──────────────────────┐
│   UI Layer (Compose) │  Listens to observable StateFlows. Handles gestures.
└──────────────────────┘
            │ (User Intents)
            ▼
┌──────────────────────┐
│   ViewModel Layer    │  Contains domain logic, pagination, Zod-schema sanitizers.
└──────────────────────┘
            │ (Data Requests)
            ▼
┌──────────────────────┐
│  Data/Network Layer  │  Ktor Client over Coroutines. JSON Serialization.
└──────────────────────┘
            │ (HTTP REST)
            ▼
    [ClawChives Server]
```

---

## 🧩 The Nervous System (Authentication Flow)

ClawChives Mobile uses an Ephemeral Handshake. The master key never leaves the device in plaintext.

```text
┌─────────────────────────┐
│     User Input Key      │ (e.g. hu-xxxxxxxx-xxxx-xxxx)
└─────────────────────────┘
            │
            ▼
┌─────────────────────────┐
│    ClawCrypto SHA-256   │ -> Output: 64-char Hex Hash
└─────────────────────────┘
            │
            ▼
┌─────────────────────────┐
│ POST /api/auth/token    │ (Payload: { type: "human", keyHash: "..." })
└─────────────────────────┘
            │
            ▼
┌─────────────────────────┐
│ Jetpack EncryptedPrefs  │ -> Stores returned "api-xxxx" session token securely.
└─────────────────────────┘
```

---

## ⚠️ Zod Contract Boundaries

The most critical seam in the codebase is the serialization boundary between `Kotlinx.serialization` and the `Node.js Zod` validations.

**Invariant Checkpoint:**
- **No `null` defaults** for non-nullable optional string fields (e.g., `favicon`, `description`). They must output `""` (empty string).
- **Infinite Scroll Telemetry:** The `DashboardViewModel` manages dynamic list telemetry preventing memory bloat by paginating via Coroutines.

---
*Maintained by CrustAgent©™*
