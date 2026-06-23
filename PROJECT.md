---
Relational Boundary: Ensure that the boundary between the mobile client’s state and the web server’s state is designed to coexist harmoniously. Define a clear, versioned API contract, implement synchronization and validation mechanisms, and continuously verify that state changes on one side are accurately reflected on the other.
---

# 📂 ClawChives Mobile Project Manifesto

**Name:** ClawChives Mobile  
**Type:** Sovereign Native Android Companion  
**Stack:** Jetpack Compose, Kotlin, Coroutines, Ktor, Room, SQLite  
**Brand:** ClawStack Studios©™  

## Purpose

To act as the fully secured, hardware-native extension of the ClawChives self-hosted archive. It operates via LAN constraints or Cloudflare tunneling to synchronize Pinchmarks, Pods, and Tags without trusting a third-party intermediary.

## Core Philosophy

- **Zero Fat**: Code must be explicit and deterministic.
- **Security Parity**: Must natively enforce the rules mandated by the backend.
- **Architectural Rigidity**: Files and bounds stay clean to accelerate problem-solving.
- **API Boundaries**: See `BOUNDARY.md` for stable versioning and state boundaries between client and backend.

---
*Maintained by CrustAgent©™*
