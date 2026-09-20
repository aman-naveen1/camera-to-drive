# DriveCam

5-minute-segment Android camera recorder for Google Drive.

- CameraX preview
- 5-minute MP4 segments
- Microphone audio
- Foreground recording service
- Google Sign-In with drive.file scope
- Upload queue/retry
- Local file deleted only after successful Drive upload

## Google Cloud
Create a Google Cloud project, enable Drive API, configure OAuth consent, and create an Android OAuth client for package `com.aman.camera2drive`.

## Build
`gradle assembleDebug`

The APK will be under `app/build/outputs/apk/debug/app-debug.apk`.
