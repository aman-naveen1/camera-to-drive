# DriveCam

Lightweight Android camera recorder that writes rolling 5-minute MP4 segments and uploads them to a **DriveCam** folder in Google Drive.

## Design goals

- CameraX 1.6 high-performance camera stack.
- Simple camera-style UI instead of a full phone camera suite.
- 1080p-class recording by default, with device capability fallback.
- Device video stabilization is enabled when the camera reports support.
- Tap-to-focus and pinch-to-zoom.
- Preview stays visible while the foreground recording service records.
- Local segment is deleted only after a successful Drive upload.
- Resumable Drive uploads for unreliable mobile connections.
- Credential Manager for Google sign-in.
- Google AuthorizationClient for the narrow `drive.file` Drive permission.
- Predictive Back is left to Android instead of implementing custom back-swipe logic.

## Google setup

The modern Credential Manager Google flow requires the **Web OAuth client ID** from the same Google Cloud project.

Put it in:

`app/src/main/res/values/strings.xml`

Replace:

`REPLACE_WITH_YOUR_WEB_CLIENT_ID.apps.googleusercontent.com`

with your Web client ID.

Keep the Android OAuth client configured for package:

`com.aman.camera2drive`

and the SHA-1 of the certificate used to sign the APK.

The Drive permission remains `drive.file`, which is the narrow Drive scope used by this app.

## Build

```bash
gradle assembleDebug
```

The debug APK is produced at:

`app/build/outputs/apk/debug/app-debug.apk`
