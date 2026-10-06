# Sound and vibration compatibility

Event audio and vibration belong to one app engine. Message notifications do
not add a second default sound/vibration. New silent notification channels
replace the old channels because Android does not allow an app to change an
existing channel's alert behavior. Migration preserves the old importance,
including a blocked channel; existing new channels retain user changes.

Sounds use the notification volume, not media volume. Every event checks the
current ringer mode, call/audio mode and app/event preferences. Vibration is
allowed in normal and vibrate modes, suppressed in silent mode and calls.
On API 24+ both signals respect the system notification switch, and on API 26+
they respect blocked/silent importance of the selected message channel. Explicit sound previews
bypass app/event switches and notification blocking but obey volume, ringer
and call state. Android 5-7 heads-up ranking uses a zero-duration vibration
pattern so removing default alerts does not remove the popup.

Player creation, asynchronous preparation, playback and cleanup run on the
main looper. Each completed/failed/replaced player is released. A new event
replaces the previous sound; stale callbacks cannot restart or release its
replacement. Service shutdown closes the engine and cancels vibration.
Inaccessible custom files fall back to the matching built-in sound; file paths
and content URIs are accepted. Decoder errors are logged and cleaned up.

Vibration uses legacy calls on API 4-20, notification AudioAttributes on API
21-25, and VibrationEffect with notification AudioAttributes on API 26+.
Modern classes are isolated in nested API helpers so Dalvik on Android 1.6
does not resolve them. Zero/negative durations and invalid patterns are no-ops;
invalid duration preferences fall back to 200 ms.

## Repeatable checks

```sh
python compatibility/check_alerts.py
./gradlew :app:assembleDebug :app:assembleRelease :app:lintDebug -PlegacyApiCheck
python compatibility/check_android_api.py --self-test
python compatibility/check_resources.py
adb shell am instrument -w ru.ivansuper.jasmin/ru.ivansuper.jasmin.compat.AlertDeviceProbe
```

The host suite compiles the actual Media, AlertCompat, AlertPolicy and
NotificationBuilder classes against recording platform boundaries. On
2026-10-06 it passed 3,438 assertions covering SDK 4 through 36: all nine
event/resource mappings, notification volume and attributes, ringer/call
states, global and event switches, previews, custom sources, descriptor
closure, preparation-time preference changes, rapid events, decoder errors,
shutdown, vibration API selection, background notification usage and channel
migration/blocking. CI runs this suite on Java 11 and 17.

The debug-only AlertDeviceProbe passed on a real Android 1.6/API 4 emulator
image: all nine packaged OGG files prepared, started and completed using the
framework MediaPlayer. Duration/pattern vibration calls and invalid-input
no-ops completed without application or verifier errors in logcat. The
emulator ran without audio output; these results confirm decoder/playback
callbacks and API execution, not an audible speaker or a physical motor.
The probe is absent from release builds.

Debug/Release builds, NewApi lint, the API-4 reference/signature audit and
resource audits passed. The resource audit also requires all nine OGG files to
remain uncompressed for openRawResourceFd, alongside stored/aligned ARSC.

The modern emulator could not start because the host disk had insufficient
free space. API 21/24/26+ branches therefore have host coverage and compiled
API checks, not device execution in this session. Physical speaker/motor,
vendor DND behavior and live background message delivery on Android 16 still
require a phone check; the host suite is not a claim that every Android version
or vendor device was run.

Platform references: [Vibrator notification usage](https://developer.android.com/reference/android/os/Vibrator),
[channel behavior](https://developer.android.com/develop/ui/compose/notifications/channels),
and [Android 5.1 heads-up ranking](https://github.com/aosp-mirror/platform_frameworks_base/blob/android-5.1.1_r38/packages/SystemUI/src/com/android/systemui/statusbar/BaseStatusBar.java).
