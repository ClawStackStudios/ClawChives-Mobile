---
roadmap_version: X.X.X
last_updated: 2026-05-17
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

- [ ]  Task 02: Implement Push Notifications

Description: Implement push notification functionality for the ClawChives mobile companion app. Integrate with a service like CaraBase SQL Server or Expo Push Notifications to send alerts for security events (e.g., new logins) and other important system updates.

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

- [ ]  Task 06: Implement Connectivity Status History View

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


# TESTING IDEAS DO NOT IMPLEMENT

- [ ] Implement swipe-to-delete functionality for bookmarks in the dashboard list. When a bookmark is swiped left, reveal a delete button. Tapping the delete button should prompt a confirmation before removing the bookmark.
- [ ] Develop The Settings Menu faithfully re-creating the ClawChives production settings menu visually. The ClawChives Mobile Settings menu should be indistinguishable from the ClawChives Web Applications settings menu. as ClawChives mobile is a full, visually faithful re-creation of the clawchives web application User Interface. 
- [ ] Add a user profile feature to the ClawChives companion app. Users should be able to set a display name and upload a custom avatar. This will be stored on the server associated with their unique user ID.
- [ ] Add Biometric authentication as an optional authentication method for users alongside ClawKeys. Users Can add a biometric authentication option that can be setup, enrolled and then used for ClawChives Mobile authentication as a third option in the middle of the 2 current options. (Place a 'BioKey©™ Option in between the 2 current options so its 3 segmented.)
- [ ] Add folder management capabilities to the ClawChives companion app. Users should be able to create, rename, delete, and move pinchmarks between folders. This will require corresponding API endpoints on the server to handle folder data.
- [ ] Develop an offline mode for the ClawChives React Native app. Implement local storage for pinchmarks and allow users to add, edit, and delete items offline. When the device reconnects to the server, implement a robust synchronization mechanism to update both local and server databases, handling any potential conflicts. Implement a robust synchronization mechanism to merge changes with the server once connectivity is re-established, ensuring data integrity and avoiding conflicts.
- [ ] Implement push notification capabilities for the ClawChives companion app. Configure the app to receive notifications from the server for events like new agent key expirations or important system announcements. Ensure users can manage their notification preferences within the app's settings.
The system should be able to send notifications for events like new agent key generation, successful logins from new devices, or critical system alerts. Consider using a self hosted service first like gotify. Only IF we can support multiple types of backedns like this proposal then should we think about something like Firebase Cloud Messaging (FCM) or OneSignal.
- [ ] Implement a search bar on the dashboard of the ClawChives companion app. This search should query all pinchmarks, matching based on URL, title, and tags. Results should be displayed dynamically as the user types.
- [ ] Implement push notifications for the ClawChives mobile companion app. The server should be able to trigger notifications for events like new AI agent activity or critical system updates. The mobile app should register for push notifications upon login and display them to the user.
- [ ] Implement offline access and synchronization for the ClawChives mobile companion app. The app should cache pinchmark data locally, allowing users to view and make basic edits (like starring or archiving)
- [ ] while offline. Upon re-establishing a connection, these changes should be synced with the server.
- [ ] Add a small visual indicator (like a colored dot) to the bottom navigation bar to signal active server connection status in real-time.
- [ ] Integrate a secure cloud synchronization feature for ClawChives. Allow users to opt-in to sync their pinchmarks to a chosen cloud storage provider (e.g., S3, Google Drive). Ensure end-to-end encryption for all synced data.
- [ ] Implement haptic vibration feedback for the central '+' button and the bottom navigation elements to match standard Android interaction patterns.

- [ ] Integrate the Google Search grounding capability to allow users to pull in fresh metadata or status information for their ClawChives server items directly from the web.