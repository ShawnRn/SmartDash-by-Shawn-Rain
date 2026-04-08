---
name: smartdash-overlay-dialog
description: Use when creating or updating any SmartDash dialog, bottom sheet, centered overlay, fullscreen detail layer, or "about/update" style popup. Enforces SmartDash-specific overlay behavior: bottom-of-settings entry placement, constrained width/height, icon fitting instead of forced cropping, predictive back, blur backdrop, unified dismiss motion, and layout patterns that match the speed, ride record, and ride detail overlays.
---

# SmartDash Overlay Dialog

Use this skill whenever work touches:

- settings "about" entry cards
- update popups
- custom `Dialog`
- custom overlay / sheet / detail layer
- fullscreen detail surfaces

## Core rule

Do not invent a fresh popup style.

Match the existing SmartDash overlay language from:

- `app/src/main/java/com/shawnrain/sdash/ui/speedtest/SpeedtestScreen.kt`
- `app/src/main/java/com/shawnrain/sdash/ui/navigation/PredictiveBackMotion.kt`
- `app/src/main/java/com/shawnrain/sdash/ui/navigation/DialogWindowEffects.kt`
- `.agents/workflows/overlay-dialog.md`
- `.agents/workflows/predictive-back.md`

## Required behavior

1. Entry placement
- "About" style entries belong at the bottom of Settings, not in the middle of the main settings stack.

2. Container geometry
- Prefer a constrained overlay surface over a tall generic sheet.
- Respect screen width with outer insets and a max content width.
- Keep height intentional; avoid giant empty columns and avoid pushing content into long wrapped stacks.

3. Icon handling
- Never fake-fit the app icon by dropping it raw into an arbitrary rounded rectangle.
- Preserve aspect ratio with `ContentScale.Fit`, controlled inner padding, and a container sized for the real asset.

4. Motion
- Use the same family of entry/dismiss motion as speed / ride-record / ride-detail overlays.
- Dismiss paths must be unified: back gesture, scrim tap, button tap, drag-dismiss all use the same dismiss channel.

5. Predictive back
- Reuse `rememberPredictiveBackMotion` or `PredictiveBackPopupTransform`.
- Provide scale, translation, alpha, and corner/inset response during the gesture.

6. Blur and scrim
- Reuse `ApplyDialogWindowBlurEffect` and `PopupBackdropBlurLayer`.
- Blur is a baseline behavior, not an optional flourish.

7. Text layout
- Avoid narrow text columns that force ugly wrapping.
- Keep hero copy short.
- Prefer compact rows, chips, and grouped actions over long descriptive paragraphs.

## Implementation workflow

1. Find the closest existing overlay in `SpeedtestScreen.kt`.
2. Copy its container geometry and motion structure before changing visuals.
3. Adapt content density for the new feature.
4. Check portrait width, visual balance, and line wrapping.
5. Verify predictive back, scrim tap, drag dismiss, and normal close button.
6. Run `./gradlew :app:compileDebugKotlin --console plain`.

## Red flags

- full-width card with uncontrolled text wrapping
- icon cropped or visually floating inside a random rounded box
- overlay taller than needed
- a bottom sheet used where a constrained centered dialog is visually closer to existing SmartDash detail overlays
- one entrance animation and a different exit animation path
