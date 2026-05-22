---
Brand: ClawStack Studios©™
Project: ClawChives Companion App
Maintained by CrustAgent©™
---

# 🦞 CrustAgent Topology Map: ClawChives Client

## Phase 1: Cartography (The Skeleton)
The foundation of the ClawChives Android companion application has been set up with strict adherence to the CrustCode©™ protocol. The Android app is exclusively a **Dumb Client** interface for an existing self-hosted server based on the README specs provided by Lucas.

### Core Load-Bearing Walls:
1. **`GatewayScreen.kt` (UI Layer)**: The primary authentication boundary. Replicates the setup wizard structure from the main application, routing users logically. Uses the `Server URL` input to define the REST target.
2. **`AuthRepository.kt` (Domain Layer)**: The mediator. Stores the `apiUrl` and Token into local `DataStore` and manages the `ApiClient` lifecycle.
3. **`ApiClient.kt` (Network Layer)**: Powered by Retrofit. Configured with auth interceptors to inject the stateless `api-` token onto every request automatically. Re-initializes targeting the user's sovereign URL.
4. **`DashboardScreen.kt`**: A stateless view that fetches live `/api/bookmarks` directly from the server. No local caching or Room databases. Keep it simple and light.

## Phase 2: Invariants (What Must Always Be True)
1. **Network Isolation**: The `ApiClient` MUST never be called without a valid `serverUrl`.
2. **No Local Monolith**: The client must rely on the network to view the truth. Offline mode is explicitly discarded in favor of simplicity and alignment with server state.
3. **Local-First Compatibility**: The application explicitly allows cleartext traffic over HTTP to support LAN connections (e.g. `http://192.168.1.100`), while seamlessly supporting secure HTTPS boundaries.
4. **Reactive Truth**: Navigation is strictly bound to `onLoginSuccess` callbacks emitted from state streams.

## Phase 3: The Iteration
We are skipping unnecessary heavy-lifting (Room databases) to provide immediate value. The app acts as a remote for the server.

## Phase 4: Performance & Battery Discipline
1. **Lifecycle Awareness**: The app exclusively uses `collectAsStateWithLifecycle()` to pause all UI state collection when backgrounded.
2. **List Rendering Optimization**: The Jetpack Compose UI strictly applies unique identifiers (`key = { it.id }`) for all items in `LazyColumn`s ensuring heavy compositions are skipped.
3. **On-Demand Fetching**: Instead of battery-draining continuous polling routines, we only fetch when the app is actively commanded to fetch (initial launch, manual refresh pulls).
