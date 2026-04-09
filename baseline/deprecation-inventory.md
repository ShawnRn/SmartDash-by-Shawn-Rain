# SmartDash Deprecation Inventory

Track deliberate `@Suppress("DEPRECATION")` usage here.

## Rules
- Explain why the deprecated API is still required.
- Point to the replacement API if known.
- Remove the entry when the suppress is deleted.

## Active Entries
- `app/src/main/java/com/shawnrain/sdash/data/sync/GoogleDriveAuth.kt`
  Google Sign-In is still the current auth surface used by the app's Drive sync flow. A full migration needs a product-level replacement path, not a local rename.
- `app/src/main/java/com/shawnrain/sdash/data/sync/GoogleDriveSyncManager.kt`
  Same reason as above; deprecated Google Sign-In access is intentionally isolated here until the auth stack is migrated.
- `app/src/main/java/com/shawnrain/sdash/data/sync/DriveSyncCoordinator.kt`
  The coordinator still derives the encryption password from the signed-in Google account email, so it inherits the temporary Google Sign-In dependency.
- `app/src/main/java/com/shawnrain/sdash/ui/navigation/DialogWindowEffects.kt`
  Window color/contrast APIs still require deprecated compatibility branches on older Android versions.
- `app/src/main/java/com/shawnrain/sdash/ble/BleManager.kt`
  Bluetooth descriptor writes still need legacy branches below Tiramisu; local suppressions are kept at the call sites only.
- `app/src/main/java/com/shawnrain/sdash/data/update/AppUpdateManager.kt`
  Package install capability checks still use a deprecated compatibility accessor on older API levels.
