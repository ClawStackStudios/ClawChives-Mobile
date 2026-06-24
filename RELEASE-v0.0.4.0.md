---

# 🦞 ClawChives — Release v0.0.4.0

## *The Storage Unification Molt*

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

Welcome to **v0.0.4.0** of **ClawChives**! This release unifies our local data persistence into a single robust source of truth. We have completely removed cleartext `SharedPreferences` for sensitive authentication state, migrating everything into our isolated Android Room Database (`AppDatabase`). Additionally, Dashboard filter states are now fully persisted locally, retaining your selected tabs, active tags, and search contexts across application restarts.

---

## 💎 Key Themes & Highlights

### 💾 1. Storage Unification & Security Hardening

Our persistence layer is now unified, testable, and secure.

* **Room Database Consolidation:** Eliminated `AuthPreferences` (`SharedPreferences`) for session tokens and raw keys, storing the unified `AppConfig` locally inside the structured Room Database.
* **Privacy & Security Alignment:** Aligned the data layer with privacy and security mandates (see `SECURITY.md` and `PRIVACY.md`) by exclusively using the robust local sandbox database to store configuration.

### 🗂️ 2. State Persistence

Your workflow should remain unbroken.

* **Dashboard State Retention:** Dashboard interactions (starred tabs, active folders, selected tags, sort orders) are now saved locally using `FilterStateDao`. When returning to the app, your previous view is exactly as you left it.

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
│   [Room Database: AppConfig, FilterState]     │
└───────────────────┬───────────────────────────┘
                    │                            
                    ▼                            
┌───────────────────────────────────────────────┐
│             🌐 [ClawChives Remote]            │
└───────────────────────────────────────────────┘

```

---

## 📋 Commit Ledger (Since `v0.0.3.0`)

* `[commit_hash]` — **refactor:** migrate Auth data from SharedPreferences to Room Database
* `[commit_hash]` — **feat:** implement local state persistence for Dashboard UI configurations
* `[commit_hash]` — **docs:** bump documentation and release notes to `v0.0.4.0`

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
