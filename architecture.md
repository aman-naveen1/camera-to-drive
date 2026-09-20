# DriveCam — Project Architecture

## Overview
DriveCam has four main runtime responsibilities:
1. MainActivity — UI, permissions, camera preview, Google sign-in/authorization.
2. RecordingService — foreground-service lifecycle for long-running recording.
3. SegmentRecorder — CameraX video capture and 5-minute segment rotation.
4. DriveUploader — local queue and Google Drive upload.

Flow: MainActivity → RecordingService → SegmentRecorder → local MP4 → DriveUploader → Google Drive → delete local file on success.

## Component boundaries
### MainActivity
Owns camera UI, permissions, Preview, tap focus, pinch zoom, Credential Manager, foreground Drive authorization resolution, recording controls, and status display.

It does not own long-running uploads, segment timers, Drive REST upload logic, or deletion of uploaded files.

### RecordingService
A LifecycleService that runs as an Android foreground service with camera and microphone types. It owns the long-running recording lifecycle and controls SegmentRecorder.

### SegmentRecorder
Uses CameraX VideoCapture. Selects quality in order FHD, HD, SD, LOWEST. Enables stabilization only when supported. Writes MP4 segments under app cache, records about five minutes, validates finalized files, enqueues valid files, and starts the next segment.

It must not call unbindAll when the Activity owns Preview.

### DriveUploader
Maintains a queue of MP4 files, discovers existing MP4s, obtains an already-authorized Drive access token, builds the Drive API client, finds/creates DriveCam, uses resumable upload, deletes only after confirmed success, and retries transient failures.

### DriveAuth
Requests drive.file, handles foreground authorization resolution, and provides an access token for already-granted authorization to the uploader. It never uses a client secret.

## Authentication model
Credential Manager handles Google identity sign-in. The Web OAuth client ID is supplied as the server client ID. Google Identity AuthorizationClient separately handles Drive authorization.

- Web client: Credential Manager server client ID.
- Android client: installed package + signing certificate identity.

## Storage lifecycle
record → finalize → validate → enqueue → upload → success: delete local file → failure: retain and retry.

This order is mandatory.

## Camera lifecycle
The Activity owns Preview. The foreground service owns VideoCapture. Components must not destroy each other's CameraX use cases.

If separate lifecycle ownership proves unreliable on a target device, refactor toward a shared camera controller/lifecycle rather than adding ad-hoc unbind/rebind behavior.

## Build
GitHub Actions performs checkout, Java 17 setup, Gradle setup, assembleDebug, debug SHA-1 output, and APK artifact upload.

The SHA-1 is required for Android OAuth configuration.

## Dependency philosophy
Use mutually compatible stable versions. Important libraries include AndroidX, CameraX, Lifecycle Service, Coroutines, Credential Manager, Google Identity, Google API/HTTP/Auth clients, and Drive API.

When a build fails, inspect the actual compiler error and make the smallest required API/dependency change.
