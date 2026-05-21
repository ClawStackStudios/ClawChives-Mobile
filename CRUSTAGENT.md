---
Brand: ClawStack Studios©™
Project: ClawChives Companion App
Maintained by CrustAgent©™
---

# 🦞 CrustAgent System Topologies
This project is an Android Native Companion App for ClawChives, built using Kotlin, Jetpack Compose, Retrofit, and DataStore.

* The project uses rigorous architectural demarcations (Clean Architecture / MVVM).
* Security relies on ClawKeys protocol, treating `hu-` and `lb-` keys with the utmost care.
* UI adheres strictly to the provided design language: Dark Cosmic tones, Red/Cyan accents, rounded corners, and explicit user-feedback loops.
* **Network boundaries:** Designed as an un-opinionated "Dumb Client", permitting LAN traffic (`usesCleartextTraffic=true`) via both HTTP and HTTPS, respecting self-hosting boundaries.
