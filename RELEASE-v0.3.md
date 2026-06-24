---

# 🦞 ClawChives — Release v0.3.0

## *The Personalization & Persistence Molt*

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

---

## 🚀 The Core Summary

Welcome to **v0.3.0** of **ClawChives**! This release introduces robust personalization and foundational persistence upgrades. We have rolled out a comprehensive **Settings Menu** with a responsive slide-in animation, enabled **OS-level Theme Synchronization** with resilient state persistence, integrated a global **Toast Notification System** for streamlined user feedback, and upgraded our local configuration architecture to use the **Room Database** for enduring server configurations.

---

## 💎 Key Themes & Highlights

### 🎨 1. Personalization & Theming

Aesthetics meet user preferences with synchronized system theming.

* **Responsive Settings Menu:** Introduced a sleek Settings Menu triggered via the sidebar, utilizing smooth slide-in/slide-out entrance animations for a cohesive UI experience.
* **OS-Level Theme Sync:** Upgraded the theme selection to a four-option split button (Light, Dark, OLED, System), automatically deferring to device-level Light/Dark mode when System is selected.
* **Theme Persistence:** Integrated robust local storage via SharedPreferences (`ThemePreferences`) to ensure user styling preferences survive application restarts seamlessly.
* **Diagnostics Integration:** Added an 'About' section directly into the Settings Menu displaying app versions alongside real-time connectivity status to the ClawChives server.

### 🔔 2. Observability & User Feedback

Critical feedback should never interrupt the workflow.

* **Global Toast Notification System:** Designed and implemented a reactive global Toast overlay (`ToastState`, `ToastHost`). Warnings and errors (e.g., server connectivity loss) are now surfaced via non-blocking cyan/red floating toasts perfectly anchored above the interaction zones.

### 💾 3. Data Persistence Architecture

Strengthening our persistence layer to lay the groundwork for advanced offline capabilities.

* **Room Database Integration:** Migrated the server URL configuration out of volatile or basic preferences into a dedicated Android Room Database (`ServerConfig`).
* **Architectural Decoupling:** Re-orchestrated the `AuthRepository` to fetch configurations seamlessly via the Room DAO, ensuring the local storage source of truth is robust and testable for future multi-profile expansions.

---

## 🏗️ Architectural Topology Map

```text
┌───────────────────────────────────────────────┐
│              📱 [ClawChives Client / UI]        │
│  ┌──────────────────┐   ┌──────────────────┐  │
│  │ DashboardScreen  │   │   ToastOverlay   │  │
│  │  (Settings Menu) │   │ (Global Context) │  │
│  └────────┬─────────┘   └────────┬─────────┘  │
└───────────┼──────────────────────┼────────────┘
            │  (Theme & Events)    │ (Feedback)
            ▼                      ▼             
┌───────────────────────────────────────────────┐
│     🔄 [ViewModels & State Controllers]       │
│  [ThemePreferences]   [LocalToastState]       │
└───────────────────┬───────────────────────────┘
                    │  (Repository Flow)            
                    ▼                            
┌───────────────────────────────────────────────┐
│            💾 [Local Data Layer]              │
│        [Room Database: ServerConfig]          │
│        [SharedPreferences: Auth Data]         │
└───────────────────┬───────────────────────────┘
                    │                            
                    ▼                            
┌───────────────────────────────────────────────┐
│             🌐 [ClawChives Remote]            │
└───────────────────────────────────────────────┘

```

---

## 📋 Commit Ledger (Since `v0.2.0`)

* `[commit_hash]` — **feat:** implement responsive Settings Menu with slide-in animations
* `[commit_hash]` — **feat:** add OS-level System Theme sync and 4-way theme selector
* `[commit_hash]` — **feat:** persist theme preferences locally using SharedPreferences
* `[commit_hash]` — **feat:** integrate Diagnostics 'About' module into Settings
* `[commit_hash]` — **feat:** build global Toast notification overlay for error visibility
* `[commit_hash]` — **refactor:** migrate Server URL configuration to Room Database
* `[commit_hash]` — **docs:** update STATE.md mapping ViewModels and Data Layer logic

---

## ⚡ Deployment & Upgrade Instructions

### Using Local Dev Mode

To build and install the latest ClawChives APK to your emulator or physical device:

```bash
git pull origin main
./gradlew assembleDebug
```
*(Install the resulting APK located in `app/build/outputs/apk/debug/`)*

---

*Structure is persistence. Prioritize tight topology over perfect context.*

**Maintained by CrustAgent©™ under ClawStack Studios©™.**

---
