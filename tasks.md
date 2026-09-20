# DriveCam — Tasks & Progress

Legend:
- [x] Complete
- [~] Implemented but requires verification
- [ ] Not complete

## Product
- [x] 5-minute segmented recording model.
- [x] Drive upload/delete lifecycle.
- [x] Minimal camera-first UX.
- [x] No custom edge-back gesture.
- [x] Hardware-aware quality strategy.

## Android
- [x] Android application module.
- [x] Package com.aman.camera2drive.
- [x] Minimum SDK 26.
- [x] Foreground recording service.
- [x] Camera/microphone permissions.
- [x] Camera/microphone foreground-service types.
- [x] Java 17 CI.

## Camera
- [x] CameraX preview.
- [x] Rear-camera default.
- [x] Tap-to-focus.
- [x] Pinch-to-zoom.
- [x] Keep screen on.
- [x] Performance preview.
- [x] FHD → HD → SD → LOWEST fallback.
- [x] Optional stabilization path.
- [~] Verify CameraX API compatibility on CI.
- [~] Verify Preview + VideoCapture coexist on a physical device.
- [ ] Long-duration thermal testing.
- [ ] Verify actual selected encoder/quality on representative devices.

## Segmentation
- [x] Five-minute timer.
- [x] Automatic next segment.
- [x] MP4 cache directory.
- [x] Validate finalized segment.
- [x] Preserve failed segments.
- [x] Scan existing MP4 segments at uploader startup.
- [~] Verify no recording gaps during physical testing.

## Google authentication
- [x] Credential Manager integration.
- [x] Google ID credential extraction.
- [x] Web OAuth client ID configured in strings.xml.
- [x] Google Identity AuthorizationClient integration.
- [~] Android OAuth client configuration.
- [ ] Obtain/register CI debug SHA-1.
- [ ] Verify OAuth consent/test-user configuration.
- [ ] Test first-time sign-in.
- [ ] Test returning-user auto sign-in.
- [ ] Test denial/retry.
- [ ] Test revoked permission recovery.

## Google Drive
- [x] drive.file scope.
- [x] Drive API client.
- [x] DriveCam folder discovery.
- [x] DriveCam folder creation.
- [x] Folder ID caching.
- [x] Resumable upload path.
- [x] Delete local file only after success.
- [~] Verify access-token retrieval during long recording.
- [~] Verify retry under network loss.
- [ ] Offline recording followed by reconnection.
- [ ] Restart with pending segments.

## UI
- [x] Fullscreen layout.
- [x] Status pills.
- [x] Top/bottom scrims.
- [x] Central record control.
- [x] Google connection control.
- [x] Focus feedback.
- [x] Drive status.
- [x] Quality status.
- [x] Android back gesture left to system.
- [~] Physical-device polish pass.

## CI/build
- [x] GitHub Actions Android build.
- [x] Debug APK artifact upload.
- [x] Debug SHA-1 reporting step.
- [~] Resolve current compilation/API issues.
- [ ] Clean build.
- [ ] Confirm SHA-1 from successful build.
- [ ] Configure Android OAuth client with package + SHA-1.
- [ ] Install APK on physical device.
- [ ] Complete end-to-end test.
- [ ] Establish stable debug/release build strategy.

## Current priority
1. Get CI compiling.
2. Verify SHA-1 step executes.
3. Configure Android OAuth.
4. Test authentication.
5. Test recording.
6. Test Drive upload.
7. Test failure/retry paths.
8. Produce the first end-to-end verified APK.

## Known risks
- CameraX API compatibility.
- Preview and VideoCapture lifecycle ownership.
- Background Drive token authorization behavior.
- Android foreground-service restrictions.
- Device-specific quality/stabilization.
- Thermal throttling.
- Network interruption during upload.
- OAuth package/SHA-1 mismatch.

Do not mark runtime tasks complete merely because CI compiles.
