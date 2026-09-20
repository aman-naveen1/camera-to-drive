# DriveCam — UI/UX Direction

## Visual goal
DriveCam should look and behave like a modern phone camera, not a file-upload utility.

Characteristics: fullscreen preview, minimal chrome, dark translucent controls, large central record control, subtle status indicators, and unobtrusive Drive state.

## Primary screen
### Camera preview
- Fullscreen.
- Rear camera by default.
- Performance-oriented PreviewView.
- Remains visible while recording.

### Top
- Drive status pill.
- Quality/recording information.
- Top gradient scrim.

### Center
- Focus feedback near the tapped location.
- No persistent large overlays.

### Bottom
- Recording status.
- Large record button.
- Google connection action when needed.
- Bottom gradient scrim.

## Style
- Near-black base.
- White/light text.
- Translucent dark pills.
- Soft transparent gradients.
- Avoid excessive accent colors.

## Record button
Ready: prominent circular control.
Recording: clearly changes to communicate stop.
Disabled: subdued when required permissions/auth are unavailable.

It should be easy to hit one-handed and never require confirmation for ordinary start/stop.

## Drive status
Examples: DRIVE • OFFLINE and DRIVE • READY.

The state must make it obvious whether recording can upload. Attempting to record without authorization should explain the requirement and begin the Google connection flow.

## Quality indicator
Examples: AUTO • 1080P, AUTO • 720P, AUTO • DEVICE.

Do not advertise a quality that CameraX did not successfully select.

## Gestures
Supported: tap = focus; pinch = zoom.

Avoid custom edge-swipe-to-exit and gestures that interfere with Android system navigation.

## First-run flow
Launch → camera/microphone permission → camera preview → Google sign-in → Drive authorization → ready.

If Google sign-in is cancelled, keep the camera preview usable but clearly indicate that Drive connection is required.

## Feedback
Use short messages such as SIGNING IN…, GRANTING DRIVE ACCESS…, READY • DRIVE CONNECTED, CONNECT GOOGLE DRIVE FIRST, ● RECORDING • UPLOADING, CAMERA + MICROPHONE REQUIRED, and DRIVE AUTHORIZATION FAILED.

Do not show technical stack traces in normal UI.

## Accessibility
- Content descriptions for controls.
- Readable text over changing camera backgrounds.
- Comfortable touch targets.
- Do not rely on color alone.

## Thermal UX
Long recordings are expected. Do not encourage maximum resolution simply for marketing. Choose sustained quality based on device support and stability.

## Design constraint
The first stable product should preserve: open → connect → record → upload automatically.
