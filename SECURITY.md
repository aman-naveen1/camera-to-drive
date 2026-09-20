# DriveCam Security Review

## Scope

Static security review of the Android application source, manifest, OAuth/Drive integration, local storage, component exposure, and network configuration.

This review is not a substitute for a device-level penetration test. Runtime interception, rooted-device testing, APK reverse engineering, TLS interception, and Google OAuth consent-flow testing require an installed APK and test device/account.

## Findings

### High
None identified in the reviewed source.

### Medium
- **Debug APK is not a production artifact.** The CI artifact is a debug build. Production distribution should use a dedicated release signing key and release build configuration.
- **Local footage cleanup is best-effort.** Recordings are kept in app-private cache storage. Android's app sandbox prevents ordinary third-party apps from reading them, but a rooted/compromised device can access them.
- **No backend-side abuse control exists.** The app talks directly to Google APIs using user authorization. There is no DriveCam server to enforce device registration, rate limits, or server-side policy.

### Low / Informational
- The Google OAuth web Client ID is public configuration and is not a secret.
- The app uses the narrow drive.file scope rather than the broad drive scope.
- The recording service is explicitly exported=false.
- Cleartext HTTP is explicitly disabled.
- No custom WebView, dynamic code loading, embedded private key, password, or hard-coded OAuth client secret was found in the reviewed source.
- Temporary MP4 files are generated with app-controlled timestamp names and stored under cacheDir/segments.

## Hardening applied

- Added an in-app Security screen.
- Added explicit usesCleartextTraffic=false.
- Added a Network Security Config with cleartext disabled.
- Added runtime status for camera/microphone permission state.
- Added a visible warning when running a debug build.
- Changed upload handling so a non-successful HTTP upload response cannot be treated as a successful upload before local footage cleanup.

## Test cases for device pentest

1. Deny camera permission.
2. Deny microphone permission.
3. Revoke Google Drive permission after initial authorization.
4. Disable network during an upload.
5. Kill the app/service during an upload.
6. Fill local storage while recording.
7. Attempt to access the service from another application.
8. Inspect app-private cache from a non-rooted second app.
9. Test HTTP interception with a controlled proxy.
10. Test malformed OAuth return intents.
11. Test account switching and Drive permission revocation.
12. Test repeated start/stop recording rapidly.
13. Test duplicate segment upload after process death.
14. Verify a failed upload never deletes the local segment.
15. Verify a successful upload eventually removes the local segment.

## Out of scope

- Attacking Google infrastructure.
- Attacking accounts or devices that do not belong to the tester.
- Credential theft or token extraction from third-party accounts.
