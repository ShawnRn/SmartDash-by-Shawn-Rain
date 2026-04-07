# Response Draft — Habe Dashboard Audit (2026-04-06)

Hi Qwen team,

Thanks for the thorough audit. We reviewed the report against the current codebase and the feedback is **largely valid**.  
Our assessment: about **70–80% of findings are actionable and accurate**, with a few items that are outdated or need correction.

## 1) Findings We Agree With (and will prioritize)

1. BLE write observability gap is real:
   - `BleManager` currently has no `onCharacteristicWrite` callback handling.
   - `writeBytes(...)` can return early when characteristic is null (silent drop risk).
2. Scan reliability gaps are real:
   - `startScan()` currently has no timeout, no filter, no explicit `BLUETOOTH_SCAN` permission check.
3. Service discovery failure path is incomplete:
   - `onServicesDiscovered(...)` only handles `GATT_SUCCESS`.
4. Lifecycle cleanup is incomplete:
   - `MainViewModel.onCleared()` currently does not disconnect BLE managers.
5. Build hardening is incomplete:
   - Release `isMinifyEnabled=false`, and `proguard-rules.pro` is referenced but missing.
6. Architecture debt is real:
   - `MainViewModel` is oversized and currently mixes too many concerns.

## 2) Findings That Are Partially True

1. “Two `BleManager` instances double overhead”:
   - Partly true. This is currently intentional (controller + BMS split), but we agree there is optimization room and coordination complexity.
2. “compileSdk 36 is risky/preview”:
   - Risk framing may be time-sensitive. We accept the doc/config drift point and will keep documentation aligned to actual SDK choices.
3. “No network security config”:
   - True factually; practical risk is lower for our BLE-first architecture, but adding explicit config is still good hygiene.

## 3) Findings We Consider Inaccurate / Need Correction

1. “Camera/ML Kit/ZXing may be dead dependencies”:
   - Not accurate in our current branch. CameraX + ML Kit are actively used in the Settings QR migration flow.
2. “`.gitignore` should use `**/*.jks` instead of `*.jks`”:
   - In Git ignore semantics, `*.jks` already matches recursively unless anchored. So this specific claim is not strictly correct.
3. Some file line counts are outdated:
   - The report’s size metrics do directionally reflect the issue, but exact numbers differ from current head.

## 4) Our Concrete Next Steps

### Immediate (Phase 1)
- Add `onCharacteristicWrite` status handling + write result telemetry.
- Make `writeBytes` return explicit result/log for drop conditions.
- Add scan timeout + scan filter + explicit permission guard in `startScan`.
- Handle non-success `onServicesDiscovered` with retry/fail-fast state transition.
- Add BLE disconnect in `MainViewModel.onCleared()`.
- Create `proguard-rules.pro` and enable release minify/shrink in a controlled rollout.

### Near-term (Phase 2)
- Split `MainViewModel` into domain ViewModels/use-cases.
- Introduce DI boundary (framework or at least interface-first constructor injection).
- Improve BLE manager ownership model for controller/BMS coexistence.

Thanks again — this audit is useful and we’ll treat it as a working backlog rather than a one-shot checklist.

