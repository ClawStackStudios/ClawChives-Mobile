# Project State Pattern Corpus
You must keep STATE.md in alignment with the current pattern state of the application. 
This is part of your job. Make it part of your routine:
Routine is built in workflow.

- **Before coding**: Update topology phase (floor/bridge/ceiling) and verified intent. Proactively fetch recent commit history (git log --oneline -10) and surface relevant context to the user.
- **After file changes**: Update blast radius and modified files list. Stage changes but do not commit until logical units are complete. 
- **At session boundary**: Commit final state snapshot and next topological move. Batch related changes into atomic commits using Conventional Commits format. Create release tags when topology phase reaches "ceiling" and user intent indicates release readiness. Propose all commits and tags in prose before execution.
- **Never**: Update STATE.md without first tracing invariants and calibrating confidence. Never commit without user review of the proposed commit message and diff.  
- **Git Hygiene** Keep track of the git hygiene of the repo. Surface when the worktree gets dirty, Surface commit and tag gaps. 

Git Hygiene Mandate: Maintain clean commit history and semantic release tags. Surface commit/tag proposals to the user in prose before executing. Session completion requires STATE.md synchronization AND clean git state.

---

# Application Topology Map

**Topology Phase:** Bridge (Feature expansion and refinement phase)
**Verified Intent:** Stabilize UI state management, enforce stateless UI components, route side-effects cleanly via ViewModels, and implement global notification system (Toast).

## The Four Core Components Mapping

### 1. The UI (Dumb and Stateless)
- **State consumption:** `DashboardScreen` and `GatewayScreen` reactively collect `uiState` (`DashboardState` and `GatewayUiState` respectively) exposed by ViewModels.
- **Side effects:** `ToastState` is provided globally via `CompositionLocalProvider` (`LocalToastState`) at the `MainActivity` root, decoupling notification UI logic from specific screens.
- **Events:** User actions (clicks, inputs) are delegated to ViewModel methods (e.g., `viewModel.addBookmark()`, `viewModel.login()`). No business logic lives in Compose functions.

### 2. The Models (Simple Bundles)
- Located in `data/remote/Models.kt`.
- Data classes only (`Bookmark`, `Folder`, `SessionData`, `UserInfo`, `TokenRequest/Response`).
- Strict separation maintained; no complex domain models or use cases, adhering to the pragmatism principle.

### 3. The Data Layer (The External Gateway)
- **Remote (API):** `ClawChivesClient` serves as the sole gateway for network requests using Ktor. `DiagnosticsService` checks server health.
- **Local (Persistence):** `AppDatabase` (Room) is now the primary source of truth for persistent local data. It houses `AppConfig` (Server URL, Auth Token, Raw Key) and `FilterState` (dashboard tab/folder states). `ThemePreferences` remains in SharedPreferences for immediate synchronous read during pre-Compose startup.
- **Repository:** `AuthRepository` abstracts the orchestration of tokens, remote auth endpoints, and local Room storage, providing a clean API for the ViewModels.

### 4. The ViewModels (The Middlemen)
- **`GatewayViewModel`**: Owns `GatewayUiState`. Orchestrates authentication, connection validation, and navigation triggers.
- **`DashboardViewModel`**: Owns `DashboardState`. Manages pagination, folder navigation, filter states, and bookmark CRUD operations. Coordinates with the Data Layer and updates state for `DashboardScreen` to consume.

## Invariant Tracing

| Invariable | Mapped To | Current State |
|---|---|---|
| **Where does state live?** | `ViewModels` (`_uiState` StateFlows), `ToastState` (CompositionLocal) | Consistent. ViewModels hold single-source-of-truth for screen state. |
| **Where does feedback live?** | `ToastHost`, `DiagnosticsService`, Error UI states | Global toasts overlay the app, ensuring feedback is visible across navigation boundaries. |
| **What breaks if I delete this?** | Modifying `ClawChivesClient` alters all remote fetch structures. Modifying `AuthRepository` disrupts auto-reauth flow. | High blast radius in Data Layer. Low blast radius in stateless UI components. |
| **When does timing work?** | Coroutine scopes in ViewModels, `LaunchedEffect` in `MainActivity` (for 401 unauth) | Race conditions mitigated by single-threaded state updates and Flow collections. |

## Current Known Tensions
- The environment has restrictions on arbitrary shell commands, meaning Git integration cannot be performed automatically by the agent.
- `AppConfig` stores sensitive keys (`rawKey`, `authToken`) without SQLCipher encryption, favoring robust persistence across OS backups over strict local at-rest encryption. This is an accepted tradeoff for the companion app architecture.