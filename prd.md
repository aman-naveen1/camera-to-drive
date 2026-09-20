# DriveCam — Product Requirements Document

## Product
DriveCam is a lightweight Android camera app that records continuously in short video segments and uploads completed segments to the user's Google Drive.

Core flow: Camera → temporary local 5-minute MP4 → Google Drive upload → delete local file only after successful upload → immediately record the next segment.

## User experience
1. Install one APK.
2. Grant camera and microphone permissions.
3. Sign in with Google.
4. Grant Google Drive access.
5. See a live camera preview.
6. Press one prominent record button.
7. Leave the app recording while 5-minute segments upload automatically.
8. Local segments are removed only after successful Drive upload.
9. Stop recording with one tap.
10. No manual OAuth/development setup is required for the end user.

## Functional requirements
### Camera
- Rear camera by default.
- Fullscreen live preview.
- Tap-to-focus.
- Pinch-to-zoom.
- No custom edge-swipe gesture that interferes with Android system back navigation.
- Keep the screen awake while the camera UI is open.
- Prefer sustained video quality over maximum sensor resolution.
- Prefer FHD/1080p when supported, with graceful fallback.
- Use device video stabilization when CameraX reports support.
- Never force unsupported capabilities.

### Recording
- Approximately 5-minute segments.
- Automatically start the next segment after finalization.
- Keep the current segment locally until Drive confirms upload.
- Never delete merely because an upload was queued.
- Failed uploads retain the segment and retry.
- Discover pending MP4 segments after service/app restart.
- Use a foreground service with camera and microphone types.

### Google authentication
- Android Credential Manager for Google sign-in.
- Web OAuth client ID as Credential Manager server client ID.
- Google Identity AuthorizationClient for Drive authorization.
- Android OAuth client for installed app package/signing identity.
- Never embed a client secret in the APK.
- Returning users should have low-friction sign-in.
- Authorization resolution must be presented from the foreground Activity.

### Google Drive
- Request drive.file, not broad Drive access.
- Use a Drive folder named DriveCam.
- Cache the folder ID.
- Use resumable uploads.
- Delete local files only after successful upload confirmation.

## UX
The interface should feel like a modern phone camera rather than a cloud-storage utility: fullscreen preview, minimal controls, dark/translucent overlays, large central record control, small Drive and quality pills, subtle top/bottom scrims, and clear permission/authentication states.

## Performance
- Avoid unnecessary background work.
- Avoid copying large video files.
- Avoid holding video in memory.
- Prefer hardware camera/encoder paths.
- Optimize for sustained recording, thermal stability, bitrate, and upload practicality.

## Reliability
- Failed upload must never destroy the only local copy.
- Do not enqueue corrupt/empty recordings.
- Network loss must not itself terminate recording.
- OAuth failures must not crash the app.
- Camera capability differences must be handled gracefully.

## Security/privacy
- Never commit a client secret.
- Never log access/ID tokens.
- Request only drive.file.
- Recordings remain local until upload succeeds.
- No unrelated analytics/tracking without an explicit requirement.

## First-version non-goals
- Cloud backend.
- Video editing.
- Live streaming.
- Multi-camera.
- Other cloud providers.
- Automatic Drive retention/deletion policies.
- Artificial enhancement that materially increases sustained load.

## Definition of done
The first stable build installs, authenticates, records, rotates 5-minute segments, uploads them to DriveCam, deletes successful local segments, retains failed segments, survives ordinary network interruption, and preserves Android system navigation.
