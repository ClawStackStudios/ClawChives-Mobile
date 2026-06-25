---
roadmap_version: 0.0.4.1
last_updated: 2026-06-24
current_position: "Phase 1: [Phase Name] — Sprint 1.1: [Sprint Name]"
statistics:
description: "[Insert a 1-2 sentence high-level objective of the project/repository here.]"
features_completed: "░░░░░░░░░░ 0%"
features_in_progress: "░░░░░░░░░░ 0%"
---


```text
       ______ __                 ______  __     _                     
      / ____// /____ _ _      __/ ____/ / /_   (_) _   __ ___   _____
     / /    / // __ `/| | /| /// /     / __ \ / //  | / // _ \ / ___/
    / /___ / // /_/ / | |/ |/ // /___ / / / // / | |/ //  __/(__  ) 
    \____//_/ \__,_/  |__/|__/ \____//_/ /_//_/  |___/ \___//____/  
            __  __       __    _ __                                  
           /  |/  /___  / /_  (_) /__                                
          / /|_/ / __ \/ __ \/ / / _ \                               
         / /  / / /_/ / /_/ / / /  __/                               
        /_/  /_/\____/\____/_/_/\___/                                
                                                                
                ClawStack Mobile Studios©™

```

---

### THE 4 INVARIABLES (Always Apply)

| Question                    | Maps To                  | Why It Matters                  |
|----------------------------|--------------------------|---------------------------------|
| Where does state live?     | Ownership & truth        | Consistency, blast radius       |
| Where does feedback live?  | Observability            | Debugging, monitoring           |
| What breaks if I delete this? | Coupling & fragility  | Safe refactoring                |
| When does timing work?     | Async & ordering         | Race conditions, correctness    |

---

# Master Project Roadmap

Systemic Design Rule: This roadmap uses a deterministic 3-Phase structure. Each Phase contains exactly 6 features/tasks, divided evenly across 2 collapsible Sprints (3 tasks per Sprint).

------------------ Current Position ------------------

## Phase 1: Sovereign Settings Menu implementation. Tasks are to be completed one at a time, not completing more than a single task at a time. 
Use Android native libraries and languages (eg. Jetpack Compose, Kotlin, etc) Always map both sides of the bridge before crossing, check code base routes and calls for presence & logical consistensy. Always confirm routes exist and are not fabricated or missing. Build the floor before the ceiling.

> Phase Feature Set Overview:
Sovereign Settings Menu

- [X]  Task 01: Initial Sidebar & First Setting menu view 'Profile'

Description: Develop The Settings Menu Sidebar & Settings Menu faithfully re-creating the ClawChives production settings menu visually. The ClawChives Mobile Settings menu should be indistinguishable from the ClawChives Web Applications settings menu. as ClawChives mobile is a full, visually faithful re-creation of the clawchives web application User Interface. 
Add a user profile feature to the ClawChives companion app. Users should be able to set a display name and upload a custom avatar. This will be stored on the server associated with their unique user ID.

> Success Criteria: A functional settings menu skeleton, with working sidebar selections [Sidebar Top: Profile, Appearance, Lobster Keys, Import / Export, Back to Dashboard, Database Stats, Claw Out - Sidebar Bottom] and a navigateable 'Profile' view from the sidebar (Only the 'Profile' selection will have an view for this task, the sidebar entries will be added to the sidebar. but the views will be in subsequent tasks in the phase) 
All Functions and variables in the provided screenshot when assuming this task on user request must be faithfully recreated and functional. 

- [X]  Task 02: Implement Push Notifications

Description: Implement a toast notification system to display errors or connectivity feedback when communication with the ClawChives server fails.

> Success Criteria: [Clear, binary conditions that determine when this task can be marked complete.]

- [ ]  Task 03: Implement Offline Mode Tggle on login form activity.

Description: Develop an offline mode for the ClawChives React Native companion app. Implement local storage (e.g., using AsyncStorage or a local SQLite database) to cache pinchmark data. Add a synchronization mechanism to push local changes to the server and pull server updates when an internet connection is available.
develop setting on main login form for turning offline mode on or off. only a single mode can be chosen before login.

> Success Criteria: [Clear, binary conditions that determine when this task can be marked complete.]

- [ ]  Task 04: Implement Server Connectivity Visual Indicator

Description: Create a visual indicator component in the UI that displays the real-time connectivity status to the SERVER_URL defined in the environment variables.

> Success Criteria: A visual indicator is present on the Dashboard (and/or relevant screens) that clearly reflects the current connectivity state to the backend API.

- [ ]  Task 05: Implement Periodic Server Health Ping

Description: Implement a periodic background task that pings the server at the configured URL to check for uptime and display a toast notification if the server becomes unreachable.

> Success Criteria: Background task periodically pings the configured SERVER_URL for health status. If the server is unreachable, a toast notification is displayed to the user.

- [X]  Task 06: Settings Repository Pattern Abstraction

**Description:** Implement a clean Repository pattern in Kotlin to abstract your Room DAO database operations for application settings (such as the server URL and user preferences). This repository acts as an architectural boundary, exposing data to your ViewModels exclusively via Kotlin `Flow`. By wrapping the Room database layer, it ensures the rest of the application interacts with a stable, decoupled API and prevents database queries from running on the main UI thread.

> **Success Criteria:**
> * A `SettingsRepository` interface and its concrete implementation `RoomSettingsRepository` are successfully created.
> * Application settings are exposed as an asynchronous, cold Kotlin `Flow` stream, allowing Compose components to observe state changes reactively.
> * No direct references to the Room database or Room DAOs exist within the ViewModel or UI layers.
> 
> 

- [X]  Task 07: Server URL Input Validation & Jetpack Compose UI Integration

**Description:** Add robust client-side input validation logic to the server URL entry field within your Jetpack Compose UI before the data is passed to the repository and saved. Use Kotlin’s native regex or Android's `URLUtil.isValidUrl()` to verify that the entry conforms to a valid scheme (`http://` or `https://`) and contains a valid domain name or IP address. If validation fails, intercept the save action, prevent the database write, and display an explicit error message on the Compose text field.

> **Success Criteria:**
> * The UI prevents the user from clicking "Save" or executing a database write if the text field contains an malformed, blank, or invalid URL structure.
> * Entering an invalid URL automatically triggers an error state on the Compose `OutlinedTextField`, displaying a clear visual error message.
> * Submitting a syntactically correct URL successfully clears the error state and passes the validated string to the repository for persistence.
> 
>

- [ ]  Task 08: Implement Connectivity Status History View

Description: Add a small history view that records the timestamp of the last five connectivity status changes for troubleshooting purposes.

> Success Criteria: The UI includes a history view displaying the timestamps of the last five connectivity state changes.

## Phase 2: [Phase Name - e.g., Integration & Mid-tier Logic]

> Phase Feature Set Overview:
[Provide a brief 2-3 sentence overview of what this phase achieves, its primary boundaries, and what success looks like upon completion.]

- [ ]  Task 07: Implement Automatic Server Reconnection Retry

Description: Implement an automatic retry mechanism for the ping task that attempts to reconnect to the server every 30 seconds after a connection failure is detected.

> Success Criteria: Background ping task automatically retries connecting to the server every 30 seconds after detecting a connection failure.

- [ ]  Task 08: [Task Name]

Description: [Detailed explanation of what needs to be built, the technical requirements, and how to verify it.]

> Success Criteria: [Clear, binary conditions that determine when this task can be marked complete.]

- [ ]  Task 09: [Task Name]

Description: [Detailed explanation of what needs to be built, the technical requirements, and how to verify it.]

> Success Criteria: [Clear, binary conditions that determine when this task can be marked complete.]

- [ ]  Task 10: [Task Name]

Description: [Detailed explanation of what needs to be built, the technical requirements, and how to verify it.]

> Success Criteria: [Clear, binary conditions that determine when this task can be marked complete.]

- [ ]  Task 11: [Task Name]

Description: [Detailed explanation of what needs to be built, the technical requirements, and how to verify it.]

> Success Criteria: [Clear, binary conditions that determine when this task can be marked complete.]

- [ ]  Task 12: [Task Name]

Description: [Detailed explanation of what needs to be built, the technical requirements, and how to verify it.]

> Success Criteria: [Clear, binary conditions that determine when this task can be marked complete.]

## Phase 3: [Phase Name - e.g., Interface Polish & Optimization]

> Phase Feature Set Overview:
[Provide a brief 2-3 sentence overview of what this phase achieves, its primary boundaries, and what success looks like upon completion.]

- [ ]  Task 13: [Task Name]

Description: [Detailed explanation of what needs to be built, the technical requirements, and how to verify it.]

> Success Criteria: [Clear, binary conditions that determine when this task can be marked complete.]

- [ ]  Task 14: [Task Name]

Description: [Detailed explanation of what needs to be built, the technical requirements, and how to verify it.]

> Success Criteria: [Clear, binary conditions that determine when this task can be marked complete.]

- [ ]  Task 15: [Task Name]

Description: [Detailed explanation of what needs to be built, the technical requirements, and how to verify it.]

> Success Criteria: [Clear, binary conditions that determine when this task can be marked complete.]

- [ ]  Task 16: [Task Name]

Description: [Detailed explanation of what needs to be built, the technical requirements, and how to verify it.]

> Success Criteria: [Clear, binary conditions that determine when this task can be marked complete.]

- [ ]  Task 17: [Task Name]

Description: [Detailed explanation of what needs to be built, the technical requirements, and how to verify it.]

> Success Criteria: [Clear, binary conditions that determine when this task can be marked complete.]

- [ ]  Task 18: [Task Name]

Description: [Detailed explanation of what needs to be built, the technical requirements, and how to verify it.]

> Success Criteria: [Clear, binary conditions that determine when this task can be marked complete.]

---

# Phase: Android Native Secure Offline-First Data Synchronization

---

### - [ ] Task 01: Secure Metadata Storage via DataStore + Tink

**Description:**
Implement the secure metadata layer using Jetpack DataStore and the modern `androidx.datastore:datastore-tink` artifact. Store synchronization metadata—including the LobsterKey-derived session tokens, server salts, and the `last_synced_at` timestamp—using a Proto DataStore instance wrapped by an `AeadSerializer`. Generate the master key inside the hardware-backed Android Keystore system.

> Success Criteria:
> * Sync metadata is written and read asynchronously via Kotlin `Flow` without blocking the main thread.
> * Modifying the underlying file from a rooted device shell triggers a cryptographic verification failure, preventing corrupted sync requests.
> 
> 

---

### - [ ] Task 02: Room Database & State-Tracking Architecture

**Description:**
Configure the local Room Database to act as the single source of truth for Jetpack Compose. Implement two core tables: `bookmarks` (for localized display) and an isolated `outbox` table to track offline changes. When a user mutates a bookmark offline, append an operation record to the `outbox` table, complete with a unique `client_mutation_id` (UUID), an operational type enum (`CREATE`, `UPDATE`, `DELETE`), and a payload string.

```kotlin
@Entity(tableName = "outbox")
data class OutboxEntry(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val operation: String, // CREATE, UPDATE, DELETE
    val payload: String,   // JSON representation of the change
    val retryCount: Int = 0,
    val maxRetries: Int = 5,
    val nextRetryAt: Long = System.currentTimeMillis()
)

```

> Success Criteria:
> * Changes made while offline instantly update the local `bookmarks` table to reflect in Compose under 50ms, while creating a corresponding `outbox` record.
> 
> 

---

### - [ ] Task 03: WorkManager Engine with Resilient Exponential Backoff

**Description:**
Build a native `CoroutineWorker` driven by Jetpack WorkManager to process the `outbox` table. The worker must query pending records where `nextRetryAt` is less than or equal to the current system time. If a network call fails, use a custom exponential backoff calculation to update the `retryCount` and `nextRetryAt` fields directly inside an isolated Room transaction block, letting WorkManager automatically reschedule the task.

> Success Criteria:
> * If the ClawChives web server is unreachable, the client backs off smoothly, stopping after 5 failed attempts without locking up database threads.
> * A successful connection clears the `outbox` table completely and updates the secure DataStore sync timestamp.
> 
> 

---

### - [ ] Task 04: Server-Salted Delta Reconciliation & Conflict Resolution

**Description:**
When the sync worker connects to the server, transmit the outbox array alongside the current session token. The server will validate the payload against its own LobsterKey records. Receive the server's update delta package and apply changes locally using a **Last-Write-Wins (LWW)** approach. Perform all insert, delete, and update operations inside a single Room `@Transaction` block to keep data consistent across the app.

> Success Criteria:
> * The sync worker cleanly processes incoming modifications without generating duplicate local records or causing UI flickering in Compose.
> * If a bookmark is deleted on the server, the local record is wiped out, and any matching pending changes in the local outbox are safely cleared.
> 
> 

---

### - [ ] Task 05: Network Boundary Hardening (Certificate Pinning)

**Description:**
Secure all communications between the Android client and your self-hosted ClawChives server against Man-in-the-Middle (MITM) attacks. Create a native `network_security_config.xml` file that disables cleartext traffic and pins your server's SHA-256 certificate digest directly at the operating system level. Attach this configuration to the application manifest.

```xml
<!-- res/xml/network_security_config.xml -->
<network-security-config>
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">your-clawchives-domain.com</domain>
        <pin-set>
            <pin digest="SHA-256">BASE64_ENCODED_SERVER_PUBLIC_KEY_PIN</pin>
        </pin-set>
    </domain-config>
</network-security-config>

```

> Success Criteria:
> * The application automatically blocks any HTTP cleartext sync traffic.
> * Trying to connect to the server through an untrusted proxy or a modified certificate authority drops the connection instantly, preventing credential leakage.
> 
>

---

---

# Phase: Gotify & UnifiedPush System Notification Pipeline

**Description:** This phase implements an ultra-lightweight, native notification receiver by utilizing Gotify via the open UnifiedPush standard. Instead of maintaining a custom, resource-heavy socket connection, ClawChives Mobile registers as a push consumer. It offloads connection maintenance to the system-level Gotify client, securely routing critical security and agent alerts into native Android notification channels.

---

### - [ ] Task 01: UnifiedPush Consumer Registration & Interface Contract

**Description:** Integrate the lightweight UnifiedPush client library into your native Android app. Create a dedicated broadcast receiver (`ClawChivesPushReceiver`) that extends the UnifiedPush handling class. This class will listen for system intents broadcasted directly by the on-device Gotify application. When your app launches or a user logs in, trigger the registration string to establish an internal system communications link.

```kotlin
class ClawChivesPushReceiver : UnifiedPushReceiver() {
    override fun onMessage(context: Context, message: ByteArray, instance: String) {
        // High-signal payload parsing logic happens here
    }

    override fun onNewEndpoint(context: Context, endpoint: String, instance: String) {
        // Send this unique endpoint URL back to your ClawChives Web Server
    }
}

```

> **Success Criteria:**
> * ClawChives Mobile successfully broadcasts a registration intent and discovers the local Gotify client app.
> * The app receives a unique webhook endpoint URL from UnifiedPush and saves it into the secure Jetpack DataStore.
> 
> 

---

### - [ ] Task 02: Web Server Push Registration & Payload Architecture

**Description:** Update your ClawChives Web Server to store the incoming UnifiedPush/Gotify endpoint URL linked to the user’s authenticated session. When a notable system event occurs (e.g., a new login location detected or a automated agent key nearing expiration), configure the server to POST a structured JSON payload directly to that saved endpoint URL.

> **Success Criteria:**
> * The server successfully captures and stores the mobile application's push endpoint during login synchronization.
> * Triggering an event on the server generates a cleanly formatted POST request to the Gotify/UnifiedPush endpoint without timeout errors.
> 
> 

---

### - [ ] Task 03: Native OS Notification Channel Mapping & Priority Matrix

**Description:** Inside your `ClawChivesPushReceiver`, parse incoming message bytes into structural categories. Establish official Android `NotificationChannel` structures to split these alerts appropriately by operational importance.

| System Event | Payload Priority Marker | Android Channel Importance |
| --- | --- | --- |
| **New Device Login** | `Priority >= 8` | `IMPORTANCE_HIGH` (Heads-up alert banner) |
| **Agent Key Expiration** | `Priority 4 - 7` | `IMPORTANCE_DEFAULT` (Standard shade entry) |
| **System Info / Backups** | `Priority <= 3` | `IMPORTANCE_LOW` (Silent minimal entry) |

> **Success Criteria:**
> * High-priority security payloads slice through system idle states to render an immediate heads-up alert banner on the device.
> * Users can control notification behaviors, sounds, and visual presentation for individual categories via native Android app settings.
> 
> 

---

### - [ ] Task 04: Jetpack Compose Push Preferences & Diagnostic Panel

**Description:** Build a clean settings interface using Jetpack Compose to give users precise control over their push connection. Include an explicit status indicator displaying the active UnifiedPush distributor application name (e.g., "Distributor: Gotify"). Add a "Send Test Notification" button that pings your web server to fire an instantaneous pipeline check, validating the full server-to-mobile communications loop.

> **Success Criteria:**
> * The settings panel dynamically displays whether the app is successfully registered with Gotify.
> * Pressing the test button triggers a complete loop, generating a native Android notification on the device in under 2 seconds over a cellular network.
> 
>

- [ ] Implement a settings page where users can toggle push notifications for specific server events, persisting these preferences locally.

---

## Completed Tasks

- [x] Unify all authentication and sensitive configurations into the local Room Database and eliminate SharedPreferences usage for keys and tokens.

- [x] Persist local dashboard interactions (starred tabs, active folders, selected tags, sort orders) across application restarts.

- [x] Add a slide-in entrance animation for the new settings menu when opened from the sidebar to provide a smoother, more cohesive UI experience.

- [x] Add a fourth option to the theme split button that syncs with the user's OS-level light/dark mode settings, automatically switching themes based on system preferences.

- [x] Implement persistent storage for the selected theme (light, dark, or oled) using localStorage so the user's preference is maintained across sessions and app restarts.

- [x] Add an 'About' section to the new settings menu that displays the app version and connectivity status, using the existing diagnostic functions to verify server parity.

- [x] Implement a toast notification system to display errors or connectivity feedback when communication with the ClawChives server fails.

- [x] Persist the server URL that is entered in the login form locally using Android native Room Library, replacing the SharedPreferences configuration.

- [x] Add folder management capabilities to the ClawChives companion app. Users should be able to create, rename, delete, and move pinchmarks between folders. This will require corresponding API endpoints on the server to handle folder data.


# TESTING IDEAS DO NOT IMPLEMENT

- [ ] Integrate HapticFeedbackConstants into the main interactive buttons (the 'Pinchmark' and 'Pod' actions) to provide subtle, premium physical confirmation for user interactions.

- [ ] Add a persistent status indicator in the header that displays 'Online' or 'Offline' based on a periodic ping to the configured server URL.

- [ ] Implement haptic vibration feedback for the central '+' button and the bottom navigation elements to match standard Android interaction patterns.

- [ ] Implement swipe-to-delete functionality for bookmarks in the dashboard list. When a bookmark is swiped left, reveal a delete button. Tapping the delete button should prompt a confirmation before removing the bookmark.

---

- [ ] Develop The Settings Menu faithfully re-creating the ClawChives production settings menu visually. The ClawChives Mobile Settings menu should be indistinguishable from the ClawChives Web Applications settings menu. as ClawChives mobile is a full, visually faithful re-creation of the clawchives web application User Interface. 

- [ ] Add Biometric authentication as an optional authentication method for users alongside ClawKeys. Users Can add a biometric authentication option that can be setup, enrolled and then used for ClawChives Mobile authentication as a third option in the middle of the 2 current options. (Place a 'BioKey©™ Option in between the 2 current options so its 3 segmented.)

---

- [ ] Implement a search bar on the dashboard of the ClawChives companion app. This search should query all pinchmarks, matching based on URL, title, and tags. Results should be displayed dynamically as the user types.

- [ ] Integrate the Google Search grounding capability to allow users to pull in fresh metadata or status information for their ClawChives server items directly from the web. consider a new button for triggering this 're-fetch' in the edit pinchmark modal activity, or possibly in the 'Gear' button to the right of the Cyan + button (since we already have the correct setting button leading to the settings menu in the sidebar. This gear button i was originally leaving as a placeholder for something exactly like this. We need a way of handling the state of 'Re-fetch ALL pinchmarks? or just granular? ie.single tag, single category. single pinchmark? we would need a possible new menu for selection status? the flow gets complicated. so we need to surface tensions in the coherent, smooth flow that makes sense and grounds in the logical inferred patterns this flow would include.)

## EXPERIMENTAL IDEAS

- [ ] Create a scrollable view that pulls and displays the latest server log lines from the configured API endpoint with auto-scrolling functionality.

- [ ] Create a status dashboard component that displays real-time server health metrics such as latency, uptime, and current load using a Recharts line graph.

- [ ] Build a background service that fetches and synchronizes notes from the configured ClawChives server.

- [ ] Create a Settings UI component using Jetpack Compose that allows users to input and update their server URL, which will then be persisted via the ROOM database.

- [ ] Integrate a secure cloud synchronization feature for ClawChives. Allow users to opt-in to sync their pinchmarks to a chosen cloud storage provider (e.g., S3, Google Drive). Ensure end-to-end encryption for all synced data.

- [ ] Create a local Room-based audit log to record basic user session durations and action timestamps, helping to refine the 'graceful exit' logic by understanding usage patterns.

- [ ] Implement a network connectivity monitor using ConnectivityManager to trigger a 'Sync Status' indicator in the dashboard, enabling offline read-only mode for cached data when the server is unreachable.