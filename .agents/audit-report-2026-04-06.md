# SmartDash by Shawn Rain - Comprehensive Project Audit Report

**Date:** April 6, 2026  
**Auditor:** Qwen Code AI  
**Scope:** Architecture, Code Quality, BLE Implementation, Security, Build Configuration  

---

## Executive Summary

The SmartDash by Shawn Rain Android project is a well-structured native Android application that successfully implements a BLE-connected dashboard for electric vehicle controller telemetry. The codebase demonstrates strong modern Android development practices (Compose, Coroutines, DataStore) but has several critical areas requiring attention, particularly around testability, BLE reliability, and code organization.

**Overall Health Score: 6.5/10**

| Category | Score | Status |
|----------|-------|--------|
| Architecture | 6/10 | ⚠️ Needs Improvement |
| Code Quality | 6/10 | ⚠️ Needs Improvement |
| BLE Implementation | 7/10 | ✅ Good with Issues |
| Protocol Handling | 8/10 | ✅ Strong |
| Security | 7/10 | ✅ Good with Gaps |
| Build Configuration | 6/10 | ⚠️ Needs Improvement |
| Testability | 3/10 | ❌ Critical |
| Documentation | 8/10 | ✅ Strong |

---

## Table of Contents

1. [Architecture Analysis](#1-architecture-analysis)
2. [Code Quality Issues](#2-code-quality-issues)
3. [BLE Implementation Deep Dive](#3-ble-implementation-deep-dive)
4. [Protocol Handling Architecture](#4-protocol-handling-architecture)
5. [Security Assessment](#5-security-assessment)
6. [Build Configuration Review](#6-build-configuration-review)
7. [UI/Compose Architecture](#7-uicompose-architecture)
8. [Data Layer Analysis](#8-data-layer-analysis)
9. [Critical Issues Summary](#9-critical-issues-summary)
10. [Recommended Action Plan](#10-recommended-action-plan)

---

## 1. Architecture Analysis

### 1.1 Overall Structure ✅ Good

The project follows a clean domain-driven structure:

```
com.shawnrain.habe/
  ├── ble/                    # BLE engine & protocols
  │   ├── BleManager.kt
  │   ├── ProtocolParser.kt
  │   ├── protocols/          # Controller protocols
  │   └── bms/                # BMS protocols
  ├── data/                   # Data layer
  │   ├── SettingsRepository.kt
  │   ├── VehicleProfile.kt
  │   ├── AutoCalibrator.kt
  │   └── gps/                # GPS tracking
  ├── ui/                     # Compose screens
  │   ├── dashboard/
  │   ├── settings/
  │   ├── bms/
  │   └── navigation/         # Shared utilities
  └── debug/                  # Logging
```

**Strengths:**
- Clean separation: BLE, data, and UI layers are well-bounded
- Single-Activity architecture with Compose navigation is modern and appropriate
- Shared utilities (`PredictiveBackMotion`, `DialogWindowEffects`) properly factored out

**Weaknesses:**
- **Typo in directory name:** `ble/bms/protcols/` should be `ble/bms/protocols/`

---

### 1.2 MVVM / State Management ⚠️ Needs Improvement

**Current Approach:** Centralized monolithic `MainViewModel` (2272 lines)

**Strengths:**
- Reactive composition with `combine()` ensures metrics consistency
- `StateFlow.asStateFlow()` used correctly for read-only exposure
- `SharingStarted.WhileSubscribed(5000)` prevents unnecessary computation

**Critical Issues:**

1. **God Object Anti-Pattern:** `MainViewModel.kt` is 2272 lines handling BLE state, ride sessions, energy estimation, GPS calibration, history management, LAN backup, speed tests, poster generation, and more. This violates Single Responsibility Principle.

2. **Mutable Side Effects in Pure Functions:** Internal mutable state (`_tripDistanceMeters`, `_maxSpeed`, `_totalEnergyUsedWh`) mutated as side effects in `calculateVehicleMetrics()`, breaking pure function semantics and making flows non-deterministic.

3. **Dead State:** `isDrivingMode` is defined as `MutableStateFlow(false).asStateFlow()` but never changes — dead code.

---

### 1.3 Dependency Injection ❌ Critical

**Current Approach:** Manual DI only — no framework (Hilt/Koin/Dagger)

```kotlin
// MainViewModel.kt - all dependencies constructed inline
private val bleManager = BleManager(application)
private val bmsBleManager = BleManager(application)
private val settingsRepository = SettingsRepository(application)
val gpsTracker = GpsTracker(application)
private val rideEnergyEstimator = RideEnergyEstimator()
```

**Problems:**
- **Zero testability:** Cannot mock dependencies for unit tests
- **Resource duplication:** Two separate `BleManager` instances (controller + BMS) doubles BLE overhead
- **Tight coupling:** Every dependency is hard-wired to `MainViewModel`
- **Hidden lifecycle:** `AutoCalibrator` receives `viewModelScope` directly, making isolated testing impossible

**Recommendation:** Introduce Hilt or Koin for dependency injection. At minimum, extract an interface layer for all dependencies.

---

### 1.4 Navigation Architecture ✅ Good with Issues

**Strengths:**
- `PredictiveBackMotion.kt` is a well-crafted reusable component with gesture-driven animations
- `DialogWindowEffects.kt` provides blur backdrop with proper animations
- Route requests handled via `SharedFlow` with `DROP_OLDEST` preventing navigation storms

**Issues:**
- Navigation setup embedded directly in `MainActivity.kt` (956 lines) instead of separate navigation graph
- No type-safe route definitions — using raw strings like `Screen.Dashboard.route`

---

## 2. Code Quality Issues

### 2.1 File Size Analysis ⚠️ Large Files

| File | Lines | Issue |
|------|-------|-------|
| `MainViewModel.kt` | 2272 | God object — should be split into multiple ViewModels |
| `SettingsScreen.kt` | 1554 | Too large — should extract sub-components |
| `ZhikeParameterCatalog.kt` | 664 | Acceptable for declarative definitions |
| `DashboardScreen.kt` | 1348 | Too large — should extract card/grid components |
| `MainActivity.kt` | 956 | Mixes lifecycle, PiP, permissions, navigation, and UI |
| `SettingsRepository.kt` | 695 | Handles settings + vehicles + history + backup — should split |

### 2.2 Identified Code Smells

1. **Global Mutable Singletons:**
   - `ProtocolParser` is a singleton object with mutable `activeProtocol` state — hard to test, hidden dependency
   - `AppLogger` singleton is acceptable but limits testability of log output

2. **Hardcoded Values:**
   - Polling interval (100ms) hardcoded in `BleManager`
   - MTU size (20 bytes) hardcoded — should request MTU negotiation
   - Magic numbers in frame parsing without named constants

3. **Error Handling Gaps:**
   - `writeBytes()` silently returns if characteristic is null — caller has no feedback
   - `runCatching { gatt.close() }` silently swallows errors
   - No timeout for service discovery — can hang indefinitely

4. **Memory Leak Risks:**
   - `pollingHandler.postDelayed` recursive calls continue even if app is backgrounded
   - No cancellation mechanism for in-progress packet writes
   - `MainViewModel.onCleared()` does not disconnect BLE — connection may linger

---

## 3. BLE Implementation Deep Dive

### 3.1 Connection Lifecycle ⚠️ Issues Found

**Strengths:**
- Proper state machine with sealed class (`Disconnected`, `Connecting`, `Connected`)
- Calls `discoverServices()` on `STATE_CONNECTED` — correct BLE flow
- Cleans up characteristic references on disconnect

**Critical Issues:**

1. **Asymmetric Disconnect:** `bluetoothGatt?.close()` called before `connect()` without calling `disconnect()` first. The `disconnect()` method only calls `bluetoothGatt?.disconnect()` but never `close()`. This can leave peripherals in inconsistent state.

2. **Race Condition in `connect(address, ...)`:** Method sets `pendingDeviceNameHint` then calls `connect(device)`, which overrides the hint with potentially stale `safeDeviceName()` result.

3. **No Reconnection Logic in BleManager:** If `connectGatt` fails, there's no retry. Auto-reconnect logic lives entirely in `MainViewModel` via watchdog scan — inefficient.

---

### 3.2 Scanning Implementation ⚠️ Needs Improvement

**Issues:**

1. **🔴 No Scan Timeout:** `startScan()` has no timeout. If `stopScan()` is never called (app backgrounded, activity destroyed, exception), scanning continues indefinitely — battery drain risk.

2. **No Scan Filters or Settings:** Uses basic `startScan(callback)` without `ScanFilter` or `ScanSettings`. Every BLE advertisement in range triggers callback. Should use:
   - Service UUID filters
   - `ScanSettings` with appropriate scan mode
   - Callback type filters

3. **Inefficient Deduplication:** Uses `any { it.address == device.address }` — O(n) per scan result. Should use `Set<String>` for O(1) lookup.

4. **No Permission Check:** Unlike `connect()`, `startScan()` does not check `BLUETOOTH_SCAN` permission.

---

### 3.3 Characteristic Read/Write 🔴 Critical Issues

**Strengths:**
- Correctly handles API 33+ vs deprecated pre-33 paths
- Writes respect `PROPERTY_WRITE_NO_RESPONSE` vs `PROPERTY_WRITE`
- Packet-based writing with delays for large Zhike writes

**Critical Issues:**

1. **🔴 CRITICAL: `writeBytes` Silently Drops Writes:** Returns immediately if characteristic is null — caller has zero feedback. Particularly dangerous for Zhike polling loop (`AA13FF01AA130001` every 100ms) — if characteristic not discovered yet, all writes silently dropped.

2. **🔴 CRITICAL: No `onCharacteristicWrite` Callback:** Android BLE stack returns status codes (`GATT_SUCCESS`, `GATT_WRITE_NOT_PERMITTED`, etc.) but this callback is not implemented. Zero visibility into write success/failure. For critical operations like Zhike parameter writes, this is a significant gap.

3. **No MTU Negotiation:** Hardcoded 20-byte MTU. Android supports up to 512 bytes; many modern controllers support 247 bytes. Missing `requestMtu(247)` after service discovery.

4. **Recursive Packet Scheduling:** `writeInPackets` uses `pollingHandler.postDelayed` recursively — continues even if app is backgrounded or connection drops mid-write.

---

### 3.4 Concurrency & Thread Safety ⚠️ Issues

**Current State:** All BLE callbacks run on main looper (no explicit executor passed to `startScan` or `connectGatt`).

**Analysis:**
- ✅ All callbacks serialized on main thread — no concurrent access today
- ⚠️ This is **accidentally safe** but not **explicitly safe**
- ⚠️ `@SuppressLint("MissingPermission")` at class level — hides potential permission issues

**Recommendations:**
- Explicitly specify `ContextCompat.getMainExecutor(context)` for callbacks
- Or use dedicated single-threaded `HandlerThread` for BLE operations
- Document threading model in code comments
- Replace `@SuppressLint("MissingPermission")` with per-call checks

---

### 3.5 Error Handling ⚠️ Incomplete

**Missing Error Handling:**

1. **`onServicesDiscovered` Failure Not Handled:** Only success path processed. If `status != BluetoothGatt.GATT_SUCCESS`, connection sits in `Connected` state with no services.

2. **No `onCharacteristicRead` Failure Handling:** Both read callback overloads log status but don't act on failures.

3. **No Timeout for Service Discovery:** Android's service discovery can hang indefinitely on some devices.

4. **No Retry for Failed Operations:** If read/write fails, no retry logic exists.

---

### 3.6 Cleanup on Disconnect ⚠️ Incomplete

**Strengths:**
- Clears characteristic references on `STATE_DISCONNECTED`
- Calls `stopPolling()` to remove 100ms polling callbacks
- Resets protocol state via `ProtocolParser.reset()`

**Issues:**

1. **`disconnect()` Only Calls `disconnect()`, Not `close()`:** Actual `close()` happens asynchronously in callback when `STATE_DISCONNECTED` fires. If callback never fires (rare but possible), GATT object leaks.

2. **No Notification Disable Before Disconnect:** Does not call `setCharacteristicNotification(char, false)` or write `DISABLE_NOTIFICATION_VALUE` to CCCD before disconnecting.

3. **No ViewModel Cleanup:** `MainViewModel.onCleared()` does not call `bleManager.disconnect()` — connection may linger if ViewModel destroyed.

---

## 4. Protocol Handling Architecture

### 4.1 Strategy Pattern ✅ Strong Design

**Approach:** `ControllerProtocol` interface with scored protocol selection

**Strengths:**
- Clean extensibility — adding protocol requires implementing `ControllerProtocol`
- Scored selection with preferred-protocol memory is robust
- `ZhikeParameterCatalog` (664 lines) is excellently designed:
  - Declarative parameter definitions
  - Bit ranges, scales, types (UINT/INT/FLOAT/BOOL/LIST/TEXT)
  - Presets and firmware version gating
  - Type-safe read/write operations with bit-level manipulation

**Weaknesses:**

1. **Protocol Selection Happens Once:** Only at `onServicesDiscovered`. No runtime re-evaluation if device advertises differently after firmware update.

2. **No Protocol Versioning:** Zhike protocol doesn't track firmware/hardware versions, despite `ZhikeParamDefinition` having `minFirmwareVersion` field.

3. **BMS Protocol Selection Is Name-Based Only:** `BmsParser.selectProtocol` only checks device name — no scoring or service UUID analysis.

---

### 4.2 Zhike Protocol ✅ Most Mature

**Strengths:**
- Frame buffering handles fragmented BLE notifications correctly
- Checksum validation (sum of 32 words == 0xFFFF)
- Sanitization functions filter spurious data (`sanitizeSpeedCandidate`, `sanitizePhaseCurrent`)
- Settings encode/decode with proper little-endian word extraction

**Issues:**

1. **Frame Length Heuristic:** `getFrameLength` for cmd `0x13` checks if `buffer[2]` and `buffer[3]` match 4-byte ACK pattern — fragile if legitimate frame has matching bytes.

2. **Empty List on Checksum Failure:** `extractWords` returns empty list — calling code handles this for most offsets but should be explicit.

---

### 4.3 APT & Yuanqu Protocols ⚠️ Less Mature

**Issues:**

1. **No CRC Validation:** Both protocols skip checksum/CRC with comments "Skipping complex CRC for now to avoid dropping frames on mismatch". Corrupted frames could emit invalid metrics.

2. **YuanquProtocol Uses Mock Mappings:** Comment says "Mock mapping based on typical Far-Drive telemetry" — not production-ready.

---

### 4.4 BMS Protocols ⚠️ Parallel Path

**Issues:**

1. **JkBmsProtocol Uses Magic Offsets:** Offsets like `118 - 4`, `126 - 4` derived from miniprogram source but not well-documented or validated.

2. **Separate from Controller Protocol Strategy:** BMS parsers live in parallel path (`BmsParser`) rather than integrated into same `ControllerProtocol` strategy — inconsistent architecture.

---

## 5. Security Assessment

### 5.1 Secrets Management ✅ Good

**Findings:**
- ✅ No hardcoded secrets, API keys, or passwords in source code
- ✅ Signing credentials loaded from environment variables via macOS Keychain
- ✅ Scripts actively block signing materials from being committed
- ✅ Defense-in-depth: `fail_if_staged_signing_files()` + `warn_if_repo_contains_signing_files()`

**Concerns:**
- ⚠️ `create-user-owned-release-signing.sh` creates plaintext `signing.env` on disk (chmod 600, but still visible if iCloud Drive compromised)
- ⚠️ Keystore Distinguished Name hardcoded as `CN=Shawn Rain, OU=SmartDash by Shawn Rain, O=Shawn Rain, L=Shanghai, ST=Shanghai, C=CN` — reveals personal identity

---

### 5.2 Git Security ✅ Good

**Findings:**
- ✅ `.gitignore` excludes signing materials (`*.jks`, `*.keystore`, `*.p12`, `signing.properties`)
- ✅ Excludes reverse-engineered source (`zhike_source/`, `habe_miniprogram/`)
- ✅ Excludes build artifacts and logs

**Issues:**
- ⚠️ Uses `*.jks` (root-only) instead of `**/*.jks` for recursive matching. Same for `*.keystore`, `*.p12`.

---

### 5.3 Network Security ⚠️ Missing Config

**Findings:**
- ⚠️ No `network_security_config.xml` exists
- ⚠️ App declares `INTERNET` permission but no network security config defined
- ✅ Android 9+ (API 28+) enforces cleartext traffic restrictions by default

**Recommendation:** Add `res/xml/network_security_config.xml` with explicit `<base-config cleartextTrafficPermitted="false">` even if app is BLE-only — documents intent and prevents future mistakes.

---

### 5.4 Release Signing ✅ Good Approach

**Strengths:**
- Uses macOS Keychain for password management
- Conditionally applies signing config — if materials missing, falls back to debug signing
- RSA 4096-bit with 100-year validity appropriate for long-lived personal app

**Concerns:**
- ⚠️ If `releaseStoreFile` is set but passwords are wrong, build fails at signing time rather than configuration time — could provide better error messages
- ⚠️ `debug` buildType not explicitly configured — defaults to Android debug keystore (acceptable but should be explicit)

---

## 6. Build Configuration Review

### 6.1 Root `build.gradle.kts` ⚠️ Needs Update

**Findings:**
- AGP `8.5.0` from mid-2024 — AGP 8.8+ available with security patches
- Kotlin `2.0.0` — Kotlin 2.1.x available
- ❌ No version catalog (`libs.versions.toml`) — all dependencies hardcoded, making updates manual and error-prone

---

### 6.2 App `build.gradle.kts` 🔴 Critical Issues

**🔴 CRITICAL: `isMinifyEnabled = false` on Release Builds**

```kotlin
release {
    isMinifyEnabled = false  // Line 41
    proguardFiles(...)
}
```

**Impact:**
- Release APK ships with all code unobfuscated and unshrunk
- Increased attack surface — full method/class names visible
- Larger APK size
- Referenced `proguard-rules.pro` file **does not exist** — latent defect if minification ever enabled

**Recommendation:** Enable minification for release builds:
```kotlin
release {
    isMinifyEnabled = true
    isShrinkResources = true
    proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
    )
}
```

---

### 6.3 SDK Version Drift ⚠️ Documentation Mismatch

**Issue:** `compileSdk = 36` and `targetSdk = 36` in code, but `AGENTS.md` documents `compileSdk = 35`

**Risk:** Android API 36 (Android 16) is still in preview/beta as of early 2026. The `gradle.properties` has `android.suppressUnsupportedCompileSdk=36` to suppress AGP warnings.

**Recommendation:** 
- Revert to `compileSdk = 35` for production stability
- Or update `AGENTS.md` to document API 36 if intentionally targeting preview

---

### 6.4 Version Management ⚠️ Never Incremented

```kotlin
versionCode = 1  // Line 19
versionName = "1.0"
```

**Issue:** These have never been incremented. For any production delivery, these should be managed properly.

**Recommendation:** Implement automatic version code generation from git commit count or CI build number.

---

### 6.5 Dependency Version Analysis ⚠️ Outdated

| Dependency | Current | Latest Stable | Risk |
|---|---|---|---|
| AGP | 8.5.0 | 8.8+ | Medium |
| Kotlin | 2.0.0 | 2.1.x | Low-Medium |
| Compose BOM | 2024.11.00 | 2025.x | Low |
| Lifecycle | 2.8.7 | 2.9.x | Low |
| Navigation Compose | 2.8.4 | 2.9.x | Low |
| Coroutines | 1.9.0 | 1.10.x | Low |
| CameraX | 1.3.4 | 1.4.x | Low |
| **Accompanist Permissions** | **0.36.0** | **Maintenance Mode** | **Medium** |
| ZXing Core | 3.5.3 | 3.5.3 | None |
| ML Kit Barcode | 17.2.0 | 17.3+ | Low |

**Critical:** Accompanist Permissions is in maintenance mode. Should migrate to AndroidX permission APIs (`androidx.activity:activity-ktx` or `com.google.accompanist:accompanist-permissions` → `androidx.core:core-ktx` permission helpers).

**Note:** No known critical CVEs in current dependency set, but entire stack is 6+ months behind.

---

### 6.6 Dead Dependencies ⚠️ Potential Bloat

**Possibly Unused:**
- `com.google.zxing:core:3.5.3`
- `androidx.camera:*` (camera-core, camera-camera2, camera-lifecycle, camera-view)
- `com.google.mlkit:barcode-scanning:17.2.0`

These suggest QR code / barcode scanning capability, but it's unclear if actively used. Dead dependencies increase APK size and potential attack surface.

**Recommendation:** Audit actual usage. If unused, remove. If used for LAN backup QR codes, document in code comments.

---

## 7. UI/Compose Architecture

### 7.1 Strengths ✅ Excellent

1. **Predictive Back Animation:** `PredictiveBackMotion.kt` provides true gesture-driven animations — scale, translation, alpha tied to back gesture progress.

2. **Blur Backdrop Dialogs:** `DialogWindowEffects.kt` provides `BlurredAlertDialog` and `BlurredModalBottomSheet` with proper blur and animations.

3. **Drag-and-Drop:** Dashboard card reordering with midpoint-swap logic — polished UX.

4. **Landscape Mode:** Dashboard hides bottom nav and focuses on core metrics in landscape.

5. **PiP Mode:** Properly guarded with lifecycle checks and route awareness.

---

### 7.2 Weaknesses ⚠️ Needs Refactoring

1. **No ViewModel-Per-Screen Pattern:** All screens observe single `MainViewModel` — tight coupling between UI screens and monolithic ViewModel.

2. **ViewModel Passed as Parameter:** `MainViewModel` passed directly to all composables rather than using `viewModel()` at each screen's boundary — prevents test isolation.

3. **Oversized Composables:** `DashboardScreen.kt` (1348 lines), `SettingsScreen.kt` (1554 lines) — should extract sub-components into separate files.

4. **Hardcoded Strings:** Some UI strings not extracted to `strings.xml` — limits localization.

---

## 8. Data Layer Analysis

### 8.1 Settings Repository ⚠️ Monolithic

**File:** `SettingsRepository.kt` (695 lines)

**Handles:**
- Settings (wheel circumference, speed source, battery source)
- Vehicle profile CRUD
- Speed test history
- Ride history
- Log level
- Overlay preferences
- Backup/export

**Strengths:**
- Flow-based reactive settings with `flatMapLatest`
- Vehicle-scoped preferences (`vKey(id, key)`) well-designed
- `safeGet()` prevents crashes from corrupted preferences
- Sanitization/coercion on all writes

**Issues:**
- **Single monolithic repository** — should split into:
  - `VehicleRepository` (vehicle profiles)
  - `SettingsRepository` (app settings)
  - `HistoryRepository` (ride/speed test history)
  - `BackupRepository` (import/export)

- **No caching layer** between DataStore and flows — every flow emission triggers full DataStore read

- **Extensive `@OptIn(ExperimentalCoroutinesApi::class)`** for `flatMapLatest` — necessary but adds noise

---

### 8.2 AutoCalibrator ✅ Well Designed

**File:** `AutoCalibrator.kt`

**Strengths:**
- GPS-based wheel calibration with jitter analysis
- Step delta analysis and lag compensation
- Finds stable cruising windows
- Robust suggestion using median of accumulated samples
- Clean state machine with `GpsCalibrationState`

---

### 8.3 Ride Session Tracking ⚠️ Missing Logic

**File:** `RideSession.kt`

**Issue:** Just a simple data class with no session management logic. Actual session tracking lives entirely in `MainViewModel`.

**Recommendation:** Extract session management into dedicated `RideSessionManager` class with start/stop/pause/resume capabilities.

---

## 9. Critical Issues Summary

### 🔴 CRITICAL (Must Fix Before Production)

| # | Issue | Location | Impact |
|---|-------|----------|--------|
| 1 | **No `onCharacteristicWrite` callback** | `BleManager.kt` | Zero visibility into BLE write success/failure — parameter writes may silently fail |
| 2 | **`writeBytes` silently drops writes** | `BleManager.kt` | Caller has no feedback when writes are dropped — polling loop writes silently lost |
| 3 | **No scan timeout** | `BleManager.kt` | Potential battery drain if scanning continues indefinitely |
| 4 | **No ViewModel `onCleared()` cleanup** | `MainViewModel.kt` | BLE connection may linger after ViewModel destroyed |
| 5 | **`isMinifyEnabled = false` on release** | `app/build.gradle.kts` | Unobfuscated APK with increased attack surface |
| 6 | **`proguard-rules.pro` does not exist** | `app/build.gradle.kts` | Latent defect — build will fail if minification enabled |

---

### ⚠️ HIGH PRIORITY (Should Fix Soon)

| # | Issue | Location | Impact |
|---|-------|----------|--------|
| 7 | **Monolithic MainViewModel (2272 lines)** | `MainViewModel.kt` | Hard to maintain, test, or reason about |
| 8 | **No dependency injection framework** | Throughout | Zero testability — cannot mock dependencies |
| 9 | **Two independent BleManager instances** | `MainViewModel.kt` | Doubles BLE scanning/connection overhead |
| 10 | **`onServicesDiscovered` failure not handled** | `BleManager.kt` | Connection may sit in "Connected" state with no services |
| 11 | **No MTU negotiation** | `BleManager.kt` | Slow large writes — stuck at 20-byte MTU |
| 12 | **compileSdk = 36 vs documented 35** | `app/build.gradle.kts` | Documentation drift; API 36 may have breaking changes |
| 13 | **Accompanist Permissions in maintenance mode** | `app/build.gradle.kts` | Should migrate to AndroidX permission APIs |
| 14 | **No permission check in `startScan()`** | `BleManager.kt` | Could crash on Android 12+ without permission |

---

### 💡 MEDIUM PRIORITY (Nice to Have)

| # | Issue | Location | Impact |
|---|-------|----------|--------|
| 15 | **Oversized Composables (1300-1500 lines)** | UI files | Hard to maintain and review |
| 16 | **No ViewModel-per-screen pattern** | UI architecture | Tight coupling prevents test isolation |
| 17 | **No type-safe navigation routes** | `MainActivity.kt` | String-based routes error-prone |
| 18 | **Monolithic SettingsRepository (695 lines)** | `SettingsRepository.kt` | Handles too many concerns |
| 19 | **No caching layer for DataStore** | `SettingsRepository.kt` | Every flow triggers full read |
| 20 | **Dependencies 6+ months outdated** | `app/build.gradle.kts` | Missing security patches and improvements |
| 21 | **No network security config** | Missing file | Should explicitly disable cleartext |
| 22 | **`versionCode = 1` never incremented** | `app/build.gradle.kts` | Not production-ready versioning |
| 23 | **Apt/Yuanqu protocols skip CRC** | Protocol files | Corrupted frames could emit invalid metrics |
| 24 | **Typo: `protcols/` directory** | `ble/bms/protcols/` | Should be `protocols/` |
| 25 | **`.gitignore` uses `*.jks` not `**/*.jks`** | `.gitignore` | Keystore in subdirectory could be committed |

---

## 10. Recommended Action Plan

### Phase 1: Critical Fixes (Immediate)

1. **Implement `onCharacteristicWrite` callback in `BleManager.kt`**
   - Propagate write results to callers via `Channel` or `SharedFlow`
   - Log write status codes for debugging

2. **Fix `writeBytes` to provide feedback**
   - Return `Boolean` or `Result<Unit>` instead of silent return
   - Log when characteristic is null

3. **Add scan timeout**
   - Auto-stop scan after 10 seconds
   - Provide mechanism to restart scan if needed

4. **Add ViewModel cleanup**
   - Call `bleManager.disconnect()` and `bmsBleManager.disconnect()` in `MainViewModel.onCleared()`

5. **Enable minification for release builds**
   - Create `proguard-rules.pro` with appropriate rules
   - Set `isMinifyEnabled = true` and `isShrinkResources = true`
   - Test release build thoroughly

---

### Phase 2: Architecture Improvements (Next Sprint)

6. **Introduce Dependency Injection**
   - Start with Hilt or Koin
   - Extract interfaces for `BleManager`, `SettingsRepository`, `GpsTracker`, etc.
   - Enable unit testing with mocked dependencies

7. **Split MainViewModel**
   - Extract `BleViewModel` for BLE connection/state management
   - Extract `RideSessionViewModel` for session tracking
   - Extract `SettingsViewModel` for settings management
   - Extract `HistoryViewModel` for ride history
   - Use `SavedStateHandle` for configuration change survival

8. **Consolidate BleManager instances**
   - Single `BleManager` with multiple GATT connections
   - Coordinate scanning to avoid conflicts with active connections

---

### Phase 3: Code Quality (Following Sprint)

9. **Extract sub-components from large files**
   - `DashboardScreen.kt`: Extract metric cards, grid layout, connection status
   - `SettingsScreen.kt`: Extract vehicle list, speed source picker, backup UI
   - `MainActivity.kt`: Extract navigation graph, PiP logic, permission handling

10. **Add type-safe navigation**
    - Use sealed class with `@Serializable` routes
    - Migrate from string-based routes to type-safe routes

11. **Split SettingsRepository**
    - `VehicleRepository`: Vehicle profile CRUD
    - `SettingsRepository`: App settings
    - `HistoryRepository`: Ride/speed test history
    - `BackupRepository`: Import/export

---

### Phase 4: BLE Reliability (Ongoing)

12. **Handle `onServicesDiscovered` failure**
    - Log failure status
    - Attempt re-discovery or transition to error state

13. **Add MTU negotiation**
    - Request MTU 247 after service discovery for Zhike devices
    - Handle MTU change callback

14. **Add permission check to `startScan()`**
    - Check `BLUETOOTH_SCAN` before starting scan
    - Request permission if not granted

15. **Add notification disable before disconnect**
    - Write `DISABLE_NOTIFICATION_VALUE` to CCCD
    - Call `setCharacteristicNotification(char, false)`

16. **Add scan filters and settings**
    - Filter by service UUID (0000FFE0)
    - Use `ScanSettings` with `SCAN_MODE_LOW_LATENCY` for connection
    - Use `SCAN_MODE_LOW_POWER` for background scanning

---

### Phase 5: Maintenance & Polish (Ongoing)

17. **Update dependencies**
    - AGP to 8.8+
    - Kotlin to 2.1.x
    - Compose BOM to 2025.x
    - Migrate from Accompanist Permissions to AndroidX

18. **Implement version management**
    - Auto-generate `versionCode` from git commit count
    - Manage `versionName` via CI/CD or manual process

19. **Add network security config**
    - Create `res/xml/network_security_config.xml`
    - Explicitly disable cleartext traffic

20. **Fix `.gitignore` patterns**
    - Change `*.jks` to `**/*.jks`
    - Change `*.keystore` to `**/*.keystore`
    - Change `*.p12` to `**/*.p12`

21. **Fix typo: `protcols/` → `protocols/`**

22. **Audit and remove dead dependencies**
    - Verify if CameraX/ZXing/ML Kit are used
    - Remove if unused

---

## Appendix A: File Inventory

### Core Application Files
- `MainActivity.kt` (956 lines) — Activity lifecycle, navigation, PiP
- `MainViewModel.kt` (2272 lines) — God object, all app state
- `HabeApplication.kt` — Application class

### BLE Layer
- `ble/BleManager.kt` — BLE connection/scanning/polling
- `ble/ProtocolParser.kt` — Protocol routing and metrics
- `ble/protocols/ControllerProtocol.kt` — Interface contract
- `ble/protocols/ZhikeProtocol.kt` — Zhike controller protocol
- `ble/protocols/AptProtocol.kt` — APT/AUSI protocol
- `ble/protocols/YuanquProtocol.kt` — Far-Drive protocol
- `ble/protocols/ZhikeParameterCatalog.kt` (664 lines) — Parameter definitions
- `ble/bms/BmsParser.kt` — BMS protocol router
- `ble/bms/protcols/JkBmsProtocol.kt` — JK BMS protocol
- `ble/bms/protcols/AntBmsProtocol.kt` — ANT BMS protocol

### Data Layer
- `data/SettingsRepository.kt` (695 lines) — All data persistence
- `data/VehicleProfile.kt` — Vehicle data model
- `data/AutoCalibrator.kt` — GPS wheel calibration
- `data/RideEnergyEstimator.kt` — Energy/range estimation
- `data/RideSession.kt` — Ride session data model
- `data/gps/GpsTracker.kt` — GPS speed tracking
- `data/gps/HeadingTracker.kt` — Compass heading tracking

### UI Layer
- `ui/dashboard/DashboardScreen.kt` (1348 lines) — Main instrument cluster
- `ui/settings/SettingsScreen.kt` (1554 lines) — Settings page
- `ui/settings/zhike/ZhikeSettingsScreen.kt` — Zhike parameter editor
- `ui/bms/BmsScreen.kt` — BMS monitoring
- `ui/connect/ConnectScreen.kt` — BLE connection UI
- `ui/speedtest/SpeedTestScreen.kt` — Speed test UI
- `ui/poster/PosterScreen.kt` — Ride summary poster
- `ui/navigation/PredictiveBackMotion.kt` — Predictive back animations
- `ui/navigation/DialogWindowEffects.kt` — Blur backdrop dialogs
- `ui/theme/` — Material 3 theming
- `ui/text/` — Text display utilities

### Debug & Utilities
- `debug/AppLogger.kt` — Application logging

### Build & Scripts
- `build.gradle.kts` — Root build configuration
- `app/build.gradle.kts` — App build configuration
- `gradle.properties` — Gradle properties
- `.agents/scripts/*.sh` — Automation scripts
- `.agents/workflows/*.md` — Workflow documentation

---

## Appendix B: Testing Status

**Current State:** NO-SOURCE — zero unit tests exist in the repository.

**Recommendations:**
1. Start with pure function tests:
   - `ZhikeProtocol.parse()` with known frame bytes
   - `AutoCalibrator` state transitions
   - `RideEnergyEstimator` calculations
   - `ZhikeParameterCatalog` encode/decode

2. Add ViewModel tests with mocked dependencies (requires DI framework):
   - BLE state transitions
   - Settings read/write
   - Ride session lifecycle

3. Add integration tests:
   - BLE connection flow (requires mock BLE peripheral or hardware-in-loop)
   - Protocol parsing with real controller

---

## Conclusion

The SmartDash by Shawn Rain project demonstrates strong modern Android development practices with Compose, Coroutines, and clean domain separation. The Zhike protocol implementation and parameter catalog are particularly well-designed.

However, the project has critical gaps in BLE reliability (no write feedback, silent drops, no timeouts), testability (no DI, monolithic ViewModel), and production readiness (no minification, version management). Addressing these issues should be prioritized before any public release or handoff.

The recommended action plan provides a phased approach to systematically improve the codebase while maintaining development velocity. Start with Phase 1 critical fixes, then progressively tackle architecture improvements and code quality.

---

**End of Audit Report**

*Generated: April 6, 2026*  
*Next Review: After Phase 1 completion*
