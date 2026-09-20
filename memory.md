# DriveCam — Project Context / Memory

## Identity
- Project: DriveCam
- Repository: aman-naveen1/camera-to-drive
- Android package: com.aman.camera2drive
- Purpose: continuous camera recording with automatic 5-minute Google Drive uploads.

## Core decision
Temporary local segments are mandatory: record 5 minutes → finalize → upload to Drive → delete local segment after success → start next segment.

## UX decisions
- Fullscreen camera preview.
- Minimal controls.
- Prominent record button.
- Drive status pill.
- Quality status pill.
- Tap focus.
- Pinch zoom.
- No custom edge-swipe navigation.

## Authentication decisions
- Credential Manager for Google identity sign-in.
- Web OAuth client ID as Credential Manager server client ID.
- Google Identity AuthorizationClient for Drive authorization.
- drive.file scope.
- Android OAuth client uses package + signing certificate SHA-1.
- No client secret in APK/repository.

## Drive decisions
- Folder name: DriveCam.
- Folder ID cached locally.
- Resumable uploads.
- Local deletion only after confirmed success.
- Failed files remain retryable.

## Camera decisions
- Rear camera default.
- CameraX stack.
- Prefer FHD/1080p for sustained recording when supported.
- Fall back to HD/SD/lowest.
- Stabilization only when supported.
- Do not blindly force maximum sensor resolution.
- Optimize for thermals, bitrate, encoder stability, and upload practicality.

## Component boundaries
- MainActivity: UI, permissions, preview, sign-in, foreground authorization.
- RecordingService: long-running recording lifecycle.
- SegmentRecorder: VideoCapture and segment rotation.
- DriveUploader: upload queue and Drive API.
- DriveAuth: Google Drive authorization/token retrieval.

## Important history
Previous CI issues included Drive API dependency resolution, LifecycleService typing, missing coroutines/HTTP dependencies, MediaHttpUploader usage, duplicate META-INF resources, foreground-service camera/microphone declarations, Credential Manager integration, Drive authorization, camera gesture handling, quality fallback, and build-system modernization.

The project had a previously successful debug build, but recent modernization changed dependencies and camera/auth architecture. Current CI state is authoritative.

## Current build strategy
GitHub Actions uses Ubuntu, Java 17, Gradle setup, assembleDebug, SHA-1 output after the build, and APK artifact upload.

The SHA-1 step is intentionally after assembleDebug so the default debug keystore exists.

## Current configuration
The Google Web OAuth client ID is configured in app/src/main/res/values/strings.xml.

Android OAuth still requires the package name and debug signing SHA-1.

## Safety constraints
- Never delete an MP4 before confirmed Drive upload.
- Never expose tokens in logs.
- Never add a client secret.
- Never sacrifice system back navigation.
- Never assume a camera capability.
- Never treat CI success as runtime proof.

## Development method
1. Inspect actual repository state.
2. Read the project documentation.
3. Read the actual CI error.
4. Make the smallest correct change.
5. Rebuild.
6. Verify.
7. Update documentation when a significant decision changes.

These markdown files preserve project context so future development does not require reconstructing requirements from chat history.
