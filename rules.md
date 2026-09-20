# DriveCam — Coding Rules

## General
- Treat prd.md as the product source of truth.
- Treat architecture.md as the component-boundary source of truth.
- Keep changes focused.
- Do not add dependencies without a concrete reason.
- Prefer AndroidX/platform APIs.
- Do not silently change product behavior while fixing compilation.

## Security
- Never commit OAuth client secrets.
- Never log access tokens, ID tokens, or credential payloads.
- Treat recordings as private user data.
- Request drive.file only.
- No analytics/tracking without explicit approval.

## Recording safety
Mandatory order: record → finalize → validate → upload → delete.

Never delete before confirmed upload. Never discard a failed segment without a deliberate recovery policy.

## CameraX
- Do not call unbindAll from a component that does not own all use cases.
- Prefer supported quality fallback.
- Check optional capability APIs before enabling features.
- Avoid unnecessary camera restarts.
- Preserve live preview while recording.

## Android lifecycle
- Long-running recording belongs in RecordingService.
- Use a foreground service.
- Keep Activity UI separate from background work.
- Cancel coroutine scopes during Activity destruction.
- Remove delayed callbacks when recording stops.
- Do not retain Activity references in long-lived background objects.

## OAuth
- Credential Manager handles identity sign-in.
- Identity AuthorizationClient handles Drive authorization.
- Foreground UI may launch authorization resolution.
- Background upload may only use already-granted authorization.
- Never put client secrets in resources, source code, or CI logs.

## UI
- Keep recording UI minimal.
- Use short actionable status messages.
- Record button needs an accessible content description.
- Do not consume Android system back gestures.
- Camera gestures stay inside the camera interaction surface.

## Errors
- Never swallow errors when doing so could cause data loss.
- Automatic sign-in may suppress non-actionable errors.
- Background failures must remain recoverable.
- Retry transient upload failures with bounded backoff.
- Do not retry corrupt recordings indefinitely.

## Performance
- Avoid unnecessary frame/bitmap processing.
- Avoid copying large MP4 files.
- Use resumable uploads.
- Do not hold full video data in memory.
- Prefer hardware CameraX/encoder paths.
- Optimize sustained operation, not peak benchmark numbers.

## CI workflow
When CI fails: read the actual error, identify the failing API/dependency, fix the smallest affected area, rebuild, verify, then change another subsystem. Do not guess at CI errors.

## Documentation
When architecture/product behavior changes, update the relevant markdown file, tasks.md, and memory.md when the decision matters for future work.

## Compatibility
- Minimum SDK 26.
- Java 17.
- Compile SDK must satisfy selected AndroidX/CameraX dependencies.
- Guard newer Android APIs appropriately.
