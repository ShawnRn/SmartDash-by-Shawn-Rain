# SmartDash Suppress Guidelines

## Rules
- Prefer fixing the warning instead of suppressing it.
- Scope the suppress to the narrowest symbol possible.
- Every `DEPRECATION` suppress must have a nearby comment explaining the compatibility reason.
- Every file-level suppress must also be listed in `baseline/deprecation-inventory.md`.
- When a replacement API becomes practical, remove both the suppress and the inventory entry in the same change.

## Preferred Pattern
```kotlin
// Android 12 and below still require the legacy branch.
@Suppress("DEPRECATION")
legacyCall()
```

## Avoid
- File-level suppressions without inventory.
- Blanket multi-warning suppressions when only one API is intentionally deprecated.
- Using suppressions to hide newly introduced warnings in feature code.
